FROM ubuntu:latest

#RUN apt-get update && apt-get install -y \
#	openjdk-17-jdk \
#	curl \
#	dos2unix \
#    vim \
#    wget

WORKDIR /app/dataland-frontend
COPY dataland-frontend/package*.json .
WORKDIR /app
COPY dataland-frontend .
#COPY --from=prepare-stage /app/dataland-frontend/src/clients ./src

EXPOSE 8080

CMD tail -f /dev/null