import { getAllCountryNamesWithCodes } from "@/utils/CountryCodeConverter";
import currencyCodeData from "currency-codes/data";

export interface DropdownOption {
  label: string;
  value: string;
}
export enum DropdownDatasetIdentifier {
  CountryCodes = "ISO 2 Codes",
  CurrencyCodes = "ISO 4217 Codes",
}

export type DropdownDataset = Array<DropdownOption>;

/**
 * Retrieves a pre-defined dataset for the dropdown components by its identifier
 *
 * @param datasetIdentifier the identifier of the common dataset to retrieve
 * @returns the dataset
 */
export function getDataset(datasetIdentifier: DropdownDatasetIdentifier): DropdownDataset {
  switch (datasetIdentifier) {
    case DropdownDatasetIdentifier.CountryCodes:
      return getCountryCodeDropdownDataset();
    case DropdownDatasetIdentifier.CurrencyCodes:
      return getCurrencyCodeDropdownDataset();
  }
  throw Error(`Unknown dataset identifier ${datasetIdentifier as string}`);
}

/**
 * Retrieves a dropdown dataset of currency codes
 *
 * @returns a dropdown dataset of currency codes
 */
function getCurrencyCodeDropdownDataset(): DropdownDataset {
  const currencyCodeDataset: DropdownDataset = [];
  currencyCodeData.forEach((it) => {
    currencyCodeDataset.push({
      label: `${it.currency} (${it.code})`,
      value: it.code,
    });
  });
  return currencyCodeDataset;
}

/**
 * Retrieves a dropdown dataset of country codes
 *
 * @returns a dropdown dataset of country codes
 */
function getCountryCodeDropdownDataset(): DropdownDataset {
  const datasetAsMap = getAllCountryNamesWithCodes();
  const countryCodeDataset: DropdownDataset = [];
  Object.keys(datasetAsMap).forEach((it) => {
    countryCodeDataset.push({
      label: `${datasetAsMap[it]} (${it})`,
      value: it,
    });
  });
  return countryCodeDataset;
}
