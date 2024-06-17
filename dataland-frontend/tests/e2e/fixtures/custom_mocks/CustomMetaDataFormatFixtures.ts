import { type DataMetaInformation, DataTypeEnum } from '@clients/backend';

type MetaInfoAssociatedWithReportingPeriodByDataType = { [key in DataTypeEnum]?: (string | DataMetaInformation)[][] };

/**
 * Creates an object that is used on the framework view page to hold data meta info
 * @param metaInfoDataForOneCompany the underlying data meta info to build the object
 * @returns the object
 */
export function extractMetaInfoAssociatedWithReportingPeriodByDataType(
  metaInfoDataForOneCompany: DataMetaInformation[]
): MetaInfoAssociatedWithReportingPeriodByDataType {
  const holdingObject: MetaInfoAssociatedWithReportingPeriodByDataType = {};
  [DataTypeEnum.EutaxonomyFinancials, DataTypeEnum.Lksg].forEach((dataType) => {
    holdingObject[dataType] = metaInfoDataForOneCompany
      .filter((metaInfo) => metaInfo.dataType == dataType)
      .map((metaInfo) => [metaInfo.reportingPeriod, metaInfo]);
  });
  return holdingObject;
}
