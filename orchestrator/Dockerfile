FROM eclipse-temurin:25-jre

WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring

COPY build/libs/app.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
