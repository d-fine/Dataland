import { faker } from '@faker-js/faker';
import { type CompanyInformation } from '@clients/backend';
import { valueOrNull } from '@e2e/utils/FakeFixtureUtils';
import { pickSubsetOfElements, pickOneOrNoElement, pickOneElement, generateArray } from '@e2e/fixtures/FixtureUtils';

const legalForms = [
  'Public Limited Company (PLC)',
  'Private Limited Company (Ltd)',
  'Limited Liability Partnership (LLP)',
  'Partnership without Limited Liability',
  'Sole Trader',
  'GmbH',
  'AG',
  'GmbH & Co. KG',
];

/**
 * Generates a random set of unique identifiers
 * @returns a random set of identifiers
 */
function getRandomIdentifiers(): { [p: string]: string[] } {
  const identifiers: { [p: string]: string[] } = {};
  identifiers['Lei'] = pickOneOrNoElement([faker.string.alphanumeric(20)]);
  identifiers['Isin'] = pickSubsetOfElements([faker.string.alphanumeric(12), faker.string.alphanumeric(12)], 1, 2);
  identifiers['PermId'] = pickOneOrNoElement([faker.string.alphanumeric(10)]);
  identifiers['Ticker'] = pickOneOrNoElement([faker.string.alphanumeric(7)]);
  identifiers['Duns'] = pickOneOrNoElement([faker.string.alphanumeric(9)]);
  identifiers['VatNumber'] = pickOneOrNoElement([faker.string.alphanumeric(9)]);
  identifiers['CompanyRegistrationNumber'] = pickOneOrNoElement([faker.string.alphanumeric(15)]);
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
    headquartersPostalCode: valueOrNull(faker.location.zipCode()),
    sector: valueOrNull(faker.company.buzzNoun()),
    identifiers: getRandomIdentifiers(),
    countryCode: faker.location.countryCode(),
    companyContactDetails: valueOrNull(generateArray(() => faker.internet.email({ provider: 'example.com' }), 0)),
    companyAlternativeNames: Array.from({ length: faker.number.int({ min: 0, max: 4 }) }, () => {
      return faker.company.name();
    }).sort((a, b) => a.localeCompare(b)),
    companyLegalForm: valueOrNull(pickOneElement(legalForms)),
    website: valueOrNull(faker.internet.url()),
    isTeaserCompany: false,
  };
}
