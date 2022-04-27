import faker from "@faker-js/faker";
import {humanize} from '@/utils/StringHumanizer';
import apiSpecs from "../../../build/clients/backend/backendOpenApi.json";

const stockIndexArray = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum
const identifierTypeArray = apiSpecs.components.schemas.CompanyIdentifier.properties.identifierType.enum

const {parse} = require('json2csv');
const fs = require('fs')
// import StringHumanizer from('../../../src/utils/StringHumanizer');
// sets locale to de

faker.locale = 'de';

function generateCompanyInformation() {
    const companies = []

    for (let id = 1; id <= 100; id++) {
        const companyName = faker.company.companyName();
        const headquarters = faker.address.city();
        const sector = faker.company.bsNoun();
        const marketCap = faker.mersenne.rand(10000000, 50000);
        const reportingDateOfMarketCap = faker.date.past().toISOString().split('T')[0]
        const indices = faker.random.arrayElements(stockIndexArray);
        const identifiers = faker.random.arrayElements([
            {
                "identifierType": identifierTypeArray[0],
                "identifierValue": faker.random.alphaNumeric(12)
            },
            {
                "identifierType": identifierTypeArray[1],
                "identifierValue": faker.random.alphaNumeric(12)
            },
            {
                "identifierType": identifierTypeArray[2],
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

    for (let id = 1; id <= 100; id++) {
        const attestation = faker.random.arrayElement(apiSpecs.components.schemas.EuTaxonomyData.properties["Attestation"].enum);
        const reportingObligation = faker.random.arrayElement(apiSpecs.components.schemas.EuTaxonomyData.properties["Reporting Obligation"].enum);
        const capexTotal = faker.finance.amount(50000, 10000000, 2);
        const capexEligible = faker.datatype.float({ min: 0, max: 1, precision: 0.0001 })
        const capexAligned = faker.datatype.float({ min: 0, max: capexEligible, precision: 0.0001 })
        const opexTotal = faker.finance.amount(50000, 10000000, 2);
        const opexEligible = faker.datatype.float({ min: 0, max: 1, precision: 0.0001 })
        const opexAligned = faker.datatype.float({ min: 0, max: opexEligible, precision: 0.0001 })
        const revenueTotal = faker.finance.amount(50000, 10000000, 2);
        const revenueEligible = faker.datatype.float({ min: 0, max: 1, precision: 0.0001 })
        const revenueAligned = faker.datatype.float({ min: 0, max: revenueEligible, precision: 0.0001 })


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

function stockIndexValue(stockIndexList: Array<string>, stockIndex: string) {
    return stockIndexList.includes(stockIndex) ? "x" : ""
}

function identifierValue(identifierArray: Array<Object>, identifierType: string) {
    const identifierObject: any = identifierArray.find((identifier: any) => {
        return identifier.identifierType === identifierType
    })
    return identifierObject ? identifierObject.identifierValue : ""
}

function percentageGenerator(value:number){
    return (Math.round(value * 100 * 100) / 100).toString() + "%"
}

function generateCSVData(companyInformation: Array<Object>, companyAssociatedEuTaxonomyData: Array<Object>) {
    const mergedData = companyInformation.map((element, index) => {
        return {...element, ...companyAssociatedEuTaxonomyData[index]}
    })
    const dateOptions: any = {year: 'numeric', month: 'numeric', day: 'numeric'};
    const dateLocale = 'de-DE';

    const options = {
        fields: [
            {label: 'Company name', value: 'companyName'},
            {label: 'Headquarter', value: 'headquarters'},
            {label: 'Sektor', value: 'sector'},
            {label: 'Market Capitalization (EURmm)', value: 'marketCap'},
            {label: 'Market Capitalization Date', value: (row: any) => new Date(row.reportingDateOfMarketCap).toLocaleDateString(dateLocale, dateOptions) },
            {label: 'Total Revenue in EURmio', value: 'data.Revenue.total'},
            {label: 'Total CapEx EURmio', value: 'data.Capex.total'},
            {label: 'Total OpEx EURmio', value: 'data.Opex.total'},
            {label: 'Eligible Revenue', value: (row: any) => percentageGenerator(row.data.Revenue.eligible)},
            {label: 'Eligible CapEx', value: (row: any) => percentageGenerator(row.data.Capex.eligible)},
            {label: 'Eligible OpEx', value: (row: any) => percentageGenerator(row.data.Opex.eligible)},
            {label: 'Aligned Revenue', value: (row: any) => percentageGenerator(row.data.Revenue.aligned)},
            {label: 'Aligned CapEx', value: (row: any) => percentageGenerator(row.data.Capex.aligned)},
            {label: 'Aligned OpEx', value: (row: any) => percentageGenerator(row.data.Opex.aligned)},
            {label: 'IS/FS', value: 'companyType', default: 'IS'},
            {label: 'NFRD Pflicht', value: (row: any) => row.data["Reporting Obligation"] === "Yes" ? "Ja" : "" },
            {label: 'Assurance', value: (row: any) => {if(row.data["Attestation"] === "LimitedAssurance"){
                    return "limited"
                }  else if (row.data["Attestation"] === "ReasonableAssurance") {
                    return "reasonable"
                } else {
                    return "none"
                }
            }},
            ...stockIndexArray.map((e: any) => {
                return {label: humanize(e), value: (row: any) => stockIndexValue(row.indices, e)}
            }),
            ...identifierTypeArray.map((e: any) => {
                return {label: humanize(e), value: (row: any) => identifierValue(row.identifiers, e)}
            }),
        ],
        delimiter: ';'
    };
    return parse(mergedData, options);
}


function main() {
    const CompanyInformation = generateCompanyInformation();
    const CompanyAssociatedEuTaxonomyData = generateCompanyAssociatedEuTaxonomyData();
    const csv = generateCSVData(CompanyInformation, CompanyAssociatedEuTaxonomyData)

    fs.writeFileSync('../testing/data/csvTestData.csv', csv, "ascii");
    fs.writeFileSync('../testing/data/CompanyInformation.json', JSON.stringify(CompanyInformation, null, '\t'));
    fs.writeFileSync('../testing/data/CompanyAssociatedEuTaxonomyData.json', JSON.stringify(CompanyAssociatedEuTaxonomyData, null, '\t'));
}

main()