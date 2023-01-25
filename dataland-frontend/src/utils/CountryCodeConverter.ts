import countries from "i18n-iso-countries";
import countriesEn from "i18n-iso-countries/langs/en.json";
countries.registerLocale(countriesEn);

export function getCountryNameFromCountryCode(countryCode: string): string {
  return countries.getName(countryCode, "en");
}

export function getAllCountryCodes(): Array<string> {
  return Object.keys(countries.getNames("en")).sort();
}
