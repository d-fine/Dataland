import { faker } from "@faker-js/faker";
import { CompanyInformation, CompanyIdentifier, CompanyIdentifierIdentifierTypeEnum } from "@clients/backend";
import { FixtureData } from "./GenerateFakeFixtures";
import { humanizeString } from "@/utils/StringHumanizer";
import { getIdentifierValueForCsv } from "./CsvUtils";

export function generateCompanyInformation(): CompanyInformation {
  const companyName = faker.company.name();
  const headquarters = faker.address.city();
  const sector = faker.company.bsNoun();
  const industry = faker.company.bsNoun();
  const currency = "EUR";
  const marketCap = faker.mersenne.rand(10000000, 50000);
  const reportingDateOfMarketCap = faker.date.past().toISOString().split("T")[0];
  const numberOfShares = faker.mersenne.rand(1000000, 10000);
  const sharePrice = faker.mersenne.rand(1000, 1);
  const numberOfEmployees = faker.mersenne.rand(1000000, 10000);

  const identifiers: Array<CompanyIdentifier> = faker.helpers
    .arrayElements([
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.Lei,
        identifierValue: faker.random.alphaNumeric(20),
      },
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.Isin,
        identifierValue: faker.random.alphaNumeric(12),
      },
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.PermId,
        identifierValue: faker.random.alphaNumeric(12),
      },
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.Ticker,
        identifierValue: faker.random.alphaNumeric(12),
      },
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.DunsNumber,
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
    industry: industry,
    currency: currency,
    marketCap: marketCap,
    reportingDateOfMarketCap: reportingDateOfMarketCap,
    numberOfShares: numberOfShares,
    sharePrice: sharePrice,
    numberOfEmployees: numberOfEmployees,
    identifiers: identifiers,
    countryCode: countryCode,
    isTeaserCompany: false,
  };
}

export function getCsvCompanyMapping<T>() {
  const dateOptions: any = {
    year: "numeric",
    month: "numeric",
    day: "numeric",
  };
  const dateLocale = "de-DE";

  return [
    {
      label: "Company Name",
      value: (row: FixtureData<T>) => row.companyInformation.companyName,
    },
    {
      label: "Headquarter",
      value: (row: FixtureData<T>) => row.companyInformation.headquarters,
    },
    {
      label: "Sector",
      value: (row: FixtureData<T>) => row.companyInformation.sector,
    },
    {
      label: "Industry",
      value: (row: FixtureData<T>) => row.companyInformation.industry,
    },
    {
      label: "Country Code",
      value: (row: FixtureData<T>) => row.companyInformation.countryCode,
    },
    {
      label: "Market Capitalization",
      value: (row: FixtureData<T>) => row.companyInformation.marketCap,
    },
    {
      label: "Market Capitalization Date",
      value: (row: FixtureData<T>) => {
        const date = row.companyInformation.reportingDateOfMarketCap;
        return date !== null && date !== undefined && date !== ""
          ? new Date(date).toLocaleDateString(dateLocale, dateOptions)
          : "";
      },
    },
    {
      label: "Teaser Company",
      value: (row: FixtureData<T>) => (row.companyInformation.isTeaserCompany ? "Yes" : "No"),
    },
    {
      label: "Number of Employees",
      value: (row: FixtureData<T>) => row.companyInformation.numberOfEmployees,
    },
    {
      label: "Number Of Shares",
      value: (row: FixtureData<T>) => row.companyInformation.numberOfShares,
    },
    {
      label: "Share Price",
      value: (row: FixtureData<T>) => row.companyInformation.sharePrice,
    },
    {
      label: "Currency",
      value: (row: FixtureData<T>) => row.companyInformation.currency,
    },
    ...Object.values(CompanyIdentifierIdentifierTypeEnum).map((e) => {
      return {
        label: humanizeString(e),
        value: (row: FixtureData<T>) => getIdentifierValueForCsv(row.companyInformation.identifiers, e),
      };
    }),
  ];
}
