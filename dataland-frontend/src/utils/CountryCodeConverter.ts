import countries from "i18n-iso-countries";
import countriesEn from "i18n-iso-countries/langs/en.json";
countries.registerLocale(countriesEn);

/**
 * Returns the english name of the country identified by its country code
 * @param countryCode the country code of the country to lookup the name for
 * @returns the english name of the country identified by countryCode
 */
export function getCountryNameFromCountryCode(countryCode: string): string {
  return countries.getName(countryCode, "en");
}

/**
 * Returns country identifiers
 * @returns the countryCodes
 */
export function getAllCountryCodes(): Array<string> {
  return Object.keys(countries.getNames("en")).sort();
}

/**
 * Returns the english names and country identifiers
 * @returns the english names of the countries and the countryCodes
 */
export function getAllCountryNamesWithCodes(): { [alpha2CountryCode: string]: string } {
  return countries.getNames("en");
}
