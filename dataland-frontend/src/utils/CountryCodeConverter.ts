import countries from "i18n-iso-countries";
import countriesEn from "i18n-iso-countries/langs/en.json";
countries.registerLocale(countriesEn);

/**
 * Returns the english name of the country identified by its country code
 *
 * @param countryCode the country code of the country to lookup the name for
 * @returns the english name of the country identified by countryCode
 */
export function getCountryNameFromCountryCode(countryCode: string): string {
  return countries.getName(countryCode, "en");
}

export function getAllCountryCodes(): Array<string> {
  return Object.keys(countries.getNames("en")).sort();
}
