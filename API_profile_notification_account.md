# 이력서·자소서 / 알림 설정 / 계정 정보 API

공통: 아래 API는 전부 로그인 필요합니다. 모든 요청에 아래 헤더를 포함해주세요.

```
Authorization: Bearer {accessToken}
```

---

## 1. 이력서·자소서 S3 업로드용 Presigned URL 발급

`POST /api/profile/files/presigned-url`

파일 자체를 BE로 보내는 게 아니라, **S3에 직접 업로드할 수 있는 URL을 먼저 발급받는 방식**입니다. 이 API → S3에 PUT 업로드 → 아래 "2. 이력서·자소서 생성"으로 업로드 확정, 순서로 이어집니다.

### Header

```
Content-Type: application/json
Authorization: Bearer {accessToken}
```

### Request

```json
{
  "category": "RESUME",
  "fileName": "이력서.pdf"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| category | string (enum) | O | `RESUME` \| `COVER_LETTER` |
| fileName | string | O | 확장자 포함 원본 파일명. 허용 확장자: `pdf`, `docx`, `hwp`, `hwpx` |

### Query String

```
없음
```

### Response

```json
HTTP 200 OK

{
  "isSuccess": true,
  "code": "RESUME_200_1",
  "message": "업로드용 Presigned URL을 발급했습니다.",
  "data": {
    "presignedUrl": "https://s3.amazonaws.com/...",
    "objectKey": "profile/1/RESUME",
    "expiresIn": 300
  }
}
```

FE는 `presignedUrl`로 파일 바이너리를 **HTTP PUT** 요청해서 S3에 직접 업로드합니다. 업로드 성공 후 `objectKey`를 아래 "2. 이력서·자소서 생성" 요청에 그대로 넣어주세요.

**잘못된 확장자로 요청 시:**

```json
HTTP 400 Bad Request

