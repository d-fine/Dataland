import { faker } from "@faker-js/faker/locale/de";
import { CompanyIdentifierIdentifierTypeEnum, CompanyInformation } from "@clients/backend";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";

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
  return legalForms[faker.number.int(legalForms.length - 1)];
}

/**
 * Generates a company fixture with random information
 * @returns information about a randomly generated company
 */
export function generateCompanyInformation(): CompanyInformation {
  return {
    companyName: faker.company.name(),
    headquarters: faker.location.city(),
    headquartersPostalCode: valueOrUndefined(faker.location.zipCode()),
    sector: faker.company.buzzNoun(),
    identifiers: faker.helpers
      .arrayElements([
        {
          identifierType: CompanyIdentifierIdentifierTypeEnum.Lei,
          identifierValue: faker.string.alphanumeric(20),
        },
        {
          identifierType: CompanyIdentifierIdentifierTypeEnum.Isin,
          identifierValue: faker.string.alphanumeric(12),
        },
        {
          identifierType: CompanyIdentifierIdentifierTypeEnum.PermId,
          identifierValue: faker.string.alphanumeric(10),
        },
        {
          identifierType: CompanyIdentifierIdentifierTypeEnum.Ticker,
          identifierValue: faker.string.alphanumeric(7),
        },
        {
          identifierType: CompanyIdentifierIdentifierTypeEnum.Duns,
          identifierValue: faker.string.alphanumeric(9),
        },
        {
          identifierType: CompanyIdentifierIdentifierTypeEnum.VatNumber,
          identifierValue: faker.string.alphanumeric(9),
        },
        {
          identifierType: CompanyIdentifierIdentifierTypeEnum.CompanyRegistrationNumber,
          identifierValue: faker.string.alphanumeric(15),
        },
      ])
      .sort((a, b) => {
        return a.identifierType.localeCompare(b.identifierType);
      }),
    countryCode: faker.location.countryCode(),
    companyAlternativeNames: Array.from({ length: faker.number.int({ min: 0, max: 4 }) }, () => {
      return faker.company.name();
    }).sort((a, b) => a.localeCompare(b)),
    companyLegalForm: valueOrUndefined(getRandomCompanyLegalForm()),
    website: valueOrUndefined(faker.internet.url()),
    isTeaserCompany: false,
  };
}
