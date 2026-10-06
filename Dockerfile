FROM maven:3.9.11-eclipse-temurin-21

ARG TEST_PROFILE=api
ARG APIBASEURL=http://localhost:4111
ARG UIBASEURL=http://localhost:3000

ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY . .

USER root

CMD ["/bin/bash", "-c", "mkdir -p /app/logs && mvn test 2>&1 | tee /app/logs/run.log"]