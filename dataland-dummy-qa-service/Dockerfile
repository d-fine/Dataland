#FROM eclipse-temurin:17.0.5_8-jre-alpine AS builder
#WORKDIR /src
#ADD . /src
#RUN ./gradlew clean bootJar

#FROM eclipse-temurin:17.0.5_8-jre-alpine
#WORKDIR /app
#COPY --from=builder /src/build/libs/*-0.0.1-SNAPSHOT.jar .
#USER guest
#CMD exec java -jar *.jar
#RUN --mount=type=secret,id=DOCKER_SECRET,target=/root/.gradle/gradle.properties,required=true \
   #  ./gradlew dataland-dummy-qa-service:bootJar --no-daemon --stacktrace

ARG DATALAND_DUMMY_QA_SERVICE_BASE_VERSION
FROM ghcr.io/d-fine/dataland/dataland_dummy_qa_service_base:${DATALAND_DUMMY_QA_SERVICE_BASE_VERSION} as builder

FROM eclipse-temurin:17.0.5_8-jre-alpine
COPY --from=builder /app/dataland-dummy-qa-service/build/libs/dataland-dummy-qa-service-0.0.1-SNAPSHOT.jar /jar/dataland-dummy-qa-service.jar
CMD ["java","-jar","/jar/dataland-dummy-qa-service.jar"]

#HEALTHCHECK --interval=30s --timeout=3s CMD wget -nv -O- -t 1 http://internal-storage:8080/internal-storage/actuator/health | tee /dev/stderr | grep -q '^{"status":"UP"' || false
