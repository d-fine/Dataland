FROM alpine:latest@sha256:56fa17d2a7e7f168a043a2712e63aed1f8543aeafdcee47c58dcffe38ed51099
RUN apk add --no-cache libreoffice bash openjdk21-jre

ADD convert-excel-to-csv.sh .

ENTRYPOINT ["/convert-excel-to-csv.sh", "/mount/excel.xlsx"]
