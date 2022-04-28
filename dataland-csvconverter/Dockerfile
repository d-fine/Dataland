FROM alpine:latest

COPY ./CompanyAssociatedEuTaxonomyData.json /storedData/CompanyAssociatedEuTaxonomyData.json
COPY ./CompanyInformation.json /storedData/CompanyInformation.json

CMD cp /storedData/*.json /testData
