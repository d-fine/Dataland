import { faker } from "@faker-js/faker";
import { generateIso2CountryCode } from "@e2e/fixtures/common/CountryFixtures";
import { type Address } from "@clients/backend";
import { DEFAULT_PROBABILITY, valueOrMissing } from "@e2e/utils/FakeFixtureUtils";

/**
 * Generates a random address
 * @param setMissingValuesToNull decides whether missing values are represented by "undefined" or "null"
 * @param missingValueProbability the probability (as number between 0 and 1) for missing values in optional fields
 * @returns a random address
 */
export function generateAddress(
  setMissingValuesToNull: boolean,
  missingValueProbability = DEFAULT_PROBABILITY,
): Address {
  return {
    streetAndHouseNumber: valueOrMissing(
      faker.location.street() + " " + faker.location.buildingNumber(),
      missingValueProbability,
      setMissingValuesToNull,
    ),
    postalCode: valueOrMissing(faker.location.zipCode(), missingValueProbability, setMissingValuesToNull),
    city: faker.location.city(),
    state: valueOrMissing(faker.location.state(), missingValueProbability, setMissingValuesToNull),
    country: generateIso2CountryCode(),
  };
}
