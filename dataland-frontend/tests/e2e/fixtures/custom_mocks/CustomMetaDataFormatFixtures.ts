import { type DataMetaInformation, DataTypeEnum } from "@clients/backend";
import { generateMetaInfoDataForOneCompany } from "@e2e/fixtures/custom_mocks/DataMetaInformationFaker";

type MetaInfoAssociatedWithReportingPeriodByDataType = { [key in DataTypeEnum]?: (string | DataMetaInformation)[][] };

/**
 * Creates an object TODO
 * @returns the generated structure
 */
export function generateMetaInfoAssociatedWithReportingPeriodByDataType(): MetaInfoAssociatedWithReportingPeriodByDataType {
  const holdingObject: MetaInfoAssociatedWithReportingPeriodByDataType = {};
  [DataTypeEnum.EutaxonomyFinancials, DataTypeEnum.Lksg].forEach((dataType) => {
    holdingObject[dataType] = generateMetaInfoDataForOneCompany()
      .filter((metaInfo) => metaInfo.dataType == dataType) // TODO: Think about this
      .map((metaInfo) => [metaInfo.reportingPeriod, metaInfo]);
  });
  return holdingObject;
}
