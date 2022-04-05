const faker = require('faker');
const fs = require('fs')
// sets locale to de
faker.locale = 'de';

function generateCompanies() {

    let companies = []

    for (let id=1; id <= 100; id++) {
        let companyName = faker.company.companyName();
        let headquarters = faker.address.city();
        let industrialSector = faker.company.bsNoun();
        let marketCap = faker.mersenne.rand(50000, 10000000);
        let reportingDateOfMarketCap = faker.date.past().toISOString().split('T')[0]

        companies.push(
            {
                "companyName": companyName,
                "headquarters": headquarters,
                "industrialSector": industrialSector,
                "marketCap": marketCap,
                "reportingDateOfMarketCap": reportingDateOfMarketCap
            }
        )

    }

    return companies
}

function main() {
    let companiesObj = generateCompanies();
    fs.writeFileSync('./tests/e2e/fixtures/companies.json', JSON.stringify(companiesObj, null, '\t'));
}

main()