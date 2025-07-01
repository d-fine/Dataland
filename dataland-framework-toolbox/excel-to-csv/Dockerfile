FROM alpine:latest@sha256:8a1f59ffb675680d47db6337b49d22281a139e9d709335b492be023728e11715
RUN apk add --no-cache libreoffice bash openjdk21-jre

ADD convert-excel-to-csv.sh .

ENTRYPOINT ["/convert-excel-to-csv.sh", "/mount/excel.xlsx"]
