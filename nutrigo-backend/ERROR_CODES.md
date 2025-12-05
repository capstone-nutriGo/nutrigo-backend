# Error code reference

이 문서는 Nutrigo 백엔드에서 반환하는 에러 코드, 메시지, HTTP 상태 및 발생 조건을 정리합니다. 모든 응답은 `ApiResponse` 포맷으로 내려가며 실패 시 `errorCode`와 `message`가 포함됩니다.

- 성공: `ApiResponse.success(data)` → `{ success: true, data, message: null, errorCode: null }`
- 실패: `ApiResponse.fail(errorCode, message)` → `{ success: false, data: null, message, errorCode }`

## 도메인 예외 (AppException 기반)
각 도메인 서비스에서 발생시키는 예외는 `AppExceptions` 하위 클래스로 정의되며, 예외별 고유 코드와 HTTP 상태를 가집니다.

| 코드 | HTTP 상태 | 메시지 | 발생 조건 |
| --- | --- | --- | --- |
| `AUTH_001` | 404 Not Found | `User not found` | 로그인 등에서 이메일로 사용자를 찾지 못했을 때 |
| `AUTH_002` | 401 Unauthorized | `Invalid credentials` | 로그인 시 비밀번호가 틀렸거나 인증 실패 시 |
| `AUTH_003` | 400 Bad Request | `Email already registered` | 회원 가입 시 이미 등록된 이메일로 요청할 때 |
| `USER_001` | 404 Not Found | `User not found` | 사용자 조회 시 대상 사용자가 없을 때 |
| `CHALLENGE_001` | 404 Not Found | `Challenge not found` | 챌린지 조회 시 대상 챌린지가 없을 때 |
| `INSIGHT_001` | 404 Not Found | `Analysis session not found: {id}` | 인사이트 분석 세션 ID로 조회 실패 시 |
| `INSIGHT_002` | 400 Bad Request | `Invalid report range: {range}` | 지원하지 않는 리포트 범위를 요청할 때 |

## 검증 오류 코드 (VALIDATION_*)
입력 검증 실패(`@Valid`) 및 제약 조건 위반 시, 검증 메시지에 따라 아래 `VALIDATION_*` 코드가 매핑됩니다. 목록에 없는 메시지는 기본값 `COMMON_001`(검증 오류) 또는 `COMMON_002`(제약 조건 위반)로 내려갑니다. HTTP 상태는 모두 400 Bad Request입니다.

| 코드 | 메시지 |
| --- | --- |
| `VALIDATION_001` | `이메일을 입력해주세요` |
| `VALIDATION_002` | `이메일 형식이 올바르지 않습니다` |
| `VALIDATION_003` | `비밀번호를 입력해주세요` |
| `VALIDATION_004` | `닉네임을 입력해주세요` |
| `VALIDATION_005` | `이름을 입력해주세요` |
| `VALIDATION_006` | `생년월일은 과거 또는 오늘 날짜여야 합니다` |
| `VALIDATION_007` | `소셜 로그인 제공자를 선택해주세요` |
| `VALIDATION_008` | `리프레시 토큰을 입력해주세요` |
| `VALIDATION_009` | `챌린지 제목을 입력해주세요` |
| `VALIDATION_010` | `챌린지 카테고리를 선택해주세요` |
| `VALIDATION_011` | `챌린지 유형을 선택해주세요` |
| `VALIDATION_012` | `챌린지 기간을 입력해주세요` |
| `VALIDATION_013` | `기간은 1일 이상이어야 합니다` |
| `VALIDATION_014` | `목표 횟수는 1 이상이어야 합니다` |
| `VALIDATION_015` | `최대 칼로리는 1 이상이어야 합니다` |
| `VALIDATION_016` | `최대 나트륨은 1 이상이어야 합니다` |
| `VALIDATION_017` | `닉네임은 1자 이상 50자 이하여야 합니다` |
| `VALIDATION_018` | `이름은 1자 이상 50자 이하여야 합니다` |
| `VALIDATION_019` | `주소는 255자 이하여야 합니다` |

## 공통 예외 코드 (COMMON_*)
프레임워크/전역 예외는 `GlobalExceptionHandler`에서 처리하며, 상황별 `COMMON_*` 코드와 HTTP 상태를 부여합니다.

| 코드 | HTTP 상태 | 설명 |
| --- | --- | --- |
| `COMMON_001` | 400 Bad Request | 검증 오류 기본 코드 (목록에 없는 `@Valid` 메시지) |
| `COMMON_002` | 400 Bad Request | 제약 조건 위반 기본 코드 (`ConstraintViolationException`) |
| `COMMON_003` | 400 Bad Request | 요청 본문 파싱 실패 또는 필수 파라미터 누락 |
| `COMMON_004` | 400 Bad Request | 비즈니스 로직에서 던진 `IllegalArgumentException` |
| `COMMON_005` | 405 Method Not Allowed | 지원하지 않는 HTTP 메서드 |
| `COMMON_006` | 409 Conflict | `IllegalStateException` 등 충돌 상황 |
| `COMMON_007` | 409 Conflict | 데이터 무결성 위반 (`DataIntegrityViolationException`) |
| `COMMON_999` | 500 Internal Server Error | 예기치 못한 서버 오류 (전역 캐치올) |

## 로그인 인증 실패 처리
`BadCredentialsException` 발생 시에도 `AUTH_002`(Invalid credentials)와 동일한 응답을 내려 Spring Security 인증 실패가 기존 인증 오류 스키마와 일관되게 처리됩니다.