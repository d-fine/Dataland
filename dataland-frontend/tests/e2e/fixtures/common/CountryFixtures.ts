/**
 * Randomly returns a country from "BE", "DE", "FR", "IT", "LU", "PL", "PT", "ES"
 * @returns the randomly chosen country code
 */
export function generateIso2CountryCode(): string {
  const someCommonIso2CountryCodes = ["BE", "DE", "FR", "IT", "LU", "PL", "PT", "ES"];
  return someCommonIso2CountryCodes[Math.floor(Math.random() * someCommonIso2CountryCodes.length)];
}
