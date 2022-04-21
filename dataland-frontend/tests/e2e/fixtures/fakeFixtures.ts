const {faker} = require('@faker-js/faker');
const apiSpecs = require( "../../../build/clients/backend/backendOpenApi.json")
const fs = require('fs')
// sets locale to de
faker.locale = 'de';

function generateCompanies() {
    const companies = []

    for (let id = 1; id <= 100; id++) {
        const companyName = faker.company.companyName();
        const headquarters = faker.address.city();
        const sector = faker.company.bsNoun();
        const marketCap = faker.mersenne.rand(50000, 10000000);
        const reportingDateOfMarketCap = faker.date.past().toISOString().split('T')[0]
        const indices = faker.random.arrayElements( apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum );
        const identifiers = faker.random.arrayElements([
            {
                "identifierType": apiSpecs.components.schemas.CompanyIdentifier.properties.identifierType.enum[0],
                "identifierValue": "529900W18LQJJN6SJ336"
            },
            {
                "identifierType": apiSpecs.components.schemas.CompanyIdentifier.properties.identifierType.enum[1],
                "identifierValue": "529900W18LQJJN6SJ336"
            },
            {
                "identifierType": apiSpecs.components.schemas.CompanyIdentifier.properties.identifierType.enum[2],
                "identifierValue": "529900W18LQJJN6SJ336"
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

function generateTaxonomies() {
    const taxonomies = []

    for (let id = 1; id <= 100; id++) {
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
    const companiesObj = generateCompanies();
    const taxonomiesObj = generateTaxonomies();
    fs.writeFileSync('./tests/e2e/fixtures/CompanyInformation.json', JSON.stringify(companiesObj, null, '\t'));
    fs.writeFileSync('./tests/e2e/fixtures/CompanyAssociatedEuTaxonomyData.json', JSON.stringify(taxonomiesObj, null, '\t'));
}

main()