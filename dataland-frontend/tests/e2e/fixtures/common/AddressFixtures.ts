import { faker } from '@faker-js/faker';
import { generateIso2CountryCode } from '@e2e/fixtures/common/CountryFixtures';
import { type Address } from '@clients/backend';
import { DEFAULT_PROBABILITY, valueOrNull } from '@e2e/utils/FakeFixtureUtils';

/**
 * Generates a random address
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a random address
 */
export function generateAddress(nullProbability = DEFAULT_PROBABILITY): Address {
  return {
    streetAndHouseNumber: valueOrNull(faker.location.street() + ' ' + faker.location.buildingNumber(), nullProbability),
    postalCode: valueOrNull(faker.location.zipCode(), nullProbability),
    city: faker.location.city(),
    state: valueOrNull(faker.location.state(), nullProbability),
    country: generateIso2CountryCode(),
  };
}
