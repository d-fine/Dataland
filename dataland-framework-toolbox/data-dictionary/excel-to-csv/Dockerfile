FROM alpine:latest
RUN apk add --no-cache libreoffice bash openjdk17-jre

ADD convert-excel-to-csv.sh .

CMD ["/convert-excel-to-csv.sh", "/mount/dataDictionary.xlsx", "/mount/csv"]