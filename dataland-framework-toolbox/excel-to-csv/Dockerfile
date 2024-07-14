FROM alpine:latest@sha256:b89d9c93e9ed3597455c90a0b88a8bbb5cb7188438f70953fede212a0c4394e0
RUN apk add --no-cache libreoffice bash openjdk21-jre

ADD convert-excel-to-csv.sh .

ENTRYPOINT ["/convert-excel-to-csv.sh", "/mount/excel.xlsx"]
