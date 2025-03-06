import { type DataMetaInformation, DataTypeEnum, QaStatus } from '@clients/backend';
import { DataMetaInformationGenerator } from '@e2e/fixtures/data_meta_information/DataMetaInformationFixtures';
import { faker } from '@faker-js/faker';
import { range } from '@/utils/ArrayUtils';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation.ts';

type MetaInfoAssociatedWithReportingPeriodByDataType = { [key in DataTypeEnum]?: (string | DataMetaInformation)[][] };

/**
 * Generates a list of data meta information for some data types
 * @returns a lost of data meta information
 */
export function generateMetaInfoDataForOneCompany(): DataMetaInformation[] {
  const listOfMetaInfo: DataMetaInformation[] = [];
  const metaInfoGenerator = new DataMetaInformationGenerator();
  const companyId = faker.string.uuid();
  let yearCounter = 2014;

  /**
   * Generates data meta information for active and by QA accepted data and adds it to the collecting list
   * @param dataType the data type for the meta information
   */
  function generateActiveMetaInfoWithTypeAndAppend(dataType: DataTypeEnum): void {
    const metaInfo = metaInfoGenerator.generateDataMetaInformation(dataType);
    yearCounter++;
    metaInfo.companyId = companyId;
    metaInfo.reportingPeriod = yearCounter.toString();
    metaInfo.currentlyActive = true;
    metaInfo.qaStatus = QaStatus.Accepted;
    listOfMetaInfo.push(metaInfo);
  }

  range(2).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.EutaxonomyFinancials));
  range(4).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.EutaxonomyNonFinancials));
  range(2).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.Lksg));
  range(1).forEach(() => generateActiveMetaInfoWithTypeAndAppend(DataTypeEnum.Sfdr));

  return listOfMetaInfo;
}

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

/**
 *
 * @param metaInfo
 * @param dataset
 */
export function createDataAndMetaInfoMock<T extends FrameworkData>(
  metaInfo: DataMetaInformation,
  dataset: T
): DataAndMetaInformation<T> {
  return { metaInfo: metaInfo, data: dataset } as DataAndMetaInformation<T>;
}
