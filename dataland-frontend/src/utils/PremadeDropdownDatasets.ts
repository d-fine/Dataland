import { getAllCountryNamesWithCodes } from '@/utils/CountryCodeConverter';
import currencyCodeData from 'currency-codes/data';
import { AreaAdjointness, ReleaseMedium, RiskPositionType, WasteClassifications } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';

export interface DropdownOption {
  label: string;
  value: string;
}
export enum DropdownDatasetIdentifier {
  CountryCodesIso2 = 'ISO 2 Codes',
  CurrencyCodes = 'ISO 4217 Codes',
  RiskPositions = 'Risk Positions',
  ReleaseMedium = 'Release Medium',
  WasteClassifications = 'Waste classifications',
  Adjointness = 'Adjointness',
}

export type DropdownDataset = Array<DropdownOption>;

/**
 * Retrieves a pre-defined dataset for the dropdown components by its identifier
 * @param datasetIdentifier the identifier of the common dataset to retrieve
 * @returns the dataset
 */
export function getDataset(datasetIdentifier: DropdownDatasetIdentifier): DropdownDataset {
  switch (datasetIdentifier) {
    case DropdownDatasetIdentifier.CountryCodesIso2:
      return getCountryCodeDropdownDataset();
    case DropdownDatasetIdentifier.CurrencyCodes:
      return getCurrencyCodeDropdownDataset();
    case DropdownDatasetIdentifier.RiskPositions:
      return getRiskPositionDropdownDataset();
    case DropdownDatasetIdentifier.ReleaseMedium:
      return getReleaseMediumDropdownDataset();
    case DropdownDatasetIdentifier.WasteClassifications:
      return getWasteClassificationsDropdownDataset();
    case DropdownDatasetIdentifier.Adjointness:
      return getAdjointnessDropdownDataset();
  }
  throw Error(`Unknown dataset identifier ${datasetIdentifier as string}`);
}

/**
 * Returns country list as a map
 * @param datasetIdentifier the identifier of the common dataset to retrieve
 * @returns the generated map
 */
export function getDatasetAsMap(datasetIdentifier: DropdownDatasetIdentifier): { [p: string]: string } {
  const mapOfDropdownOptions = new Map<string, string>();
  getDataset(datasetIdentifier).forEach((element) => mapOfDropdownOptions.set(element.value, element.label));
  return Object.fromEntries(mapOfDropdownOptions);
}

/**
 * Retrieves a dropdown dataset of lksg risk positions
 * @returns a dropdown dataset of risk positions
 */
function getRiskPositionDropdownDataset(): DropdownDataset {
  const riskPositionDataset: DropdownDataset = [];
  Object.keys(RiskPositionType).forEach((it) => {
    riskPositionDataset.push({
      label: humanizeStringOrNumber(it),
      value: it,
    });
  });
  return riskPositionDataset;
}

/**
 * Retrieves a dropdown dataset of currency codes
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

export enum ReportingPeriodTableActions {
  EditDataset = 'editDataset',
  ResolveRequest = 'resolveRequest',
  ReopenRequest = 'reOpenRequest',
}
export interface ReportingPeriodTableEntry {
  reportingPeriod: string;
  editUrl: string;
  dataRequestId?: string;
  actionOnClick?: ReportingPeriodTableActions;
  isClickable: boolean;
}
/**
 * Retrieves a dropdown dataset of vsme release medium
 * @returns a dropdown dataset of vsme release medium
 */
function getReleaseMediumDropdownDataset(): DropdownDataset {
  const releaseMediumDataset: DropdownDataset = [];
  Object.keys(ReleaseMedium).forEach((it) => {
    releaseMediumDataset.push({
      label: humanizeStringOrNumber(it),
      value: it,
    });
  });
  return releaseMediumDataset;
}

/**
 * Retrieves a dropdown dataset of vsme waste classification
 * @returns a dropdown dataset of vsme waste classification
 */
function getWasteClassificationsDropdownDataset(): DropdownDataset {
  const WasteClassificationDataset: DropdownDataset = [];
  Object.keys(WasteClassifications).forEach((it) => {
    WasteClassificationDataset.push({
      label: humanizeStringOrNumber(it),
      value: it,
    });
  });
  return WasteClassificationDataset;
}
/**
 * Retrieves a dropdown dataset of vsme adjointness
 * @returns a dropdown dataset of vsme adjointness
 */
function getAdjointnessDropdownDataset(): DropdownDataset {
  const adjointnessDataset: DropdownDataset = [];
  Object.keys(AreaAdjointness).forEach((it) => {
    adjointnessDataset.push({
      label: humanizeStringOrNumber(it),
      value: it,
    });
  });
  return adjointnessDataset;
}
