const {faker} = require('@faker-js/faker');
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
        let indices = faker.random.arrayElements([
            "CDAX",
            "DAX",
            "GeneralStandards",
            "GEX",
            "MDAX",
            "PrimeStandards",
            "SDAX",
            "TecDAX",
            "ScaleHDAX",
            "DAX_50_ESG"
        ]);
        let identifiers = faker.random.arrayElements([
            { "type": "Lei", "value": "529900W18LQJJN6SJ336"},
            { "type": "Isin", "value": "529900W18LQJJN6SJ336"},
            { "type": "PermId", "value": "529900W18LQJJN6SJ336"}
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
        let attestation = faker.random.arrayElement([
            "None",
            "LimitedAssurance",
            "ReasonableAssurance"
        ]);
        let reportingObligation = faker.random.arrayElement([
            "Yes",
            "No"
        ]);
        let capexTotal = faker.mersenne.rand(50000, 10000000);
        let capexEligible = faker.mersenne.rand(50000, capexTotal);
        let capexAligned = faker.mersenne.rand(50000, capexTotal);
        let opexTotal = faker.mersenne.rand(50000, 10000000);
        let opexEligible = faker.mersenne.rand(50000, opexTotal);
        let opexAligned = faker.mersenne.rand(50000, opexTotal);
        let revenueTotal = faker.mersenne.rand(50000, 10000000);
        let revenueEligible = faker.mersenne.rand(50000, revenueTotal);
        let revenueAligned = faker.mersenne.rand(50000, revenueTotal);


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
    fs.writeFileSync('./tests/e2e/fixtures/companies.json', JSON.stringify(companiesObj, null, '\t'));
    fs.writeFileSync('./tests/e2e/fixtures/eutaxonomies.json', JSON.stringify(taxonomiesObj, null, '\t'));
}

main()