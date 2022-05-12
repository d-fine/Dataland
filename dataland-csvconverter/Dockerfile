FROM alpine:latest

COPY ./EuTaxonomyData.json /storedData/EuTaxonomyData.json
COPY ./CompanyInformation.json /storedData/CompanyInformation.json

CMD cp /storedData/*.json /testData
