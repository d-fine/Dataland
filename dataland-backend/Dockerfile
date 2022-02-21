FROM eclipse-temurin:17.0.2_8-jre-alpine

COPY dataland-backend/build/libs/*.jar /app/
RUN rm /app/*-plain.jar
EXPOSE 8080
WORKDIR /app

CMD sh -c "java -jar *.jar || tail -f /dev/null"
