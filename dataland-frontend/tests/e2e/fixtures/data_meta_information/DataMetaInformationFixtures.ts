import { faker } from "@faker-js/faker";
import { type DataMetaInformation, DataTypeEnum, QaStatus } from "@clients/backend";
import { generateBoolean, generateInt } from "@e2e/fixtures/common/NumberFixtures";
import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { pickOneElement } from "@e2e/fixtures/FixtureUtils";

export class DataMetaInformationGenerator extends Generator {
  constructor(setMissingValuesToNull = false) {
    super(DEFAULT_PROBABILITY, setMissingValuesToNull);
  }

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
    return faker.string.uuid();
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
      uploadTime: generateInt(Number.MAX_SAFE_INTEGER),
      uploaderUserId: canSeeUploader ? this.generateId() : this.missingValue(),
    };
  }
}
