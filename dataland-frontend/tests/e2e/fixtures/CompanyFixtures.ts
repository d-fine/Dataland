import { faker } from "@faker-js/faker";
import {
  CompanyInformation,
  CompanyIdentifier,
  CompanyIdentifierIdentifierTypeEnum,
  CompanyInformationIndicesEnum,
} from "../../../build/clients/backend/api";
import { JSONSet } from "./Utils";
import { FixtureData } from "./GenerateFakeFixtures";
import { humanizeString } from "../../../src/utils/StringHumanizer";
import { getIdentifierValueForCsv, getStockIndexValueForCsv } from "./CsvUtils";

export function generateCompanyInformation(): CompanyInformation {
  const companyName = faker.company.name();
  const headquarters = faker.address.city();
  const sector = faker.company.bsNoun();
  const marketCap = faker.mersenne.rand(10000000, 50000);
  const reportingDateOfMarketCap = faker.date.past().toISOString().split("T")[0];
  const indices = faker.helpers.arrayElements(Object.values(CompanyInformationIndicesEnum));

  const identifiers: Array<CompanyIdentifier> = faker.helpers
    .arrayElements([
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.Lei,
        identifierValue: faker.random.alphaNumeric(12),
      },
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.Isin,
        identifierValue: faker.random.alphaNumeric(12),
      },
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.PermId,
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
    indices: new JSONSet(indices),
    identifiers: identifiers,
    countryCode: countryCode,
  };
}

export function getCsvCompanyMapping<T>() {
  const dateOptions: any = { year: "numeric", month: "numeric", day: "numeric" };
  const dateLocale = "de-DE";

  return [
    {
      label: "Unternehmensname",
      value: (row: FixtureData<T>) => row.companyInformation.companyName,
    },
    {
      label: "Headquarter",
      value: (row: FixtureData<T>) => row.companyInformation.headquarters,
    },
    { label: "Sector", value: (row: FixtureData<T>) => row.companyInformation.sector },
    {
      label: "Countrycode",
      value: (row: FixtureData<T>) => row.companyInformation.countryCode,
    },
    {
      label: "Market Capitalization EURmm",
      value: (row: FixtureData<T>) => row.companyInformation.marketCap,
    },
    {
      label: "Market Capitalization Date",
      value: (row: FixtureData<T>) =>
        new Date(row.companyInformation.reportingDateOfMarketCap).toLocaleDateString(dateLocale, dateOptions),
    },
    ...Object.values(CompanyInformationIndicesEnum).map((e) => {
      return {
        label: humanizeString(e),
        value: (row: FixtureData<T>) => getStockIndexValueForCsv(row.companyInformation.indices, e),
      };
    }),
    ...Object.values(CompanyIdentifierIdentifierTypeEnum).map((e) => {
      return {
        label: humanizeString(e),
        value: (row: FixtureData<T>) => getIdentifierValueForCsv(row.companyInformation.identifiers, e),
      };
    }),
  ];
}
