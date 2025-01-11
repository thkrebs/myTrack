FROM azul/zulu-openjdk:23-latest
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=target/*.jar

MAINTAINER Thomas Krebs "thkrebs@gmail.com"


EXPOSE 8080
EXPOSE 8443
EXPOSE 5677

ENV DB_HOST=localhost
ENV DB_USER=postgres
ENV DB_PWD=some
ENV DB_PORT=5432

COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","/app.jar"]