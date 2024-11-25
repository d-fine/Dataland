FROM alpine:latest@sha256:1e42bbe2508154c9126d48c2b8a75420c3544343bf86fd041fb7527e017a4b4a
RUN apk add --no-cache libreoffice bash openjdk21-jre

ADD convert-excel-to-csv.sh .

ENTRYPOINT ["/convert-excel-to-csv.sh", "/mount/excel.xlsx"]
