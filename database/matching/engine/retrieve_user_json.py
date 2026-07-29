"""페르소나 → 벡터 유사도 기반 공고 추천 (stdout = JSON only).

Java(JobSimilarService)가 이 스크립트를 실행하고 stdout 을 그대로 JSON 파싱한다.
따라서 진단 로그는 전부 stderr 로만 출력한다.

동작:
  1) personas.json 에서 페르소나 로드 (id 대소문자 무시)
  2) DATABASE_URL 로 접속, job_postings + companies 조회 (컬럼은 실제 스키마를 introspect)
  3) 공고 텍스트 임베딩 → 유저 쿼리 임베딩과 코사인 유사도 계산
     · fastembed 사용 가능하면 ONNX 모델 사용
     · 불가하면 해시 기반 384차원 TF 벡터로 폴백 (의존성 없음)
  4) 코사인 + 스킬 교집합 + 기업규모 선호 보정 → score_final 내림차순
  5) job_postings.embedding(TEXT) 에 계산된 벡터를 캐싱 (다음 실행 재사용)

  set DATABASE_URL=postgresql://postgres:1234@localhost:5432/jobisbe
  python retrieve_user_json.py --persona sb --topn 20
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import sys

import numpy as np
import psycopg2
from psycopg2.extras import RealDictCursor

DIM = 384
SIZE_ALIAS = {
    "스타트업": {"스타트업", "벤처기업", "중소기업"},
    "벤처기업": {"벤처기업"},
    "중견기업": {"중견기업"},
    "대기업": {"대기업", "외국계(외국 투자기업)"},
}


def log(msg):
    print(msg, file=sys.stderr)


# ---------------------------------------------------------------- 임베딩
class HashingEmbedder:
    """의존성 없는 폴백 임베더. 토큰 해시 → 384차원 TF 벡터 (L2 정규화)."""

    name = "hashing-384"

    @staticmethod
    def _tokens(text):
        text = (text or "").lower()
        words = re.findall(r"[a-z0-9]+|[가-힣]+", text)
        out = []
        for w in words:
            out.append(w)
            # 한글은 bigram 으로 부분 일치 확보
            if len(w) > 1 and not w.isascii():
                out.extend(w[i:i + 2] for i in range(len(w) - 1))
        return out

    def embed(self, texts):
        mat = np.zeros((len(texts), DIM), dtype=np.float32)
        for i, t in enumerate(texts):
            for tok in self._tokens(t):
                h = int(hashlib.md5(tok.encode("utf-8")).hexdigest()[:8], 16)
                mat[i, h % DIM] += 1.0
        mat /= (np.linalg.norm(mat, axis=1, keepdims=True) + 1e-9)
        return mat


class FastEmbedEmbedder:
    name = "fastembed-MiniLM-L12-v2"
    MODEL = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"

    def __init__(self):
        from fastembed import TextEmbedding  # noqa: WPS433
        self.model = TextEmbedding(model_name=self.MODEL)

    def embed(self, texts):
        mat = np.array(list(self.model.embed(texts)), dtype=np.float32)
        mat /= (np.linalg.norm(mat, axis=1, keepdims=True) + 1e-9)
        return mat


def build_embedder():
    if os.environ.get("EMBEDDER", "").lower() == "hashing":
        log("[embed] EMBEDDER=hashing 지정 → 해시 임베더 사용")
        return HashingEmbedder()
    try:
        emb = FastEmbedEmbedder()
        log("[embed] fastembed 사용")
        return emb
    except Exception as e:  # 미설치/모델 다운로드 실패 등
        log(f"[embed] fastembed 사용 불가({type(e).__name__}: {e}) → 해시 임베더로 폴백")
        return HashingEmbedder()


# ---------------------------------------------------------------- DB
def existing_columns(cur, table):
    cur.execute(
        "SELECT column_name FROM information_schema.columns WHERE table_name=%s",
        (table,),
    )
    return {r["column_name"] for r in cur.fetchall()}


def fetch_jobs(cur, limit):
    cols = existing_columns(cur, "job_postings")
    ccols = existing_columns(cur, "companies")

    def jp(name, alias=None):
        return f"jp.{name} AS {alias or name}" if name in cols else f"NULL AS {alias or name}"

    def co(name):
        return f"c.{name} AS {name}" if name in ccols else f"NULL AS {name}"

    title_expr = (
        "COALESCE(jp.position, jp.title)" if {"position", "title"} <= cols
        else ("jp.position" if "position" in cols else "jp.title")
    )

    select = ", ".join([
        "jp.id AS id",
        jp("external_id"),
        f"{title_expr} AS position",
        co("name"),
        co("company_type"),
        co("employee_count"),
        jp("location_city"),
        jp("location_full"),
        jp("is_remote"),
        jp("career_min"),
        jp("career_max"),
        jp("skill_tags"),
        jp("source_url"),
        jp("main_tasks"),
        jp("requirements"),
        jp("preferred_points"),
        jp("intro"),
        jp("embedding"),
    ])

    where = "WHERE COALESCE(jp.status,'active') NOT IN ('REMOVED','removed')" if "status" in cols else ""
    sql = (
        f"SELECT {select} FROM job_postings jp "
        f"LEFT JOIN companies c ON c.id = jp.company_id {where} "
        f"ORDER BY jp.id DESC LIMIT %(limit)s"
    )
    cur.execute(sql, {"limit": limit})
    return cur.fetchall(), cols


def job_text(r):
    parts = [
        r.get("position"), r.get("name"),
        " ".join(r.get("skill_tags") or []),
        r.get("main_tasks"), r.get("requirements"),
        r.get("preferred_points"), r.get("intro"),
        r.get("location_full"),
    ]
    return " / ".join(p for p in parts if p)[:4000]


def persona_text(p):
    return " / ".join(filter(None, [
        " ".join(p.get("interest_roles") or []),
        "기술스택: " + ", ".join(p.get("skills") or []),
        p.get("free_text") or "",
    ]))


def size_ok(company_type, prefs):
    if not prefs or not company_type:
        return False
    allowed = set().union(*(SIZE_ALIAS.get(x, {x}) for x in prefs))
    return company_type in allowed


def parse_vec(txt):
    if not txt:
        return None
    try:
        v = np.array(json.loads(txt.replace("{", "[").replace("}", "]")), dtype=np.float32)
        return v if v.shape == (DIM,) else None
    except Exception:
        return None


def vec_literal(v):
    return "[" + ",".join(f"{x:.6f}" for x in v) + "]"


# ---------------------------------------------------------------- main
def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--persona", required=True)
    ap.add_argument(
        "--personas",
        default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "personas.json"),
    )
    ap.add_argument("--dsn", default=os.environ.get("DATABASE_URL"))
    ap.add_argument("--topn", type=int, default=20)
    ap.add_argument("--overfetch", type=int, default=300)
    a = ap.parse_args()

    if not a.dsn:
        log("DATABASE_URL(또는 --dsn) 필요")
        print(json.dumps({"error": "no dsn", "candidates": []}, ensure_ascii=False))
        return 1

    personas = {p["id"].lower(): p for p in json.load(open(a.personas, encoding="utf-8"))}
    persona = personas.get(a.persona.lower())
    if persona is None:
        log(f"페르소나 없음: {a.persona} (가능: {sorted(personas)})")
        print(json.dumps({"error": f"unknown persona {a.persona}", "candidates": []}, ensure_ascii=False))
        return 1

    conn = psycopg2.connect(a.dsn)
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        rows, cols = fetch_jobs(cur, a.overfetch)
    log(f"[db] 후보 원본 {len(rows)}건")

    if not rows:
        conn.close()
        print(json.dumps({"persona": persona, "candidates": []}, ensure_ascii=False))
        return 0

    embedder = build_embedder()

    # 캐시된 임베딩 재사용, 없는 것만 계산
    cached = {i: parse_vec(r.get("embedding")) for i, r in enumerate(rows)}
    todo = [i for i, v in cached.items() if v is None]
    if todo:
        log(f"[embed] 신규 계산 {len(todo)}건 / 캐시 {len(rows) - len(todo)}건")
        vecs_new = embedder.embed([job_text(rows[i]) for i in todo])
        for k, i in enumerate(todo):
            cached[i] = vecs_new[k]

    mat = np.vstack([cached[i] for i in range(len(rows))])
    qv = embedder.embed([persona_text(persona)])[0]
    cos = mat @ qv  # 정규화된 벡터의 내적 = 코사인 유사도

    # 임베딩 캐시 저장 (embedding 컬럼이 있을 때만)
    if todo and "embedding" in cols:
        try:
            with conn.cursor() as wcur:
                for i in todo:
                    wcur.execute(
                        "UPDATE job_postings SET embedding=%s WHERE id=%s",
                        (vec_literal(cached[i]), rows[i]["id"]),
                    )
            conn.commit()
            log(f"[db] 임베딩 캐시 저장 {len(todo)}건")
        except Exception as e:
            conn.rollback()
            log(f"[db] 임베딩 캐시 저장 실패(무시): {e}")
    conn.close()

    pskills = {s.lower() for s in (persona.get("skills") or [])}
    prefs = persona.get("company_size_pref") or []
    locs = set(persona.get("locations") or [])
    yrs = persona.get("career_years")
    excl = [e.lower() for e in (persona.get("exclude") or persona.get("excludes") or [])]
    remote_ok = bool(persona.get("remote_ok"))

    out = []
    for i, r in enumerate(rows):
        # 하드필터 (정보가 없으면 통과 — 데이터 결손으로 0건 되는 것 방지)
        if locs and r.get("location_city"):
            if r["location_city"] not in locs and not (remote_ok and r.get("is_remote")):
                continue
        if yrs is not None and r.get("career_min") is not None and r["career_min"] > yrs:
            continue
        blob = job_text(r).lower()
        if excl and any(e in blob for e in excl):
            continue

        tags = {t.lower() for t in (r.get("skill_tags") or [])}
        overlap = len(tags & pskills)
        c = float(cos[i])
        final = c + 0.15 * (overlap / max(1, len(pskills))) + 0.05 * (1.0 if size_ok(r.get("company_type"), prefs) else 0.0)
        out.append({
            "external_id": str(r.get("external_id") or r["id"]),
            "position": r.get("position") or "",
            "company": r.get("name") or "",
            "company_type": r.get("company_type"),
            "employee_count": r.get("employee_count"),
            "location_full": r.get("location_full"),
            "source_url": r.get("source_url"),
            "skill_tags": r.get("skill_tags") or [],
            "score_cosine": round(c, 4),
            "skill_overlap": overlap,
            "score_final": round(final, 4),
        })

    out.sort(key=lambda x: x["score_final"], reverse=True)
    log(f"[result] 필터 통과 {len(out)}건 → 상위 {min(a.topn, len(out))}건 반환 (embedder={embedder.name})")

    print(json.dumps(
        {"persona": persona, "embedder": embedder.name, "candidates": out[:a.topn]},
        ensure_ascii=False,
    ))
    return 0


if __name__ == "__main__":
    sys.exit(main())
