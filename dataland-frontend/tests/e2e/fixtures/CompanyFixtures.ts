import { faker } from "@faker-js/faker";
import { CompanyInformation, CompanyIdentifier, CompanyIdentifierIdentifierTypeEnum } from "@clients/backend";
import { FixtureData } from "./GenerateFakeFixtures";
import { humanizeString } from "@/utils/StringHumanizer";
import { getIdentifierValueForCsv } from "./CsvUtils";

export function generateCompanyInformation(): CompanyInformation {
  const companyName = faker.company.name();
  const headquarters = faker.address.city();
  const sector = faker.company.bsNoun();

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
    identifiers: identifiers,
    countryCode: countryCode,
    isTeaserCompany: false,
  };
}

export function getCsvCompanyMapping<T>() {
  return [
    {
      label: "Unternehmensname",
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
      label: "Countrycode",
      value: (row: FixtureData<T>) => row.companyInformation.countryCode,
    },
    {
      label: "Teaser Company",
      value: (row: FixtureData<T>) => (row.companyInformation.isTeaserCompany ? "Yes" : "No"),
    },
    ...Object.values(CompanyIdentifierIdentifierTypeEnum).map((e) => {
      return {
        label: humanizeString(e),
        value: (row: FixtureData<T>) => getIdentifierValueForCsv(row.companyInformation.identifiers, e),
      };
    }),
  ];
}
