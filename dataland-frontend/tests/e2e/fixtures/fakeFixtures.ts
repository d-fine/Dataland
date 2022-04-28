const {faker} = require('@faker-js/faker');
const apiSpecs = require( "../../../build/clients/backend/backendOpenApi.json")
const fs = require('fs')
// sets locale to de
faker.locale = 'de';

function generateCompanyInformation() {
    const companies = []
    for (let id = 1; id <= 250; id++) {
        const companyName = faker.company.companyName();
        const headquarters = faker.address.city();
        const sector = faker.company.bsNoun();
        const marketCap = faker.mersenne.rand(50000, 10000000);
        const reportingDateOfMarketCap = faker.date.past().toISOString().split('T')[0]
        const indices = faker.random.arrayElements( apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum );
        const identifiers = faker.random.arrayElements([
            {
                "identifierType": apiSpecs.components.schemas.CompanyIdentifier.properties.identifierType.enum[0],
                "identifierValue": faker.random.alphaNumeric(12)
            },
            {
                "identifierType": apiSpecs.components.schemas.CompanyIdentifier.properties.identifierType.enum[1],
                "identifierValue": faker.random.alphaNumeric(12)
            },
            {
                "identifierType": apiSpecs.components.schemas.CompanyIdentifier.properties.identifierType.enum[2],
                "identifierValue": faker.random.alphaNumeric(12)
            }
        ]);

        companies.push(
            {
                "companyName": companyName,
                "headquarters": headquarters,
                "sector": sector,
                "marketCap": marketCap,
                "reportingDateOfMarketCap": reportingDateOfMarketCap,
                "indices": indices,
                "identifiers": identifiers
            }
        )

    }

    return companies
}

function generateCompanyAssociatedEuTaxonomyData() {
    const taxonomies = []

    for (let id = 1; id <= 250; id++) {
        const attestation = faker.random.arrayElement(apiSpecs.components.schemas.EuTaxonomyData.properties["Attestation"].enum);
        const reportingObligation = faker.random.arrayElement(apiSpecs.components.schemas.EuTaxonomyData.properties["Reporting Obligation"].enum);
        const capexTotal = faker.mersenne.rand(50000, 10000000);
        const capexEligible = faker.mersenne.rand(50000, capexTotal);
        const capexAligned = faker.mersenne.rand(50000, capexEligible);
        const opexTotal = faker.mersenne.rand(50000, 10000000);
        const opexEligible = faker.mersenne.rand(50000, opexTotal);
        const opexAligned = faker.mersenne.rand(50000, opexEligible);
        const revenueTotal = faker.mersenne.rand(50000, 10000000);
        const revenueEligible = faker.mersenne.rand(50000, revenueTotal);
        const revenueAligned = faker.mersenne.rand(50000, revenueEligible);


        taxonomies.push(
            {
                "companyId": id,
                "data": {
                    "Capex": {
                        "total": capexTotal,
                        "aligned": capexAligned,
                        "eligible": capexEligible
                    },
                    "Opex": {
                        "total": opexTotal,
                        "aligned": opexAligned,
                        "eligible": opexEligible
                    },
                    "Revenue": {
                        "total": revenueTotal,
                        "aligned": revenueAligned,
                        "eligible": revenueEligible
                    },
                    "Reporting Obligation": reportingObligation,
                    "Attestation": attestation
                }
            }
        )

    }

    return taxonomies
}

function main() {
    const CompanyInformation = generateCompanyInformation();
    const CompanyAssociatedEuTaxonomyData = generateCompanyAssociatedEuTaxonomyData();
    fs.writeFileSync('../testing/data/CompanyInformation.json', JSON.stringify(CompanyInformation, null, '\t'));
    fs.writeFileSync('../testing/data/CompanyAssociatedEuTaxonomyData.json', JSON.stringify(CompanyAssociatedEuTaxonomyData, null, '\t'));
}

main()