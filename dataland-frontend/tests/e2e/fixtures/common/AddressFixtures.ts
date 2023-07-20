import { faker } from "@faker-js/faker";
import { generateIso2CountryCode } from "@e2e/fixtures/common/CountryFixtures";
import { Address } from "@clients/backend";

/**
 * Generates a random address
 * @returns a random address
 */
export function generateAddress(): Address {
  return {
    streetAndHouseNumber: faker.location.street() + " " + faker.location.buildingNumber(),
    city: faker.location.city(),
    state: faker.location.state(),
    postalCode: faker.location.zipCode(),
    country: generateIso2CountryCode(),
  };
}
