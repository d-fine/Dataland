import { faker } from "@faker-js/faker";

/**
 * Randomly returns a country from "BE", "DE", "FR", "IT", "LU", "PL", "PT", "ES"
 *
 * @returns the randomly chosen country code
 */
export function generateIso2CountryCode(): string {
  const someCommonIso2CountryCodes = ["BE", "DE", "FR", "IT", "LU", "PL", "PT", "ES"];
  return someCommonIso2CountryCodes[Math.floor(Math.random() * someCommonIso2CountryCodes.length)];
}

/**
 * Randomly returns a list of country codes
 *
 * @returns the randomly generated list of country codes
 */
export function generateListOfIso2CountryCodes(): string[] {
  return Array.from({ length: faker.datatype.number({ min: 0, max: 5 }) }, () => {
    return generateIso2CountryCode();
  });
}
