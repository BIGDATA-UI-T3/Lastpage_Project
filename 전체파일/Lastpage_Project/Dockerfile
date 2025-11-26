FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드된 jar 이름에 맞게 수정 (탐색해서 정확히 적기)
COPY build/libs/untitled-0.0.1-SNAPSHOT.jar app.jar

# 프로필 docker로 고정 (application-docker.yml 쓰게)
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8090

ENTRYPOINT ["java","-jar","/app/app.jar"]
