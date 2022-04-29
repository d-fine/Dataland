import faker from "@faker-js/faker";
import {humanize} from '../../../src/utils/StringHumanizer';
import apiSpecs from "../../../build/clients/backend/backendOpenApi.json";

const stockIndexArray = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum
const identifierTypeArray = apiSpecs.components.schemas.CompanyIdentifier.properties.identifierType.enum

const {parse} = require('json2csv');
const fs = require('fs')

faker.locale = 'de';

const maxEuro=1000000
const minEuro=50000
const resolution=0.0001

function generateCompanyInformation() {
    const companies = []
    for (let id = 1; id <= 250; id++) {
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
        ]).sort((a,b) => {return a.identifierType.localeCompare(b.identifierType)});

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
        const capexTotal = faker.finance.amount(minEuro, maxEuro, 2);
        const capexEligible = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4)
        const capexAligned = faker.datatype.float({ min: 0, max: parseFloat(capexEligible), precision: resolution }).toFixed(4)
        const opexTotal = faker.finance.amount(minEuro, maxEuro, 2);
        const opexEligible = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4)
        const opexAligned = faker.datatype.float({ min: 0, max: parseFloat(opexEligible), precision: resolution }).toFixed(4)
        const revenueTotal = faker.finance.amount(minEuro, maxEuro, 2);
        const revenueEligible = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4)
        const revenueAligned = faker.datatype.float({ min: 0, max: parseFloat(revenueEligible), precision: resolution }).toFixed(4)


        taxonomies.push(
            {
                "companyId": id,
                "data": {
                    "Capex": {
                        "totalAmount": capexTotal,
                        "alignedPercentage": capexAligned,
                        "eligiblePercentage": capexEligible
                    },
                    "Opex": {
                        "totalAmount": opexTotal,
                        "alignedPercentage": opexAligned,
                        "eligiblePercentage": opexEligible
                    },
                    "Revenue": {
                        "totalAmount": revenueTotal,
                        "alignedPercentage": revenueAligned,
                        "eligiblePercentage": revenueEligible
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
    return (Math.round(value * 100 * 100) / 100).toFixed(2).replace(".",",") + "%"
}

function euroGenerator(value:number){
    return value.toString().replace(".",",")
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
            {label: 'Sector', value: 'sector'},
            {label: 'Market Capitalization EUR', value: 'marketCap'},
            {label: 'Market Capitalization Date', value: (row: any) => new Date(row.reportingDateOfMarketCap).toLocaleDateString(dateLocale, dateOptions) },
            {label: 'Total Revenue EUR', value: (row: any) => euroGenerator(row.data.Revenue.totalAmount)},
            {label: 'Total CapEx EUR', value: (row: any) => euroGenerator(row.data.Capex.totalAmount)},
            {label: 'Total OpEx EUR', value: (row: any) => euroGenerator(row.data.Opex.totalAmount)},
            {label: 'Eligible Revenue', value: (row: any) => percentageGenerator(row.data.Revenue.eligiblePercentage)},
            {label: 'Eligible CapEx', value: (row: any) => percentageGenerator(row.data.Capex.eligiblePercentage)},
            {label: 'Eligible OpEx', value: (row: any) => percentageGenerator(row.data.Opex.eligiblePercentage)},
            {label: 'Aligned Revenue', value: (row: any) => percentageGenerator(row.data.Revenue.alignedPercentage)},
            {label: 'Aligned CapEx', value: (row: any) => percentageGenerator(row.data.Capex.alignedPercentage)},
            {label: 'Aligned OpEx', value: (row: any) => percentageGenerator(row.data.Opex.alignedPercentage)},
            {label: 'IS/FS', value: 'companyType', default: 'IS'},
            {label: 'NFRD mandatory', value: (row: any) => row.data["Reporting Obligation"] === "Yes" ? "Ja" : "" },
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

    fs.writeFileSync('../testing/data/csvTestData.csv', csv);
    fs.writeFileSync('../testing/data/CompanyInformation.json', JSON.stringify(CompanyInformation, null, '\t'));
    fs.writeFileSync('../testing/data/CompanyAssociatedEuTaxonomyData.json', JSON.stringify(CompanyAssociatedEuTaxonomyData, null, '\t'));
}

main()