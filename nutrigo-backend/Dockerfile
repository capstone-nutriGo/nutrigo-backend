# nutrigo-backend/Dockerfile

########################
# 1단계: 빌드 컨테이너
########################
FROM gradle:8.10-jdk21-alpine AS builder

# 작업 디렉토리
WORKDIR /workspace

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# 소스 코드 복사
COPY src ./src

# (옵션) Gradle 캐시를 활용하기 위해 의존성만 먼저 다운
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# 실제 애플리케이션 빌드 (bootJar)
RUN ./gradlew bootJar --no-daemon

########################
# 2단계: 런타임 컨테이너
########################
FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드 단계에서 만들어진 JAR 파일 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

# Spring 프로파일 (필요에 따라 dev/prod 등으로 변경)
ENV SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 포트
EXPOSE 8080

# 스프링 부트 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
