import { faker } from "@faker-js/faker";
import { CompanyInformation, CompanyIdentifier, CompanyIdentifierIdentifierTypeEnum } from "@clients/backend";
import { DataPoint } from "./FixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
import { humanizeString } from "@/utils/StringHumanizer";
import { getIdentifierValueForCsv } from "./CsvUtils";
import { valueOrUndefined } from "./common/DataPointFixtures";

const legalForms = [
  "Public Limited Company (PLC)",
  "Private Limited Company (Ltd)",
  "Limited Liability Partnership (LLP)",
  "Partnership without Limited Liability",
  "Sole Trader",
  "GmbH",
  "AG",
  "GmbH & Co. KG",
];

/**
 * Randomly picks and returns a legal form from a list of available legal forms.
 * @returns a random legal form from the list as string
 */
export function getRandomCompanyLegalForm(): string {
  return legalForms[faker.datatype.number(legalForms.length - 1)];
}

/**
 * Generates a company fixture with random information
 * @returns information about a randomly generated company
 */
export function generateCompanyInformation(): CompanyInformation {
  const companyName = faker.company.name();
  const headquarters = faker.address.city();
  const headquartersPostalCode = valueOrUndefined(faker.address.zipCode());
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
      {
        identifierType: CompanyIdentifierIdentifierTypeEnum.CompanyRegistrationNumber,
        identifierValue: faker.random.alphaNumeric(15),
      },
    ])
    .sort((a, b) => {
      return a.identifierType.localeCompare(b.identifierType);
    });
  const countryCode = faker.address.countryCode();
  const companyAlternativeNames = Array.from({ length: faker.datatype.number({ min: 0, max: 4 }) }, () => {
    return faker.company.name();
  }).sort();
  const companyLegalForm = valueOrUndefined(getRandomCompanyLegalForm());
  const website = valueOrUndefined(faker.internet.url());

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
    website: website,
  };
}

/**
 * Returns the CSV mapping for the columns showing basic company information
 * @returns the static CSV mapping
 */
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
    {
      label: "Website",
      value: (row: FixtureData<T>): string | undefined => row.companyInformation.website,
    },
    ...Object.values(CompanyIdentifierIdentifierTypeEnum).map((identifiyerTypeAsString) => {
      return {
        label: humanizeString(identifiyerTypeAsString),
        value: (row: FixtureData<T>): string =>
          getIdentifierValueForCsv(row.companyInformation.identifiers, identifiyerTypeAsString),
      };
    }),
  ];
}
