import { EuTaxonomyDataForFinancialsFinancialServicesTypesEnum } from "../../../build/clients/backend/api";

export function getAttestation(attestation: string) {
  if (attestation === "LimitedAssurance") {
    return "limited";
  } else if (attestation === "ReasonableAssurance") {
    return "reasonable";
  } else {
    return "none";
  }
}

export function getCompanyType(type: EuTaxonomyDataForFinancialsFinancialServicesTypesEnum): string {
  switch (type) {
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement:
      return "Asset Management Company";
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution:
      return "Credit Institution";
    case EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance:
      return "Insurance/Reinsurance";
  }
  throw Error(`Unknown FS type ${type}`);
}

export function decimalSeparatorConverter(value: number | undefined): string {
  return value === undefined ? "" : value.toString().replace(".", ",");
}
export function convertToPercentageString(value: number | undefined): string {
  if (value === undefined) return "";
  const valueRounded = parseFloat((Math.round(value * 100 * 100) / 100).toFixed(2))
    .toString()
    .replace(".", ",");

  return `${valueRounded}%`;
}

export function getStockIndexValueForCsv(
  setStockIndexList: Set<string> | undefined,
  stockIndexToCheck: string
): string {
  return setStockIndexList && setStockIndexList.has(stockIndexToCheck) ? "x" : "";
}

export function getIdentifierValueForCsv(identifierArray: Array<Object>, identifierType: string): string {
  const identifierObject: any = identifierArray.find((identifier: any) => {
    return identifier.identifierType === identifierType;
  });
  return identifierObject ? identifierObject.identifierValue : "";
}
