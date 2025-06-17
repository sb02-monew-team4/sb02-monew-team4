FROM amazoncorretto:17 as builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew && ./gradlew dependencies

COPY src src
RUN ./gradlew build -x test

FROM amazoncorretto:17

WORKDIR /app

ENV PROJECT_NAME=monew
ENV PROJECT_VERSION=0.0.1-SNAPSHOT
ENV JVM_OPTS=""
ENV SERVER_PORT=80

VOLUME /tmp

COPY --from=builder /app/build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar app.jar


EXPOSE 80

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
