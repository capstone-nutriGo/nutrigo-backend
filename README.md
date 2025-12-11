# nutriGo-backend

## 이미지 업로드 → 분석 플로우 (S3 직업로드)
1) 클라이언트가 `POST /api/v1/nutrition/upload-url` 호출  
   - body 예: `{"type": "CART_IMAGE", "content_type": "image/jpeg"}`
   - 응답: `upload_url`, `key`, `expires_at`, `content_type`
2) 클라이언트는 presigned `upload_url` 로 이미지를 PUT 업로드.
3) 업로드 완료 후 기존 분석 API 호출 시 `image_url` 자리에 S3 키(또는 `s3://bucket/key`)를 전달.  
   - `/api/v1/nutrition/cart-image`  
   - `/api/v1/nutrition/order-image`
4) 파이썬 `nutrigo-ai` 는 S3에서 직접 이미지를 읽어 OCR → 분석 수행.

### 환경변수 (Spring)
- `aws.s3.bucket` (필수): 업로드 대상 버킷명
- `aws.s3.region`: S3 리전 (예: `ap-northeast-2`)
- `aws.s3.endpoint` (옵션): 커스텀 엔드포인트 사용 시
- `aws.access-key` / `aws.secret-key` (옵션): IAM Role 사용 시 생략 가능
- `aws.s3.url-expiration-seconds` (옵션): presigned 유효시간(기본 900초)