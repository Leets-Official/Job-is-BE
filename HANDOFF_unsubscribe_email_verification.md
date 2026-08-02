# 작업 인계 노트 (Job-is-BE / 수신거부·이메일인증 API)

다른 Claude 세션에서 이어서 작업할 때 참고할 요약입니다. 프로젝트: `Job-is-BE` (Spring Boot, `com.leets7th.job_is_be`). 리포 컨벤션은 `/Users/syk98/Documents/Job-is-BE/CLAUDE.md` 참고 (요약: 브랜치 `타입/#이슈번호`, 커밋 메시지 `[Feat]/[Fix]/[Test]/[Chore] #이슈번호 설명`, 응답 포맷 `ApiResponse` + `SuccessStatus`/`ErrorStatus` enum, `GeneralException(BaseStatus)` 던지는 방식, entity는 `BaseEntity` 상속 + `@Builder` + 상태 전이는 인스턴스 메서드로).

## 완료된 작업

### 이슈 #60 — 수신거부/재구독/해지 사유 API (`feat/#60`, push 완료, PR 준비 완료)

3개 엔드포인트 구현·테스트·push 완료:

- `GET /api/unsubscribe/{token}` — 토큰 기반 원클릭 해지 처리
- `POST /api/unsubscribe/{token}/resubscribe` — 재구독 처리
- `POST /api/unsubscribe/{token}/feedback` — 해지 사유 제출

파일:
- `domain/notification/entity/NotificationSetting.java` — `unsubscribeToken` 컬럼 추가 (생성자에서 SecureRandom 32바이트 Base64url로 발급, 유니크 제약)
- `domain/notification/repository/NotificationSettingRepository.java` — `findByUnsubscribeToken` 추가
- `domain/notification/enums/UnsubscribeReason.java` (신규) — `NOT_RELEVANT`/`TOO_FREQUENT`/`NOT_USING_SERVICE`/`OTHER`
- `domain/notification/entity/UnsubscribeFeedback.java` (신규), `domain/notification/repository/UnsubscribeFeedbackRepository.java` (신규)
- `domain/notification/dto/UnsubscribeResponse.java`, `UnsubscribeFeedbackRequest.java` (신규)
- `domain/notification/service/UnsubscribeService.java`, `domain/notification/controller/UnsubscribeController.java` (신규)
- `global/status/ErrorStatus.java` — `UNSUBSCRIBE_TOKEN_INVALID` 추가
- `global/status/SuccessStatus.java` — `UNSUBSCRIBE_SUCCESS`/`RESUBSCRIBE_SUCCESS`/`UNSUBSCRIBE_FEEDBACK_SUCCESS` 추가
- `global/config/SecurityConfig.java` — `/api/unsubscribe/**` permitAll 추가
- 테스트: `domain/notification/service/UnsubscribeServiceTest.java` (5케이스, 토큰 무효/해지/멱등/재구독/사유제출)

**핵심 설계 결정**
- 로그인 불필요: 이메일 링크의 토큰 자체가 인증 수단 (JWT와 별개). "원클릭 해지"라는 기획 의도상 로그인 세션 없는 기기에서도 동작해야 해서 이렇게 결정함.
- 토큰은 회전(재발급)하지 않음: 해지↔재구독을 반복해도 같은 이메일 링크가 계속 유효해야 하기 때문.
- GET 해지 처리는 멱등: 이미 해지된 토큰으로 재요청해도 에러 없이 동일 상태 반환 (스팸필터의 링크 사전 스캔 등으로 중복 클릭될 수 있어서).
- 해지/재구독은 기존 `NotificationSetting.emailSubscribed` 필드로 제어 (이 랜딩페이지가 "오늘의 레터" 자체를 껐다 켜는 거라 `marketingSubscribed`와는 무관, 건드리지 않음).
- 사유 제출은 `UnsubscribeReason` enum + `OTHER` 선택 시 자유 comment, 전체 선택사항(스킵 가능).

**알려진 이슈 / 리뷰 코멘트 (보류 중)**
- `unsubscribe_token` 컬럼이 `NOT NULL + UNIQUE`인데, 프로젝트에 Flyway/Liquibase가 없고 `ddl-auto: update`에만 의존해서, 기존 row가 있는 DB에서 컬럼 추가 시 조용히 실패할 수 있음 (로컬에서 실제로 겪음 → `DROP TABLE notification_settings` 후 재기동으로 해결).
- PR 리뷰에서 "nullable 추가 → 백필 → NOT NULL/UNIQUE 적용" 단계별 안전 마이그레이션을 요구함. **사용자가 이번 PR에서는 수정하지 않고 그대로 머지하기로 결정, 추후 리팩토링 단계에서 다루기로 함.**
- 이때 Flyway 도입을 추천했음 (Spring Boot에 거의 설정 없이 붙고, Hibernate ddl-auto보다 먼저 실행되어 이런 컬럼 추가/백필 순서를 SQL 마이그레이션 파일 하나로 안전하게 자동화 가능. 지금이 마이그레이션 히스토리가 짧아 도입 비용이 가장 낮은 시점이라는 게 추천 이유). 트레이드오프: 엔티티 변경할 때마다 매칭되는 마이그레이션 SQL을 같이 작성해야 하는 규율 필요.

