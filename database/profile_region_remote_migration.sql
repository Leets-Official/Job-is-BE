-- ============================================================
-- 마이그레이션 — 희망 지역 다중 선택 + 원격 포함 토글
--
-- 근거: screen-spec/60-profile.md PRO-01 필드 표
--   · 희망 지역 = 다중 선택 칩 1~3개 (기존 스키마는 사용자당 1개만 허용)
--   · 원격 포함 토글 = 지역과 별개 필드 (기존 스키마에 없음)
--         03-matching-contract §4.1 에서 하드필터 입력(도시 일치 OR 원격)으로 쓰인다
--
-- 왜 수동 실행이 필요한가:
--   ddl-auto: update 는 컬럼·테이블을 "추가"만 하고 기존 제약을 지우지 않는다.
--   uk_user_regions_user(user_id 단독 UNIQUE)가 남아 있으면 두 번째 지역 INSERT 가
--   무결성 위반으로 실패한다. remote_ok 도 기존 행이 있으면 NOT NULL 추가가 막히므로
--   기본값과 함께 먼저 넣어 둔다.
--
-- 실행 시점: 애플리케이션을 내리거나 배포 직전에 1회. 그 뒤 앱을 띄우면
--            ddl-auto 가 이미 반영된 상태를 보고 아무 것도 하지 않는다.
--
-- 실행: psql -h <host> -p <port> -U <user> -d <db> -f database/profile_region_remote_migration.sql
--
-- 재실행 안전: IF EXISTS / IF NOT EXISTS 로 감쌌다.
-- ============================================================

-- 이 파일은 UTF-8 이다. Windows cmd 의 psql 은 기본 클라이언트 인코딩이
-- UHC(CP949)라 한글이 깨지므로 세션 인코딩을 먼저 지정한다.
SET client_encoding TO 'UTF8';

BEGIN;

-- ------------------------------------------------------------
-- 1) user_regions — 사용자당 1개 제한 해제, (user_id, region_id) 중복만 차단
--
-- 기존 제약은 "한 사용자에 한 행"이라 다중 지역이 애초에 불가능했다.
-- 새 제약은 "같은 지역을 두 번 고르는 것"만 막는다. 개수 상한(3개)은
-- ProfileService.MAX_REGIONS 에서 검증한다(스키마로 강제하지 않음).
-- ------------------------------------------------------------
ALTER TABLE user_regions DROP CONSTRAINT IF EXISTS uk_user_regions_user;

-- 혹시 남아 있을 (user_id, region_id) 중복 행 정리 — 새 제약 생성 전 선행 필요.
-- 같은 조합이 여러 건이면 id 가 가장 작은 행만 남긴다.
DELETE FROM user_regions a
    USING user_regions b
WHERE a.user_id = b.user_id
  AND a.region_id = b.region_id
  AND a.id > b.id;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_user_regions_user_region'
    ) THEN
        ALTER TABLE user_regions
            ADD CONSTRAINT uk_user_regions_user_region UNIQUE (user_id, region_id);
    END IF;
END $$;

-- ------------------------------------------------------------
-- 2) user_profiles — 원격 포함 토글 추가
--
-- 기존 행은 false(원격 비희망)로 채운다. 온보딩을 이미 마친 사용자의
-- 추천 결과를 바꾸지 않는 쪽이 안전하다(원격을 켜면 후보가 넓어진다).
-- ------------------------------------------------------------
ALTER TABLE user_profiles
    ADD COLUMN IF NOT EXISTS remote_ok boolean NOT NULL DEFAULT false;

COMMIT;

-- ------------------------------------------------------------
-- 확인용 (실행 후 눈으로 검증)
-- ------------------------------------------------------------
-- 기대: uk_user_regions_user 없음 / uk_user_regions_user_region 있음
-- SELECT conname FROM pg_constraint
--  WHERE conrelid = 'user_regions'::regclass AND contype = 'u';
--
-- 기대: remote_ok / boolean / NOT NULL / default false
-- SELECT column_name, data_type, is_nullable, column_default
--   FROM information_schema.columns
--  WHERE table_name = 'user_profiles' AND column_name = 'remote_ok';
--
-- 기대: 0건 (사용자당 지역 중복 없음)
-- SELECT user_id, region_id, count(*) FROM user_regions
--  GROUP BY 1, 2 HAVING count(*) > 1;
