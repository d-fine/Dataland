import { faker } from "@faker-js/faker";
import { getRandomIso2CountryCode } from "@e2e/fixtures/common/CountryFixtures";
import { Address } from "@clients/backend";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";

/**
 * Generates a random address
 * @param undefinedProbability
 * @returns a random address
 */
export function generateAddress(undefinedProbability = 0.5): Address {
  return {
    streetAndHouseNumber: valueOrUndefined(faker.location.street() + " " + faker.location.buildingNumber()),
    city: faker.location.city(),
    state: valueOrUndefined(faker.location.state()),
    postalCode: valueOrUndefined(faker.location.zipCode()),
    country: getRandomIso2CountryCode(),
  };
}
