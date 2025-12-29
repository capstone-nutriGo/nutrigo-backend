# nutriGo-backend

Spring Boot 기반 RESTful API 서버입니다. 사용자 인증, 영양 분석 요청 처리, 이미지 업로드 관리 등의 기능을 제공합니다.

---

## 준비물

- Java 21 이상
- MySQL 8.0 이상
- Gradle (또는 Gradle Wrapper 사용)

---

## 설치

### 1) 프로젝트 빌드

프로젝트 루트 디렉터리에서:

Windows:
```bash
gradlew.bat build
```

Linux/macOS:
```bash
./gradlew build
```

### 2) 데이터베이스 설정

MySQL에 데이터베이스를 생성합니다:

```sql
CREATE DATABASE nutrigo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'nutrigo_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON nutrigo.* TO 'nutrigo_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 환경 변수

`.env` 파일 또는 시스템 환경 변수에 아래 값을 설정합니다.

### 데이터베이스 설정
- `SPRING_DATASOURCE_URL`: MySQL 연결 URL (예: `jdbc:mysql://localhost:3306/nutrigo`)
- `SPRING_DATASOURCE_USERNAME`: 데이터베이스 사용자명
- `SPRING_DATASOURCE_PASSWORD`: 데이터베이스 비밀번호

### AWS S3 설정
- `AWS_S3_BUCKET` (필수): 업로드 대상 버킷명
- `AWS_S3_REGION`: S3 리전 (예: `ap-northeast-2`)
- `AWS_S3_ENDPOINT` (옵션): 커스텀 엔드포인트 사용 시
- `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` (옵션): IAM Role 사용 시 생략 가능
- `AWS_S3_URL_EXPIRATION_SECONDS` (옵션): presigned 유효시간(기본 900초)

### JWT 설정
- `JWT_SECRET`: JWT 토큰 서명에 사용할 비밀키
- `JWT_EXPIRATION`: 토큰 만료 시간 (밀리초, 기본 86400000 = 24시간)

### AI 서비스 연동
- `AI_SERVICE_URL`: AI 서비스 URL (예: `http://localhost:8000`)

---

## 실행

### 개발 모드

Windows:
```bash
gradlew.bat bootRun
```

Linux/macOS:
```bash
./gradlew bootRun
```

또는 IDE에서 `NutrigoBackendApplication.java`를 실행합니다.

기본 포트: 8080
Swagger 문서: http://localhost:8080/swagger-ui.html

---

## 주요 API 엔드포인트

### 인증
- `POST /api/v1/auth/signup`: 회원가입
- `POST /api/v1/auth/login`: 로그인
- `POST /api/v1/auth/refresh`: 토큰 갱신

### 영양 분석
- `POST /api/v1/nutrition/upload-url`: S3 업로드 URL 발급
- `POST /api/v1/nutrition/store-link`: 가게 링크 분석 요청
- `POST /api/v1/nutrition/cart-image`: 장바구니 이미지 분석
- `POST /api/v1/nutrition/order-image`: 주문 내역 이미지 분석

### 사용자
- `GET /api/v1/user/profile`: 사용자 프로필 조회
- `PUT /api/v1/user/profile`: 사용자 프로필 수정
- `GET /api/v1/user/goals`: 사용자 목표 조회
- `PUT /api/v1/user/goals`: 사용자 목표 설정

### 인사이트
- `GET /api/v1/insights/calendar`: 식습관 캘린더 데이터
- `GET /api/v1/insights/weekly`: 주간 인사이트 리포트

### 챌린지
- `GET /api/v1/challenge`: 챌린지 목록
- `POST /api/v1/challenge`: 챌린지 시작
- `GET /api/v1/challenge/{id}`: 챌린지 상세 정보
- `GET /api/v1/challenge/{id}/progress`: 챌린지 진행 상황

### NutriBot
- `POST /api/v1/nutribot/chat`: 챗봇 메시지 전송

---

## 이미지 업로드 → 분석 플로우 (S3 직접 업로드)

1) 클라이언트가 `POST /api/v1/nutrition/upload-url` 호출  
   - body 예: `{"type": "CART_IMAGE", "content_type": "image/jpeg"}`
   - 응답: `upload_url`, `key`, `expires_at`, `content_type`
2) 클라이언트는 presigned `upload_url` 로 이미지를 PUT 업로드.
3) 업로드 완료 후 기존 분석 API 호출 시 `image_url` 자리에 S3 키(또는 `s3://bucket/key`)를 전달.  
   - `/api/v1/nutrition/cart-image`  
   - `/api/v1/nutrition/order-image`
4) 파이썬 `nutrigo-ai` 는 S3에서 직접 이미지를 읽어 OCR → 분석 수행.

---

## 디렉터리 구조

```
nutrigo-backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/nutrigo/nutrigo_backend/
│       │       ├── domain/          # 도메인 모델
│       │       ├── controller/      # REST 컨트롤러
│       │       ├── service/         # 비즈니스 로직
│       │       ├── repository/      # 데이터 접근 계층
│       │       ├── dto/             # 데이터 전송 객체
│       │       ├── config/          # 설정 클래스
│       │       └── security/       # 보안 설정
│       └── resources/
│           ├── application.yml     # 애플리케이션 설정
│           └── db/migration/        # Flyway 마이그레이션 스크립트
├── build.gradle
└── Dockerfile
```

---

## 문제 해결

### 데이터베이스 연결 실패
- MySQL 서비스가 실행 중인지 확인
- `.env` 파일의 데이터베이스 설정 확인
- 방화벽 설정 확인

### 빌드 실패
- Java 버전이 21 이상인지 확인: `java --version`
- Gradle 캐시 삭제 후 재빌드: `gradlew clean build`

### 포트 충돌
- 다른 애플리케이션이 포트 8080을 사용 중인지 확인
- `application.yml`에서 포트 변경 가능

### S3 업로드 실패
- AWS 자격 증명이 올바른지 확인
- S3 버킷 권한 설정 확인
- 리전 설정 확인

---
