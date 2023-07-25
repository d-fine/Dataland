import { getAllCountryCodes } from "@/utils/CountryCodeConverter";
import { faker } from "@faker-js/faker";

const allIso2CountryCodes = getAllCountryCodes();

/**
 * Randomly returns one Iso2 country code from all available Iso2 country codes
 * @returns the randomly chosen country code
 */
export function getRandomIso2CountryCode(): string {
  return faker.helpers.arrayElement(allIso2CountryCodes);
}
