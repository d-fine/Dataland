import { faker } from "@faker-js/faker";
import { CompanyInformation, CompanyIdentifier, CompanyIdentifierIdentifierTypeEnum } from "@clients/backend";
import { FixtureData, DataPoint } from "./FixtureUtils";
import { humanizeString } from "@/utils/StringHumanizer";
import { getIdentifierValueForCsv } from "./CsvUtils";

export function generateCompanyInformation(): CompanyInformation {
  const companyName = faker.company.name();
  const headquarters = faker.address.city();
  const headquartersPostalCode = faker.address.zipCode();
  const sector = faker.company.bsNoun();

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
        identifierValue: faker.random.alphaNumeric(10),
      },
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.Ticker,
        identifierValue: faker.random.alphaNumeric(7),
      },
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.Duns,
        identifierValue: faker.random.alphaNumeric(9),
      },
    ])
    .sort((a, b) => {
      return a.identifierType.localeCompare(b.identifierType);
    });
  const countryCode = faker.address.countryCode();
  const companyAlternativeNames = Array.from({ length: faker.datatype.number({ min: 0, max: 4 }) }, () => {
    return faker.company.name();
  });
  const companyLegalForm = faker.company.bsNoun();

  return {
    companyName: companyName,
    companyAlternativeNames: companyAlternativeNames,
    companyLegalForm: companyLegalForm,
    headquarters: headquarters,
    headquartersPostalCode: headquartersPostalCode,
    sector: sector,
    identifiers: identifiers,
    countryCode: countryCode,
    isTeaserCompany: false,
  };
}

export function getCsvCompanyMapping<T>(): Array<DataPoint<FixtureData<T>, string>> {
  return [
    {
      label: "Unternehmensname",
      value: (row: FixtureData<T>): string => row.companyInformation.companyName,
    },
    {
      label: "Alternative Names",
      value: (row: FixtureData<T>): string | undefined =>
        row.companyInformation.companyAlternativeNames?.map((name) => `"${name}"`).join(", "),
    },
    {
      label: "Company Legal Form",
      value: (row: FixtureData<T>): string | undefined => row.companyInformation.companyLegalForm,
    },
    {
      label: "Headquarter",
      value: (row: FixtureData<T>): string => row.companyInformation.headquarters,
    },
    {
      label: "Headquarter Postal Code",
      value: (row: FixtureData<T>): string | undefined => row.companyInformation.headquartersPostalCode,
    },
    {
      label: "Sector",
      value: (row: FixtureData<T>): string => row.companyInformation.sector,
    },
    {
      label: "Countrycode",
      value: (row: FixtureData<T>): string => row.companyInformation.countryCode,
    },
    {
      label: "Teaser Company",
      value: (row: FixtureData<T>): string => (row.companyInformation.isTeaserCompany ? "Yes" : "No"),
    },
    ...Object.values(CompanyIdentifierIdentifierTypeEnum).map((e) => {
      return {
        label: humanizeString(e),
        value: (row: FixtureData<T>): string => getIdentifierValueForCsv(row.companyInformation.identifiers, e),
      };
    }),
  ];
}
