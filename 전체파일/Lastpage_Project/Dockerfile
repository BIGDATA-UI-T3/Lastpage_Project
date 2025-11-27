# ========================================================
# 1. BUILDER STAGE: 빌드 환경 (더 무거움)
# ========================================================
# 빌드에 필요한 JDK와 Gradle이 포함된 이미지 사용
FROM eclipse-temurin:21-jdk AS builder 

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
# 캐싱을 위해 필요한 파일들을 먼저 복사합니다.
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드를 모두 복사합니다.
COPY src src

# 프로젝트 빌드 실행
# (권장) 이 명령어가 JAR 파일을 생성합니다.
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test 
# -x test 옵션은 빌드 시간을 줄이기 위해 테스트를 건너뛰는 것입니다.

# ========================================================
# 2. FINAL STAGE: 실행 환경 (더 가벼움)
# ========================================================
# 애플리케이션 실행에 필요한 JRE만 포함된 이미지 사용
FROM eclipse-temurin:21-jre

# 작업 디렉토리 설정
WORKDIR /app

# --------------------------------------------------------
# 💡 핵심: 이전 스테이지(builder)에서 생성된 JAR 파일 복사
# --------------------------------------------------------
# JAR 파일 이름이 'untitled-0.0.1-SNAPSHOT.jar' 라고 가정합니다.
# 실제 파일 이름이 'lastpage-0.0.1-SNAPSHOT.jar' 인 경우 이 부분을 수정하세요!
COPY --from=builder /app/build/libs/untitled-0.0.1-SNAPSHOT.jar app.jar

# 프로필 docker로 고정 (application-docker.yml 쓰게)
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8090

ENTRYPOINT ["java","-jar","/app/app.jar"]