## 보류 중 (착수 안 함)

### 이슈 #59 — 이메일 확인 메일 발송/재발송 + 링크 검증 API

브랜치 `feat/#59`는 develop에서 파놨지만 **코드는 전혀 작성 안 함**. 착수 전에 아래 논의 사항에 사용자가 막혀서 "보류"로 결론남:

- `POST /api/auth/email-verification` — 이메일 확인 메일 발송/재발송
- `GET /api/auth/email-verification/confirm` — 이메일 확인 링크 검증

**미해결 논의 사항 (다음에 착수하려면 이것부터 정해야 함)**
1. **검증 대상 이메일이 불명확함.** `KakaoOAuthClient`/`GoogleOAuthClient`가 로그인 시점에 이미 소셜 제공자의 `email_verified`를 검증하고 있어서, 로그인 이메일(`User.email`)은 가입 시점에 이미 검증된 상태로 들어옴. `User.email`엔 수정 메서드(setter)도 없어서 "받는 이메일 변경" 기능 자체가 없음. 그런데 `NotificationSetting.emailVerified`는 기본값 `false`로 시작함. 이 API가 (a) 아직 없는 "수신 이메일 변경" 기능의 선행 작업인지, (b) 그냥 `User.email` 그대로 두고 `NotificationSetting.emailVerified`를 형식적으로 `true`로 바꾸는 절차인지 확인 필요.
2. **메일 발송 인프라가 전혀 없음.** `build.gradle`에 JavaMailSender/SES 등 메일 관련 의존성 없음. 이 이슈 범위가 토큰 발급+검증 API까지만인지, 실제 메일 전송(SMTP/SES 연동)까지 포함인지 확인 필요.
3. **토큰 설계는 #60과 정반대여야 함.** #60의 unsubscribe 토큰은 "회전 없음, 장기 유효"였지만, 이메일 인증 토큰은 보안 목적(메일함 소유 증명)이라 짧은 만료시간 + 1회성이 맞음. `domain/auth/oauth/OAuthLoginCodeStore.java`가 이미 Redis + TTL + 1회 consume 패턴을 갖고 있어서 그대로 재사용 가능할 듯. 만료시간(예: 30분? 24시간?)과 재발송 쿨다운 정책 확인 필요.
4. `GET /api/auth/email-verification/confirm`은 스펙에 `{token}` 경로 변수가 없어서, 쿼리 파라미터(`?token=`)로 받는 걸로 가정했었음 — 확인 필요.
5. `POST /api/auth/email-verification`(발송/재발송)은 "내 계정"에 보내는 거라 JWT 인증이 걸려야 할 것 같은데(반대로 confirm/unsubscribe류는 비로그인), 확인 필요.

## 알아두면 좋은 환경/트러블슈팅 메모

- 로컬 서버 포트는 **8081** (`application-dev.yml`의 `server.port: ${SERVER_PORT:8081}`).
- 로컬 DB: Postgres, `localhost:5432/jobisbe`, user `postgres`(기본 비번 `1234`).
- `POST /api/auth/dev/login`(dev 프로필 전용, `{"userId": N}`)으로 스웨거 테스트용 JWT 발급 — DB에 해당 userId row가 미리 있어야 함.
- Swagger UI: `http://localhost:8081/swagger-ui.html`.
- **`.git/index.lock`이 반복적으로 안 지워지고 남는 이슈가 있음** (Cowork 세션이 마운트하는 FS의 unlink 제약 때문으로 추정, 사용자의 실제 맥 터미널에서도 동일 증상 재현됨 — 같은 물리 폴더라 그런 듯). `git checkout`/`git stash` 등이 "Unable to create index.lock: File exists"로 막히면 진행 중인 다른 git 프로세스가 없는 게 확실한 한 `rm -f .git/index.lock` 하고 재시도하면 됨.
- 이 프로젝트는 **Flyway/Liquibase 없이 `ddl-auto: update`에만 의존**. NOT NULL/UNIQUE 컬럼을 기존 테이블에 추가할 때 실패할 수 있음 — 지금은 로컬 DB 테이블 드롭 후 재기동으로 회피 중.
- **사용자가 명시적으로 요청함: git push/pull 등 원격 관련 명령은 사용자가 직접 실행. Claude는 코드 작성 + 실행할 명령어 안내만 할 것.**

## 다음에 할 일 후보

1. 이슈 #60 PR 리뷰 대응, 머지되면 `develop` 최신화.
2. 이슈 #59 착수 여부 결정 — 위 5가지 미해결 논의 사항부터 정리.
3. (리팩토링 단계) Flyway 도입 검토 — 첫 마이그레이션으로 `unsubscribe_token` 백필까지 같이 정리.
