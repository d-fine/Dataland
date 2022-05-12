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
        const companyName = faker.company.companyName();
        const headquarters = faker.address.city();
        const sector = faker.company.bsNoun();
        const marketCap = faker.mersenne.rand(10000000, 50000);
        const reportingDateOfMarketCap = faker.date.past().toISOString().split('T')[0]
        const indices =faker.helpers.arrayElements(stockIndexArray);
        const identifiers =faker.helpers.arrayElements([
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
        return  {
            "companyName": companyName,
            "headquarters": headquarters,
            "sector": sector,
            "marketCap": marketCap,
            "reportingDateOfMarketCap": reportingDateOfMarketCap,
            "indices": indices,
            "identifiers": identifiers
        }
}


function generateEuTaxonomyData() {
        const attestation =faker.helpers.arrayElement(apiSpecs.components.schemas.EuTaxonomyData.properties["Attestation"].enum);
        const reportingObligation =faker.helpers.arrayElement(apiSpecs.components.schemas.EuTaxonomyData.properties["Reporting Obligation"].enum);
        const capexTotal = faker.finance.amount(minEuro, maxEuro, 2);
        const capexEligible = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4)
        const capexAligned = faker.datatype.float({ min: 0, max: parseFloat(capexEligible), precision: resolution }).toFixed(4)
        const opexTotal = faker.finance.amount(minEuro, maxEuro, 2);
        const opexEligible = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4)
        const opexAligned = faker.datatype.float({ min: 0, max: parseFloat(opexEligible), precision: resolution }).toFixed(4)
        const revenueTotal = faker.finance.amount(minEuro, maxEuro, 2);
        const revenueEligible = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4)
        const revenueAligned = faker.datatype.float({ min: 0, max: parseFloat(revenueEligible), precision: resolution }).toFixed(4)

        return  {
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

function generateCompanyWithEuTaxonomyData() {
    const companiesWithEuTaxonomyData = []
    for (let id = 1; id <= 250; id++) {
        companiesWithEuTaxonomyData.push({companyInformation: generateCompanyInformation() , euTaxonomyData: generateEuTaxonomyData()})
    }
    return companiesWithEuTaxonomyData
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

function generateCSVData(companyInformationWithEuTaxonomyData: Array<Object>) {
    const mergedData = companyInformationWithEuTaxonomyData.map((element:any) => {
        return {...element["companyInformation"], ...element["euTaxonomyData"]}
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
            {label: 'Total Revenue EUR', value: (row: any) => euroGenerator(row.Revenue.totalAmount)},
            {label: 'Total CapEx EUR', value: (row: any) => euroGenerator(row.Capex.totalAmount)},
            {label: 'Total OpEx EUR', value: (row: any) => euroGenerator(row.Opex.totalAmount)},
            {label: 'Eligible Revenue', value: (row: any) => percentageGenerator(row.Revenue.eligiblePercentage)},
            {label: 'Eligible CapEx', value: (row: any) => percentageGenerator(row.Capex.eligiblePercentage)},
            {label: 'Eligible OpEx', value: (row: any) => percentageGenerator(row.Opex.eligiblePercentage)},
            {label: 'Aligned Revenue', value: (row: any) => percentageGenerator(row.Revenue.alignedPercentage)},
            {label: 'Aligned CapEx', value: (row: any) => percentageGenerator(row.Capex.alignedPercentage)},
            {label: 'Aligned OpEx', value: (row: any) => percentageGenerator(row.Opex.alignedPercentage)},
            {label: 'IS/FS', value: 'companyType', default: 'IS'},
            {label: 'NFRD mandatory', value: (row: any) => row["Reporting Obligation"] === "Yes" ? "Ja" : "" },
            {label: 'Assurance', value: (row: any) => {if(row["Attestation"] === "LimitedAssurance"){
                    return "limited"
                }  else if (row["Attestation"] === "ReasonableAssurance") {
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
    const companyInformationWithEuTaxonomyData = generateCompanyWithEuTaxonomyData();
    const csv = generateCSVData(companyInformationWithEuTaxonomyData)

    fs.writeFileSync('../testing/data/csvTestData.csv', csv);
    fs.writeFileSync('../testing/data/CompanyInformationWithEuTaxonomyData.json', JSON.stringify(companyInformationWithEuTaxonomyData, null, '\t'));
}

main()