import {faker} from "@faker-js/faker";

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
 * Generates an array consisting of 1 to 8 random country codes
 *
 * @returns 1 to 8 random country codes
 */
export function generateListOfIso2CountryCodes(): string[] {
  const listOfCountryCodes = Array.from(
      { length: faker.datatype.number({ min: 0, max: 8 }) },
      generateIso2CountryCode
  );
  return listOfCountryCodes
}

/**
 * Generates an array consisting of 1 to 8 random country codes or undefined
 *
 * @returns 1 to 8 random country codes or undefined
 */
export function generateListOfIso2CountryCodesOrUndefined(): string[] | undefined {
  const listOfCountryCodes = Array.from(
      { length: faker.datatype.number({ min: 0, max: 8 }) },
      generateIso2CountryCode
  );
  return faker.datatype.boolean() ? listOfCountryCodes : undefined;
}
