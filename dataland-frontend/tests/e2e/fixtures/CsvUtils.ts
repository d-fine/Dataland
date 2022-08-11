import { EuTaxonomyDataForFinancialsFinancialServicesTypeEnum } from "../../../build/clients/backend/api";

export function getAttestation(attestation: string) {
  if (attestation === "LimitedAssurance") {
    return "limited";
  } else if (attestation === "ReasonableAssurance") {
    return "reasonable";
  } else {
    return "none";
  }
}

export function getCompanyType(type: EuTaxonomyDataForFinancialsFinancialServicesTypeEnum): string {
  switch (type) {
    case EuTaxonomyDataForFinancialsFinancialServicesTypeEnum.AssetManagement:
      return "Asset Management Company";
    case EuTaxonomyDataForFinancialsFinancialServicesTypeEnum.CreditInstitution:
      return "Credit Institution";
    case EuTaxonomyDataForFinancialsFinancialServicesTypeEnum.InsuranceOrReinsurance:
      return "Insurance/Reinsurance";
  }
}

export function decimalSeparatorConverter(value: number | undefined): string {
  return value === undefined ? "" : value.toString().replace(".", ",");
}
export function convertToPercentageString(value: number | undefined): string {
  return value === undefined ? "" : (Math.round(value * 100 * 100) / 100).toFixed(2).replace(".", ",") + "%";
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
