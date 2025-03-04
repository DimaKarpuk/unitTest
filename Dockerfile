FROM openjdk:22-jdk-slim

WORKDIR /app

COPY build.gradle settings.gradle gradlew* ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew && ./gradlew build --no-daemon

CMD ["./gradlew", "test"]