{
  "isSuccess": false,
  "code": "RESUME_400_1",
  "message": "지원하지 않는 파일 형식입니다. (PDF, DOCX, HWP, HWPX만 업로드 가능합니다)",
  "data": null
}
```

---

## 2. 이력서·자소서 생성 (업로드 확정)

`POST /api/profile/files`

S3 업로드가 끝난 뒤 이 API로 확정해야 DB에 저장됩니다. **`category`당 1개 슬롯만 유지**되며, 같은 카테고리로 다시 호출하면 기존 파일을 덮어씁니다(교체).

### Header

```
Content-Type: application/json
Authorization: Bearer {accessToken}
```

### Request

```json
{
  "objectKey": "profile/1/RESUME",
  "fileName": "이력서.pdf",
  "category": "RESUME"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| objectKey | string | O | 1번 API 응답의 `objectKey` 그대로 |
| fileName | string | O | 원본 파일명 |
| category | string (enum) | O | `RESUME` \| `COVER_LETTER` (1번 요청과 동일해야 함) |

### Query String

```
없음
```

### Response

**신규 등록 (해당 카테고리에 파일이 없던 경우):**

```json
HTTP 201 Created

{
  "isSuccess": true,
  "code": "RESUME_201_1",
  "message": "이력서/자소서 파일을 저장했습니다.",
  "data": {
    "fileId": 1,
    "created": true
  }
}
```

**교체 (해당 카테고리에 이미 파일이 있던 경우):**

```json
HTTP 200 OK

{
  "isSuccess": true,
  "code": "RESUME_200_4",
  "message": "이력서/자소서 파일을 갱신했습니다.",
  "data": {
    "fileId": 1,
    "created": false
  }
}
```

**에러 케이스**

| code | HTTP | 상황 |
| --- | --- | --- |
| `RESUME_400_2` | 400 | `objectKey`가 1번에서 발급받은 값과 다름 |
| `RESUME_400_3` | 400 | S3에 실제 업로드된 파일이 없음 (1번 → S3 PUT 없이 바로 호출한 경우) |
| `RESUME_413_1` | 413 | 파일 용량이 10MB 초과 |

---

## 3. 이력서·자소서 조회

`GET /api/profile/files`

내가 업로드한 파일 목록(최대 2개: RESUME 1개 + COVER_LETTER 1개)을 조회합니다.

### Header

```
Authorization: Bearer {accessToken}
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
  "code": "RESUME_200_2",
  "message": "이력서/자소서 파일 목록을 조회했습니다.",
  "data": [
    {
      "fileId": 1,
      "category": "RESUME",
      "fileName": "이력서.pdf",
      "fileFormat": "PDF",
      "uploadedAt": "2026-07-28T10:00:00"
    }
  ]
}
```

파일이 하나도 없으면 `data`는 빈 배열 `[]`입니다.

---

## 4. 이력서·자소서 삭제

`DELETE /api/profile/files/{fileId}`

### Path Variable

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| fileId | number | 삭제할 파일 ID (조회 응답의 `fileId`) |

### Header

```
Authorization: Bearer {accessToken}
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
  "code": "RESUME_200_3",
  "message": "이력서/자소서 파일을 삭제했습니다."
}
```

**존재하지 않거나 내 파일이 아닌 fileId로 요청 시:**

```json
HTTP 404 Not Found

{
  "isSuccess": false,
  "code": "RESUME_404_1",
  "message": "이력서/자소서 파일을 찾을 수 없습니다.",
  "data": null
}
```

---

## 5. 알림 수신 설정 조회

`GET /api/settings/notification`

설정이 없던 유저(최초 조회)면 기본값(07:30 발송, 브리핑 on, 마케팅 off)으로 자동 생성한 뒤 반환합니다.

### Header

```
Authorization: Bearer {accessToken}
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
  "code": "NOTIFICATION_200_1",
  "message": "알림 수신 설정을 조회했습니다.",
  "data": {
    "briefingEnabled": true,
    "sendSlot": "07:30",
    "marketingSubscribed": false,
    "snooze": {
      "snoozed": false,
      "until": null,
      "indefinite": false
    }
  }
}
```

`snooze.snoozed`가 `true`면 현재 스누즈 중이라는 뜻이고, `until`은 재개 예정일(무기한이면 `null` + `indefinite: true`)입니다.

---

## 6. 알림 수신 설정 변경

`PATCH /api/settings/notification`

전달한 필드만 부분 수정됩니다(나머지는 기존 값 유지). 설정이 없던 유저면 기본값으로 자동 생성 후 수정됩니다.

### Header

```
Content-Type: application/json
Authorization: Bearer {accessToken}
```

### Request

```json
{
  "briefingEnabled": true,
  "sendSlot": "07:30",
  "marketingSubscribed": false
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| briefingEnabled | boolean | X | "오늘의 레터" 수신 여부 |
| sendSlot | string | X | `07:30` \| `12:30` \| `18:30` 중 하나 |
| marketingSubscribed | boolean | X | 마케팅 수신 동의 여부 |

세 필드 다 optional이라, 하나만 바꾸고 싶으면 그 필드만 보내면 됩니다.

### Query String

```
없음
```

### Response

```json
HTTP 200 OK

{
  "isSuccess": true,
  "code": "NOTIFICATION_200_2",
  "message": "알림 수신 설정을 변경했습니다.",
  "data": {
    "briefingEnabled": true,
    "sendSlot": "07:30",
    "marketingSubscribed": false,
    "snooze": {
      "snoozed": false,
      "until": null,
      "indefinite": false
    }
  }
}
```

**`sendSlot`에 허용되지 않은 값(위 3개 외)을 보내면:**

```json
HTTP 400 Bad Request

{
  "isSuccess": false,
  "code": "NOTIFICATION_400_1",
  "message": "허용되지 않는 발송 시간대입니다.",
  "data": null
}
```

---

## 7. 알림 발송 스누즈 설정

`POST /api/settings/notification/snooze`

일정 기간 "오늘의 레터" 발송을 잠시 멈춥니다.

### Header

```
Content-Type: application/json
Authorization: Bearer {accessToken}
```

### Request

```json
{
  "duration": "SEVEN_DAYS"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| duration | string (enum) | O | `SEVEN_DAYS`(7일) \| `THIRTY_DAYS`(30일) \| `INDEFINITE`(무기한) |

### Query String

```
없음
```

### Response

```json
HTTP 200 OK

{
  "isSuccess": true,
  "code": "NOTIFICATION_200_3",
  "message": "알림 발송을 스누즈 설정했습니다."
}
```

**`duration` 누락 시:**

```json
HTTP 400 Bad Request

{
  "isSuccess": false,
  "code": "NOTIFICATION_400_2",
  "message": "스누즈 기간을 선택해주세요.",
  "data": null
}
```

---

## 8. 스누즈 해제

`DELETE /api/settings/notification/snooze`

### Header

```
Authorization: Bearer {accessToken}
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
  "code": "NOTIFICATION_200_4",
  "message": "알림 스누즈를 해제했습니다."
}
```

---

## 9. 계정 정보 조회

⚠️ **주의**: 전달주신 스펙엔 `GET /api/settings/account`로 돼 있는데, 실제 구현된 경로는 `GET /api/auth/account`입니다. 스펙 문서가 갱신이 안 된 건지, 경로가 바뀐 건지 확인 부탁드려요. 아래는 실제 구현 기준으로 작성했습니다.

`GET /api/auth/account`

### Header

```
Authorization: Bearer {accessToken}
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
  "code": "ACCOUNT_200_1",
  "message": "계정 정보를 조회했습니다.",
  "data": {
    "socialType": "KAKAO",
    "joinedAt": "2026-01-15T09:00:00",
    "receivingEmail": "user@example.com",
    "emailVerified": false
  }
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| socialType | string (enum) | `GOOGLE` \| `KAKAO` |
| joinedAt | string (datetime) | 가입일 |
| receivingEmail | string | 수신 이메일 (현재는 로그인 이메일과 동일) |
| emailVerified | boolean | 이메일 인증 여부 (이슈 #59, 아직 미구현이라 항상 `false`) |
