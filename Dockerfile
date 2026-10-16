
ARG RUNTIME_IMAGE=eclipse-temurin:21-jre-alpine
FROM ${RUNTIME_IMAGE}


RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --chown=app:app build/app.jar /app/app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
