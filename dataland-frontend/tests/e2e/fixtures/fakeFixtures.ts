const {faker} = require('@faker-js/faker');
const backend = require( "../../../build/clients/backend/backendOpenApi.json")
const fs = require('fs')
// sets locale to de
faker.locale = 'de';

function generateCompanies() {
    let companies = []

    for (let id = 1; id <= 100; id++) {
        let companyName = faker.company.companyName();
        let headquarters = faker.address.city();
        let sector = faker.company.bsNoun();
        let marketCap = faker.mersenne.rand(50000, 10000000);
        let reportingDateOfMarketCap = faker.date.past().toISOString().split('T')[0]
        let indices = faker.random.arrayElements( backend.components.schemas.CompanyInformation.properties["indices"].items.enum );
        let identifiers = faker.random.arrayElements([
            {
                "identifierType": backend.components.schemas.CompanyIdentifier.properties.identifierType.enum[0],
                "identifierValue": "529900W18LQJJN6SJ336"
            },
            {
                "identifierType": backend.components.schemas.CompanyIdentifier.properties.identifierType.enum[1],
                "identifierValue": "529900W18LQJJN6SJ336"
            },
            {
                "identifierType": backend.components.schemas.CompanyIdentifier.properties.identifierType.enum[2],
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
    let taxonomies = []

    for (let id = 1; id <= 100; id++) {
        let attestation = faker.random.arrayElement(backend.components.schemas.EuTaxonomyData.properties["Attestation"].enum);
        let reportingObligation = faker.random.arrayElement(backend.components.schemas.EuTaxonomyData.properties["Reporting Obligation"].enum);
        let capexTotal = faker.mersenne.rand(50000, 10000000);
        let capexEligible = faker.mersenne.rand(50000, capexTotal);
        let capexAligned = faker.mersenne.rand(50000, capexEligible);
        let opexTotal = faker.mersenne.rand(50000, 10000000);
        let opexEligible = faker.mersenne.rand(50000, opexTotal);
        let opexAligned = faker.mersenne.rand(50000, opexEligible);
        let revenueTotal = faker.mersenne.rand(50000, 10000000);
        let revenueEligible = faker.mersenne.rand(50000, revenueTotal);
        let revenueAligned = faker.mersenne.rand(50000, revenueEligible);


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
    let companiesObj = generateCompanies();
    let taxonomiesObj = generateTaxonomies();
    fs.writeFileSync('./tests/e2e/fixtures/CompanyInformation.json', JSON.stringify(companiesObj, null, '\t'));
    fs.writeFileSync('./tests/e2e/fixtures/CompanyAssociatedEuTaxonomyData.json', JSON.stringify(taxonomiesObj, null, '\t'));
}

main()