#FROM eclipse-temurin:17-jdk AS build
#WORKDIR /app
#COPY gradlew .
#COPY gradle gradle
#RUN chmod +x gradlew
#COPY . .
#RUN ./gradlew clean build -x test --no-daemon --stacktrace
#
#FROM eclipse-temurin:17-jdk
#WORKDIR /app
#COPY --from=build /app/build/libs/*.jar app.jar
#EXPOSE 8090
#ENTRYPOINT ["java", "-jar", "app.jar"]
