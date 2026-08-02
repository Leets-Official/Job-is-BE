## 1. 이력서·자소서 S3 업로드 (Presigned URL 발급)

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

**⚠️ PUT 요청 시 Content-Type 헤더 필수 (중요)**

서버가 presigned URL을 발급할 때 클라이언트가 보낸 값이 아니라, 파일 확장자 기준으로 서버가 정한 Content-Type으로 서명합니다. S3에 PUT 요청 보낼 때 **이 값과 정확히 일치하는 `Content-Type` 헤더**를 넣어야 합니다. 다르면(또는 브라우저 자동 지정 값 사용 시) 서명 불일치로 업로드가 실패합니다.

| 확장자 | PUT 요청 시 Content-Type |
| --- | --- |
| pdf | `application/pdf` |
| docx | `application/vnd.openxmlformats-officedocument.wordprocessingml.document` |
| hwp | `application/x-hwp` |
| hwpx | `application/haansofthwpx` |

또한 이 PUT 요청에는 쿠키나 `Authorization` 헤더를 넣지 마세요. S3 인증은 URL 안의 서명 쿼리스트링으로 이미 처리되며, 우리 서비스의 인증정보는 S3가 이해하지 못합니다.

**잘못된 확장자로 요청 시:**
```json
HTTP 400 Bad Request
{
  "isSuccess": false,
  "code": "RESUME_400_1",
  "message": "지원하지 않는 파일 형식입니다. (PDF, DOCX, HWP, HWPX만 업로드 가능합니다)"
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

## 3. 이력서·자소서 목록 조회

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
  "message": "이력서/자소서 파일을 찾을 수 없습니다."
}
```

---

## 5. 이력서·자소서 다운로드 URL 발급

`GET /api/profile/files/{fileId}/download-url`

업로드된 파일을 다운로드할 수 있는 Presigned URL을 발급합니다. 본인 소유 파일만 발급 가능합니다.

### Path Variable
| 이름 | 타입 | 설명 |
| --- | --- | --- |
| fileId | number | 다운로드할 파일 ID (조회 응답의 `fileId`) |

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
  "code": "RESUME_200_5",
  "message": "다운로드용 Presigned URL을 발급했습니다.",
  "data": {
    "downloadUrl": "https://s3.amazonaws.com/...",
    "fileName": "이력서.pdf",
    "expiresIn": 300
  }
}
```

FE는 `downloadUrl`을 새 창으로 열거나 `<a>` 태그 클릭으로 바로 다운로드시키면 됩니다. 이 URL 자체가 인증 역할을 하므로 별도 헤더가 필요 없습니다 (업로드 때 PUT presigned URL과 동일한 원리).

`expiresIn`(초)이 지나면 URL이 만료되니, 발급 즉시 사용하는 방식으로 붙여주세요. 미리 받아뒀다가 나중에 쓰는 방식은 피해주세요.

**존재하지 않거나 내 파일이 아닌 fileId로 요청 시:**
```json
HTTP 404 Not Found
{
  "isSuccess": false,
  "code": "RESUME_404_1",
  "message": "이력서/자소서 파일을 찾을 수 없습니다."
}
```
