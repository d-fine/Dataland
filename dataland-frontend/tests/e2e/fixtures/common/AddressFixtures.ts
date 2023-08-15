import { faker } from "@faker-js/faker";
import { getRandomIso2CountryCode } from "@e2e/fixtures/common/CountryFixtures";
import { Address } from "@clients/backend";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";

/**
 * Generates a random address
 * @param undefinedProbability the probability for "undefined" values in nullable fields
 * @returns a random address
 */
export function generateAddress(undefinedProbability = 0.5): Address {
  return {
    streetAndHouseNumber: valueOrUndefined(
      faker.location.street() + " " + faker.location.buildingNumber(),
      undefinedProbability,
    ),
    postalCode: valueOrUndefined(faker.location.zipCode(), undefinedProbability),
    city: faker.location.city(),
    state: valueOrUndefined(faker.location.state(), undefinedProbability),
    country: getRandomIso2CountryCode(),
  };
}
