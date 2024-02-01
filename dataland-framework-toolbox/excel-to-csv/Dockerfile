FROM alpine:latest
RUN apk add --no-cache libreoffice bash openjdk17-jre

ADD convert-excel-to-csv.sh .

ENTRYPOINT ["/convert-excel-to-csv.sh", "/mount/excel.xlsx"]
