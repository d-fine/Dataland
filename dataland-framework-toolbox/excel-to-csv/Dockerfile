FROM alpine:latest@sha256:51183f2cfa6320055da30872f211093f9ff1d3cf06f39a0bdb212314c5dc7375
RUN apk add --no-cache libreoffice bash openjdk21-jre

ADD convert-excel-to-csv.sh .

ENTRYPOINT ["/convert-excel-to-csv.sh", "/mount/excel.xlsx"]
