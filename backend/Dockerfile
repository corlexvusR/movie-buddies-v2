FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# 시스템 패키지 설치 (빌드용)
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 소스 코드 복사
COPY src src

# Gradle Wrapper 실행 권한 부여 및 애플리케이션 빌드
RUN chmod +x ./gradlew && \
    ./gradlew build -x test --no-daemon

# 런타임 스테이지
FROM openjdk:17-jre-slim

# 시스템 패키지 설치 (런타임용)
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 사용자 생성 (보안 강화)
RUN addgroup --system --gid 1001 moviebuddies && \
    adduser --system --uid 1001 --gid 1001 --shell /bin/bash moviebuddies

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 업로드 및 로그 디렉토리 생성
RUN mkdir -p /app/uploads/profile-images && \
    mkdir -p /app/uploads/chat-images && \
    mkdir -p /var/log/moviebuddies && \
    chown -R moviebuddies:moviebuddies /app && \
    chown -R moviebuddies:moviebuddies /var/log/moviebuddies

# 사용자 전환
USER moviebuddies

EXPOSE 8080

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 옵션 최적화 (메모리 사용량 고려)
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxGCPauseMillis=200"

# 애플리케이션 실행
CMD java $JAVA_OPTS -jar app.jar