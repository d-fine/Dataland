import { faker } from "@faker-js/faker";
import { generateIso2CountryCode } from "@e2e/fixtures/common/CountryFixtures";
import { type Address } from "@clients/backend";
import { DEFAULT_PROBABILITY, valueOrMissing } from "@e2e/utils/FakeFixtureUtils";

/**
 * Generates a random address
 * @param missingValueProbability the probability (as number between 0 and 1) for missing values in optional fields
 * @param setMissingValuesToNull controls if missing values should be undefined or null
 * @returns a random address
 */
export function generateAddress(
  missingValueProbability = DEFAULT_PROBABILITY,
  setMissingValuesToNull: boolean,
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
