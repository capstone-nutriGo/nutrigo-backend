# S3 버킷 CORS 설정 가이드

## 문제
프론트엔드에서 S3로 직접 파일 업로드 시 CORS 에러가 발생합니다.

## 해결 방법

### 방법 1: AWS CLI 사용 (권장)

1. AWS CLI가 설치되어 있고 설정되어 있는지 확인:
```bash
aws --version
aws configure list
```

2. CORS 설정 적용:
```bash
aws s3api put-bucket-cors \
  --bucket nutrigo-ai \
  --cors-configuration file://s3-cors-config.json \
  --region ap-northeast-2
```

3. 설정 확인:
```bash
aws s3api get-bucket-cors --bucket nutrigo-ai --region ap-northeast-2
```

### 방법 2: AWS 콘솔 사용

1. AWS 콘솔에 로그인: https://console.aws.amazon.com/s3/
2. `nutrigo-ai` 버킷 선택
3. **Permissions** 탭 클릭
4. **Cross-origin resource sharing (CORS)** 섹션에서 **Edit** 클릭
5. 다음 JSON을 붙여넣기:

```json
[
    {
        "AllowedHeaders": [
            "*"
        ],
        "AllowedMethods": [
            "GET",
            "PUT",
            "POST",
            "DELETE",
            "HEAD"
        ],
        "AllowedOrigins": [
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:5174",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000"
        ],
        "ExposeHeaders": [
            "ETag",
            "x-amz-server-side-encryption",
            "x-amz-request-id",
            "x-amz-id-2"
        ],
        "MaxAgeSeconds": 3000
    }
]
```

6. **Save changes** 클릭

## 참고사항

- 프로덕션 환경에서는 `AllowedOrigins`에 실제 도메인을 추가해야 합니다
- `MaxAgeSeconds`는 브라우저가 CORS preflight 요청 결과를 캐시하는 시간입니다 (초 단위)

