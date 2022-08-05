import { faker } from "@faker-js/faker";
import { humanizeString } from "../../../src/utils/StringHumanizer";
import apiSpecs from "../../../build/clients/backend/backendOpenApi.json";

const stockIndexArray = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum;
const identifierTypeArray = apiSpecs.components.schemas.CompanyIdentifier.properties.identifierType.enum;

const { parse } = require("json2csv");
const fs = require("fs");

faker.locale = "de";

const maxEuro = 1000000;
const minEuro = 50000;
const resolution = 0.0001;

function generateCompanyInformation() {
  const companyName = faker.company.companyName();
  const headquarters = faker.address.city();
  const sector = faker.company.bsNoun();
  const marketCap = faker.mersenne.rand(10000000, 50000);
  const reportingDateOfMarketCap = faker.date.past().toISOString().split("T")[0];
  const indices = faker.helpers.arrayElements(stockIndexArray);
  const identifiers = faker.helpers
    .arrayElements([
      {
        identifierType: identifierTypeArray[0],
        identifierValue: faker.random.alphaNumeric(12),
      },
      {
        identifierType: identifierTypeArray[1],
        identifierValue: faker.random.alphaNumeric(12),
      },
      {
        identifierType: identifierTypeArray[2],
        identifierValue: faker.random.alphaNumeric(12),
      },
    ])
    .sort((a, b) => {
      return a.identifierType.localeCompare(b.identifierType);
    });
  const countryCode = faker.address.countryCode();
  return {
    companyName: companyName,
    headquarters: headquarters,
    sector: sector,
    marketCap: marketCap,
    reportingDateOfMarketCap: reportingDateOfMarketCap,
    indices: indices,
    identifiers: identifiers,
    countryCode: countryCode,
  };
}

function generateEuTaxonomyDataForNonFinancials() {
  const attestation = faker.helpers.arrayElement(
    apiSpecs.components.schemas.EuTaxonomyDataForNonFinancials.properties["Attestation"].enum
  );
  const reportingObligation = faker.helpers.arrayElement(
    apiSpecs.components.schemas.EuTaxonomyDataForNonFinancials.properties["Reporting Obligation"].enum
  );
  const capexTotal = faker.finance.amount(minEuro, maxEuro, 2);
  const capexEligible = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);
  const capexAligned = faker.datatype
    .float({ min: 0, max: parseFloat(capexEligible), precision: resolution })
    .toFixed(4);
  const opexTotal = faker.finance.amount(minEuro, maxEuro, 2);
  const opexEligible = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);
  const opexAligned = faker.datatype.float({ min: 0, max: parseFloat(opexEligible), precision: resolution }).toFixed(4);
  const revenueTotal = faker.finance.amount(minEuro, maxEuro, 2);
  const revenueEligible = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);
  const revenueAligned = faker.datatype
    .float({ min: 0, max: parseFloat(revenueEligible), precision: resolution })
    .toFixed(4);

  return {
    Capex: {
      totalAmount: capexTotal,
      alignedPercentage: capexAligned,
      eligiblePercentage: capexEligible,
    },
    Opex: {
      totalAmount: opexTotal,
      alignedPercentage: opexAligned,
      eligiblePercentage: opexEligible,
    },
    Revenue: {
      totalAmount: revenueTotal,
      alignedPercentage: revenueAligned,
      eligiblePercentage: revenueEligible,
    },
    "Reporting Obligation": reportingObligation,
    Attestation: attestation,
  };
}

function generateEuTaxonomyDataForFinancials() {
  const attestation = faker.helpers.arrayElement(
    apiSpecs.components.schemas.EuTaxonomyDataForFinancials.properties["Attestation"].enum
  );
  const reportingObligation = faker.helpers.arrayElement(
    apiSpecs.components.schemas.EuTaxonomyDataForFinancials.properties["Reporting Obligation"].enum
  );
  const totalAssets = faker.finance.amount(minEuro, maxEuro, 2);
  const taxonomyEligibleEconomicActivity = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);
  const eligibleDerivates = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);
  const eligibleBanksAndIssuers = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);
  const eligibleNonNfrd = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);
  const eligibleTradingPortfolio = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);
  const eligibleOnDemandLoans = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);
  const eligibleTradingPortfolioAndLoans = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);
  const eligibleNonLifeInsurance = faker.datatype.float({ min: 0, max: 1, precision: resolution }).toFixed(4);

  return {
    Exposure: {
      totalAmount: totalAssets,
      eligibleActivity: taxonomyEligibleEconomicActivity,
      eligibleDerivates: eligibleDerivates,
      eligibleBanksAndIssuers: eligibleBanksAndIssuers,
      eligibleNonNfrd: eligibleNonNfrd,
    },
    CreditKpiInsurance: {
      eligibleTradingPortfolio: eligibleNonLifeInsurance,
    },
    CreditKpiDualField: {
      eligibleTradingPortfolio: eligibleTradingPortfolio,
      eligibleOnDemandLoans: eligibleOnDemandLoans,
    },
    CreditKpiSingleField: {
      eligibleTradingPortfolio: eligibleTradingPortfolioAndLoans,
    },
    "Reporting Obligation": reportingObligation,
    Attestation: attestation,
  };
}

