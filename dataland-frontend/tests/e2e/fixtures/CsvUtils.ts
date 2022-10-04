import {
  AssuranceDataAssuranceEnum,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  YesNo,
  YesNoNa,
} from "@clients/backend";
import Big from "big.js";
import { humanizeString } from "@/utils/StringHumanizer";

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
  throw Error(`Unknown assurance type ${assurance}`);
}

export function getFiscalYearDeviation(isdeviation: YesNo | undefined): string | undefined {
  switch (isdeviation) {
    case undefined:
    case null:
      return undefined;
    case YesNoNa.Yes:
      return "Deviation";
    case YesNoNa.No:
      return "No Deviation";
  }
  throw Error(`Unknown yesno type ${isdeviation}`);
}

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
  throw Error(`Unknown FS type ${type}`);
}

export function humanizeOrUndefined(stringToHumanise: string | undefined): string | undefined {
  if (stringToHumanise == undefined) return undefined;
  return humanizeString(stringToHumanise!!);
}

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
  throw Error(`Unknown FS type ${type}`);
}

export function decimalSeparatorConverter(scaleFactor: number): (value: number | undefined) => string {
  return (value) => {
    return value === undefined ? "" : Big(value).div(Big(scaleFactor)).toString().replace(".", ",");
  };
}

export function convertToPercentageString(value: number | undefined): string {
  if (value === undefined) return "";
  const valueRounded = parseFloat((Math.round(value * 100 * 100) / 100).toFixed(2))
    .toString()
    .replace(".", ",");

  return `${valueRounded}%`;
}

export function getIdentifierValueForCsv(identifierArray: Array<Object>, identifierType: string): string {
  const identifierObject: any = identifierArray.find((identifier: any) => {
    return identifier.identifierType === identifierType;
  });
  return identifierObject ? identifierObject.identifierValue : "";
}
