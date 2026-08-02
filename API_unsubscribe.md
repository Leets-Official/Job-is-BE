# 수신거부 / 재구독 / 해지 사유 API (#60)

공통: 로그인(Authorization 헤더) 불필요. 이메일에 포함된 `token` 하나로 인증합니다.
토큰이 유효하지 않으면 세 API 모두 아래처럼 공통으로 404를 반환합니다.

```json
HTTP 404 Not Found

{
  "isSuccess": false,
  "code": "UNSUBSCRIBE_404_1",
  "message": "유효하지 않은 수신거부 링크입니다.",
  "data": null
}
```

---

## 1. 수신거부 처리

`GET /api/unsubscribe/{token}`

원클릭 해지입니다. 진입(호출) 즉시 처리됩니다. 이미 해지된 토큰으로 다시 호출해도 에러 없이 같은 결과(`emailSubscribed: false`)를 반환합니다(멱등).

### Path Variable

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| token | string | 이메일 링크에 포함된 수신거부 토큰 |

### Header

```
없음 (Authorization 불필요)
```

### Request

```
없음 (body 없음)
```

### Query String

```
없음
```

### Response

```json
HTTP 200 OK

{
  "isSuccess": true,
  "code": "UNSUBSCRIBE_200_1",
  "message": "수신거부가 처리되었습니다.",
  "data": {
    "emailSubscribed": false
  }
}
```

---

## 2. 재구독

`POST /api/unsubscribe/{token}/resubscribe`

해지했던 걸 다시 구독 상태로 되돌립니다. 이미 구독 중인 토큰으로 호출해도 에러 없이 같은 결과(`emailSubscribed: true`)를 반환합니다(멱등).

### Path Variable

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| token | string | 이메일 링크에 포함된 수신거부 토큰 (해지 시와 동일한 토큰, 재발급되지 않음) |

### Header

```
없음 (Authorization 불필요)
```

### Request

```
없음 (body 없음)
```

### Query String

```
없음
```

### Response

```json
HTTP 200 OK

{
  "isSuccess": true,
  "code": "UNSUBSCRIBE_200_2",
  "message": "다시 구독되었습니다.",
  "data": {
    "emailSubscribed": true
  }
}
```

---

## 3. 해지 사유 제출

`POST /api/unsubscribe/{token}/feedback`

수신거부 랜딩페이지에서 사유를 선택해 제출할 때 호출합니다. **전체 선택 사항**이라, 사용자가 사유 없이 그냥 나가면 이 API를 호출하지 않아도 됩니다 (건너뛰기 = 호출 안 함).

### Path Variable

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| token | string | 이메일 링크에 포함된 수신거부 토큰 |

### Header

```
Content-Type: application/json
```

### Request

```json
{
  "reason": "TOO_FREQUENT",
  "comment": null
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| reason | string (enum) | O | 아래 4개 값 중 하나 |
| comment | string (max 200자) | X | `reason`이 `OTHER`일 때만 사용하는 자유 입력. 그 외엔 무시됨 |

`reason` enum 값:
- `NOT_RELEVANT` — 관련 공고가 없어요
- `TOO_FREQUENT` — 메일이 너무 자주 와요
- `NOT_USING_SERVICE` — 서비스를 더 이상 사용하지 않아요
- `OTHER` — 기타 (comment에 자유 입력)

### Query String

```
없음
```

### Response

```json
HTTP 200 OK

{
  "isSuccess": true,
  "code": "UNSUBSCRIBE_200_3",
  "message": "해지 사유가 제출되었습니다."
}
```

**`reason` 누락/잘못된 값으로 호출 시:**

```json
HTTP 400 Bad Request

{
  "isSuccess": false,
  "code": "COMM_400",
  "message": "잘못된 요청입니다.",
  "data": null
}
```
