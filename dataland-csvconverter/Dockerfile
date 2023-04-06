FROM alpine:3.17.3

COPY CompanyInformationWithEuTaxonomyDataForNonFinancials.json /storedData/CompanyInformationWithEuTaxonomyDataForNonFinancials.json
COPY CompanyInformationWithEuTaxonomyDataForFinancials.json /storedData/CompanyInformationWithEuTaxonomyDataForFinancials.json
COPY pdfs/*.pdf /storedData/pdfs/

CMD cp /storedData/*.json /testData
