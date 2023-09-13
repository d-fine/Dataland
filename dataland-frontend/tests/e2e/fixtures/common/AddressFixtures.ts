import { faker } from "@faker-js/faker";
import { getRandomIso2CountryCode } from "@e2e/fixtures/common/CountryFixtures";
import { type Address } from "@clients/backend";
import { DEFAULT_PROBABILITY, valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";

/**
 * Generates a random address
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns a random address
 */
export function generateAddress(undefinedProbability = DEFAULT_PROBABILITY): Address {
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
