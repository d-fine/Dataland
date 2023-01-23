import {
  AssuranceDataAssuranceEnum,
  CompanyIdentifier,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  FiscalYearDeviation,
} from "@clients/backend";
import Big from "big.js";
import { humanizeString } from "@/utils/StringHumanizer";

/**
 * A mapper from the AssuranceDataAssuranceEnum enum to the corresponding expected CSV string
 *
 * @param assurance the assurance enum to convert
 * @returns the converted assurance enum
 */
export function getAssurance(assurance: AssuranceDataAssuranceEnum | undefined): string | undefined {
  switch (assurance) {
    case undefined:
    case null:
      return undefined;
    case AssuranceDataAssuranceEnum.LimitedAssurance:
      return "limited";
    case AssuranceDataAssuranceEnum.ReasonableAssurance:
      return "reasonable";
    case AssuranceDataAssuranceEnum.None:
      return "none";
  }
  throw Error(`Unknown assurance type ${String(assurance)}`);
}

/**
 * A mapper from the FiscalYearDeviation enum to the corresponding expected CSV string
 *
 * @param isDeviation the fiscal year deviation to convert
 * @returns the converted fiscal year deviation
 */
export function getFiscalYearDeviation(isDeviation: FiscalYearDeviation | undefined): string | undefined {
  switch (isDeviation) {
    case undefined:
    case null:
      return undefined;
    case FiscalYearDeviation.Deviation:
      return "Deviation";
    case FiscalYearDeviation.NoDeviation:
      return "No Deviation";
  }
  throw Error(`Unknown fiscal year deviation type ${String(isDeviation)}`);
}

/**
 * A function that returns the header suffix of columns belonging to a specified financial services type
 *
 * @param type the financial services type to get the suffix for
 * @returns the suffix belonging to type
 */
export function getCompanyTypeHeader(type: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum): string {
  switch (type) {
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement:
      return "Asset Management Company";
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution:
      return "Credit Institution";
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance:
      return "Insurance/Reinsurance";
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm:
      return "Investment Firm";
  }
  throw Error(`Unknown FS type ${String(type)}`);
}

/**
 * Uses the humanizeString utils function to convert the input function (or returns undefined if the input is undefined)
 *
 * @param stringToHumanise the string to humanize (or undefined)
 * @returns the converted string (or undefined if the input is undefined)
 */
export function humanizeOrUndefined(stringToHumanise: string | undefined): string | undefined {
  if (stringToHumanise == undefined) return undefined;
  return humanizeString(stringToHumanise);
}

/**
 * A mapper from the financial services type enum to an integer for the CSV
 *
 * @param type the type to convert to an integer
 * @returns the integer corresponding to the enum value
 */
export function getCompanyTypeCsvValue(type: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum): number {
  switch (type) {
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution:
      return 1;
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance:
      return 2;
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement:
      return 3;
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm:
      return 4;
  }
  throw Error(`Unknown FS type ${String(type)}`);
}

/**
 * A higher level function that returns a converter that divides a given value by scaleFactor ensuring
 * the decimal separator is ","
 *
 * @param scaleFactor the number to device values by
 * @returns a function that devides input values by scaleFactor ensuring the decimal separator is ","
 */
export function decimalSeparatorConverter(scaleFactor: number): (value: number | undefined) => string {
  return (value) => {
    return value === undefined ? "" : Big(value).div(Big(scaleFactor)).toString().replace(".", ",");
  };
}

/**
 * A function that formats a decimal value to a percentage string in german CSV format
 * (i.e. 0.123 gets converted to 12,3%)
 *
 * @param value the value in [0,1] to convert
 * @returns the converted percentage string
 */
export function convertToPercentageString(value: number | undefined): string {
  if (value === undefined) return "";
  const valueRounded = parseFloat((Math.round(value * 100 * 100) / 100).toFixed(2))
    .toString()
    .replace(".", ",");

  return `${valueRounded}%`;
}

/**
 * Returns the first identifier of type identifierType from the identifierArray.
 * Returns an empty string if such an identifier does not exist
 *
 * @param identifierArray the identifier array to search in
 * @param identifierType the type of identifier to look for
 * @returns the found identifier or an empty string
 */
export function getIdentifierValueForCsv(identifierArray: Array<CompanyIdentifier>, identifierType: string): string {
  const identifierObject: CompanyIdentifier | undefined = identifierArray.find((identifier: CompanyIdentifier) => {
    return identifier.identifierType === identifierType;
  });
  return identifierObject ? identifierObject.identifierValue : "";
}
