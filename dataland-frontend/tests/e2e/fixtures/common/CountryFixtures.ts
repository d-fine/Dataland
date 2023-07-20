import { getAllCountryCodes } from "@/utils/CountryCodeConverter";

const allIso2CountryCodes = getAllCountryCodes();

/**
 * Randomly returns one Iso2 country code from all available Iso2 country codes
 * @returns the randomly chosen country code
 */
export function getRandomIso2CountryCode(): string {
  const randomIndex = Math.floor(Math.random() * allIso2CountryCodes.length);
  return allIso2CountryCodes[randomIndex];
}
