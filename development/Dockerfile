FROM ubuntu:latest

RUN apt-get update && apt-get install -y \
	openjdk-17-jdk \
	curl \
	dos2unix \
    vim \
    wget

EXPOSE 8080

CMD tail -f /dev/null