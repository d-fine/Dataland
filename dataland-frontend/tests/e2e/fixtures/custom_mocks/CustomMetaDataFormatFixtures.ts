import { type DataMetaInformation, DataTypeEnum } from "@clients/backend";

type MetaInfoAssociatedWithReportingPeriodByDataType = { [key in DataTypeEnum]?: (string | DataMetaInformation)[][] };

/**
 * Extracts data meta information with data type "EU taxonomy for financials" and "LkSG" and stores them in a custom format
 * @param listOfMetaInformationForOneCompany the list of data meta information to parse
 * @returns the generated structure
 */
export function extractMetaInfoForEuFinancialsAndLksg(
  listOfMetaInformationForOneCompany: DataMetaInformation[],
): MetaInfoAssociatedWithReportingPeriodByDataType {
  const holdingObject: MetaInfoAssociatedWithReportingPeriodByDataType = {};
  [DataTypeEnum.EutaxonomyFinancials, DataTypeEnum.Lksg].forEach((dataType) => {
    holdingObject[dataType] = listOfMetaInformationForOneCompany
      .filter((metaInfo) => metaInfo.dataType == dataType)
      .map((metaInfo) => [metaInfo.reportingPeriod, metaInfo]);
  });
  return holdingObject;
}
