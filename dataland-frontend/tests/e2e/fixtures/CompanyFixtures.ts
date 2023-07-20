import { faker } from "@faker-js/faker/locale/de";
import { CompanyInformation } from "@clients/backend";
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
 * Generates a random set of unique identifiers
 * @returns a random set of identifiers
 */
function getRandomIdentifiers(): { [p: string]: string[] } {
  const identifiers: { [p: string]: string[] } = {};
  identifiers["Lei"] = faker.helpers.arrayElements([faker.string.alphanumeric(20)], { min: 0, max: 1 });
  identifiers["Isin"] = faker.helpers.arrayElements([faker.string.alphanumeric(12), faker.string.alphanumeric(12)], {
    min: 0,
    max: 2,
  });
  identifiers["PermId"] = faker.helpers.arrayElements([faker.string.alphanumeric(10)], { min: 0, max: 1 });
  identifiers["Ticker"] = faker.helpers.arrayElements([faker.string.alphanumeric(7)], { min: 0, max: 1 });
  identifiers["Duns"] = faker.helpers.arrayElements([faker.string.alphanumeric(9)], { min: 0, max: 1 });
  identifiers["VatNumber"] = faker.helpers.arrayElements([faker.string.alphanumeric(9)], { min: 0, max: 1 });
  identifiers["CompanyRegistrationNumber"] = faker.helpers.arrayElements([faker.string.alphanumeric(15)], {
    min: 0,
    max: 1,
  });
  return identifiers;
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
    sector: valueOrUndefined(faker.company.buzzNoun()),
    identifiers: getRandomIdentifiers(),
    countryCode: faker.location.countryCode(),
    companyAlternativeNames: Array.from({ length: faker.number.int({ min: 0, max: 4 }) }, () => {
      return faker.company.name();
    }).sort((a, b) => a.localeCompare(b)),
    companyLegalForm: valueOrUndefined(getRandomCompanyLegalForm()),
    website: valueOrUndefined(faker.internet.url()),
    isTeaserCompany: false,
  };
}
