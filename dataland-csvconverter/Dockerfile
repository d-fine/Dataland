FROM ubuntu:latest

RUN apt-get update && apt-get upgrade -y && apt-get install samba && apt-get clean

CMD ["tail","-f","/dev/null"]