function generateCompanyWithEuTaxonomyDataForNonFinancials() {
  const companiesWithEuTaxonomyDataForNonFinancials = [];
  for (let id = 1; id <= 250; id++) {
    companiesWithEuTaxonomyDataForNonFinancials.push({
      companyInformation: generateCompanyInformation(),
      euTaxonomyDataForNonFinancials: generateEuTaxonomyDataForNonFinancials(),
    });
  }
  return companiesWithEuTaxonomyDataForNonFinancials;
}

function generateCompanyWithEuTaxonomyDataForFinancials() {
  const companiesWithEuTaxonomyDataForFinancials = [];
  for (let id = 1; id <= 250; id++) {
    companiesWithEuTaxonomyDataForFinancials.push({
      companyInformation: generateCompanyInformation(),
      euTaxonomyDataForNonFinancials: generateEuTaxonomyDataForFinancials(),
    });
  }
  return companiesWithEuTaxonomyDataForFinancials;
}

function getStockIndexValueForCsv(setStockIndexList: Array<string>, stockIndexToCheck: string) {
  return setStockIndexList.includes(stockIndexToCheck) ? "x" : "";
}

function getIdentifierValueForCsv(identifierArray: Array<Object>, identifierType: string) {
  const identifierObject: any = identifierArray.find((identifier: any) => {
    return identifier.identifierType === identifierType;
  });
  return identifierObject ? identifierObject.identifierValue : "";
}

function convertToPercentageString(value: number) {
  return (Math.round(value * 100 * 100) / 100).toFixed(2).replace(".", ",") + "%";
}

function decimalSeparatorConverter(value: number) {
  return value.toString().replace(".", ",");
}

function generateCSVData(companyInformationWithEuTaxonomyDataForNonFinancials: Array<Object>) {
  const mergedData = companyInformationWithEuTaxonomyDataForNonFinancials.map((element: any) => {
    return { ...element["companyInformation"], ...element["euTaxonomyDataForNonFinancials"] };
  });
  const dateOptions: any = { year: "numeric", month: "numeric", day: "numeric" };
  const dateLocale = "de-DE";

  const options = {
    fields: [
      { label: "Unternehmensname", value: "companyName" },
      { label: "Headquarter", value: "headquarters" },
      { label: "Sector", value: "sector" },
      { label: "Countrycode", value: "countryCode" },
      { label: "Market Capitalization EURmm", value: "marketCap" },
      {
        label: "Market Capitalization Date",
        value: (row: any) => new Date(row.reportingDateOfMarketCap).toLocaleDateString(dateLocale, dateOptions),
      },
      { label: "Total Revenue EURmm", value: (row: any) => decimalSeparatorConverter(row.Revenue.totalAmount) },
      { label: "Total CapEx EURmm", value: (row: any) => decimalSeparatorConverter(row.Capex.totalAmount) },
      { label: "Total OpEx EURmm", value: (row: any) => decimalSeparatorConverter(row.Opex.totalAmount) },
      { label: "Eligible Revenue", value: (row: any) => convertToPercentageString(row.Revenue.eligiblePercentage) },
      { label: "Eligible CapEx", value: (row: any) => convertToPercentageString(row.Capex.eligiblePercentage) },
      { label: "Eligible OpEx", value: (row: any) => convertToPercentageString(row.Opex.eligiblePercentage) },
      { label: "Aligned Revenue", value: (row: any) => convertToPercentageString(row.Revenue.alignedPercentage) },
      { label: "Aligned CapEx", value: (row: any) => convertToPercentageString(row.Capex.alignedPercentage) },
      { label: "Aligned OpEx", value: (row: any) => convertToPercentageString(row.Opex.alignedPercentage) },
      { label: "IS/FS", value: "companyType", default: "IS" },
      { label: "NFRD mandatory", value: (row: any) => row["Reporting Obligation"] },
      {
        label: "Assurance",
        value: (row: any) => {
          if (row["Attestation"] === "LimitedAssurance") {
            return "limited";
          } else if (row["Attestation"] === "ReasonableAssurance") {
            return "reasonable";
          } else {
            return "none";
          }
        },
      },
      ...stockIndexArray.map((e: any) => {
        return { label: humanizeString(e), value: (row: any) => getStockIndexValueForCsv(row.indices, e) };
      }),
      ...identifierTypeArray.map((e: any) => {
        return { label: humanizeString(e), value: (row: any) => getIdentifierValueForCsv(row.identifiers, e) };
      }),
    ],
    delimiter: ";",
  };
  return parse(mergedData, options);
}

function mainNonFinancial() {
  const companyInformationWithEuTaxonomyDataForNonFinancials = generateCompanyWithEuTaxonomyDataForNonFinancials();
  const csv = generateCSVData(companyInformationWithEuTaxonomyDataForNonFinancials);

  fs.writeFileSync("../testing/data/csvTestData.csv", csv);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForNonFinancials.json",
    JSON.stringify(companyInformationWithEuTaxonomyDataForNonFinancials, null, "\t")
  );
}

mainNonFinancial();

function mainFinancial() {
  const companyInformationWithEuTaxonomyDataForFinancials = generateCompanyWithEuTaxonomyDataForFinancials();
  const csv = generateCSVData(companyInformationWithEuTaxonomyDataForFinancials);

  fs.writeFileSync("../testing/data/csvTestData.csv", csv);
  fs.writeFileSync(
    "../testing/data/CompanyInformationWithEuTaxonomyDataForFinancials.json",
    JSON.stringify(companyInformationWithEuTaxonomyDataForFinancials, null, "\t")
  );
}

mainFinancial();
