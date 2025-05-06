import { faker } from '@faker-js/faker';
import { type DataMetaInformation, DataTypeEnum, QaStatus } from '@clients/backend';
import { generateBoolean, generateInt } from '@e2e/fixtures/common/NumberFixtures';
import { Generator } from '@e2e/utils/FakeFixtureUtils';
import { pickOneElement } from '@e2e/fixtures/FixtureUtils';

export class DataMetaInformationGenerator extends Generator {
  /**
   * Generates a random ID
   * @returns a random ID
   */
  generateId(): string {
    return faker.string.uuid();
  }

  /**
   * Generates a random reporting period as one of the recent years
   * @returns a random reporting period
   */
  generateReportingPeriodRecentYear(): string {
    return pickOneElement(['2023', '2022', '2021', '2020']);
  }

  /**
   * Generates a random data meta information
   * @param dataType the type of the associated data
   * @param canSeeUploader should the uploaderUserId field be set
   * @returns a random data meta information
   */
  generateDataMetaInformation(dataType?: DataTypeEnum, canSeeUploader = false): DataMetaInformation {
    const currentlyActive = generateBoolean();
    return {
      dataId: this.generateId(),
      companyId: this.generateId(),
      dataType: dataType ?? pickOneElement(Object.values(DataTypeEnum)),
      reportingPeriod: this.generateReportingPeriodRecentYear(),
      currentlyActive: currentlyActive,
      qaStatus: currentlyActive ? QaStatus.Accepted : pickOneElement(Object.values(QaStatus)),
      uploadTime: generateInt(),
      uploaderUserId: canSeeUploader ? this.generateId() : null,
      ref: 'https://dataland.com/',
    };
  }
}
