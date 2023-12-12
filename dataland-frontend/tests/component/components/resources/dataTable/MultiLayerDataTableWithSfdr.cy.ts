import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import {
  type DataMetaInformation,
  type DataAndMetaInformationSfdrData,
  DataTypeEnum,
  QaStatus,
  type SfdrData,
} from "@clients/backend";

import { convertDataModelToMLDTConfig } from "@/components/resources/dataTable/conversion/MultiLayerDataTableConfigurationConverter";
import { sfdrDataModel } from "@/components/resources/frameworkDataSearch/sfdr/SfdrDataModel";
import { type MLDTConfig } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import {
  mountMLDTFrameworkPanelFromFakeFixture,
  mountMLDTFrameworkPanel,
} from "@ct/testUtils/MultiLayerDataTableComponentTestUtils";
import { getCellValueContainer } from "@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils";

describe("Component tests for SfdrPanel", () => {
  let preparedFixtures: Array<FixtureData<SfdrData>>;
  const sfdrDisplayConfiguration = convertDataModelToMLDTConfig(sfdrDataModel) as MLDTConfig<SfdrData>;
  before(function () {
    cy.fixture("CompanyInformationWithSfdrPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<SfdrData>>;
    });
  });

  it("Check SFDR view page for company with one SFDR data set works and displays the fiscal year end correctly", () => {
    const preparedFixture = getPreparedFixture("companyWithOneFilledSfdrSubcategory", preparedFixtures);
    const sfdrData = preparedFixture.t;
    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Sfdr, sfdrDisplayConfiguration, [preparedFixture]);

    getCellValueContainer("Fiscal Year End").should("contain.text", sfdrData.general.general.fiscalYearEnd);
  });

  /**
   * This functions imitates an api response of the /data/sfdr/companies/mock-company-id endpoint
   * to include 6 active Sfdr datasets from different years to test the simultaneous display of multiple Sfdr
   * datasets (constructed datasets range from 2023 to 2028)
   * @param baseDataset the SFDR dataset used as a basis for constructing the 6 mocked ones
   * @returns a mocked api response
   */
  function constructCompanyApiResponseForSfdrSixYears(baseDataset: SfdrData): DataAndMetaInformationSfdrData[] {
    const sfdrDatasets: DataAndMetaInformationSfdrData[] = [];
    for (let i = 0; i < 6; i++) {
      const reportingYear = 2023 + i;
      const fiscalYearEnd = `${reportingYear}-01-01`;
      const sfdrData = structuredClone(baseDataset);
      sfdrData.general.general.fiscalYearEnd = fiscalYearEnd;
      const metaData: DataMetaInformation = {
        dataId: `dataset-${i}`,
        reportingPeriod: reportingYear.toString(),
        qaStatus: QaStatus.Accepted,
        currentlyActive: true,
        dataType: DataTypeEnum.Sfdr,
        companyId: "mock-company-id",
        uploadTime: 0,
        uploaderUserId: "mock-uploader-id",
      };

      sfdrDatasets.push({
        metaInfo: metaData,
        data: sfdrData,
      });
    }
    return sfdrDatasets;
  }

  it("Check SFDR view page for company with six SFDR datasets reported in different years", () => {
    const preparedFixture = getPreparedFixture("companyWithOneFilledSfdrSubcategory", preparedFixtures);
    const mockedData = constructCompanyApiResponseForSfdrSixYears(preparedFixture.t);
    mountMLDTFrameworkPanel(DataTypeEnum.Sfdr, sfdrDisplayConfiguration, mockedData);
    getCellValueContainer("Fiscal Year End", 5).should("contain.text", "2023-01-01");

    for (let indexOfColumn = 0; indexOfColumn < 6; indexOfColumn++) {
      cy.get(`th[data-dataset-index=${indexOfColumn}]`).should("contain.text", (2028 - indexOfColumn).toString());
    }
  });

  it("Check SFDR view page for a dataset which has null values", () => {
    const preparedFixture = getPreparedFixture("sfdr-a-lot-of-nulls", preparedFixtures);

    mountMLDTFrameworkPanelFromFakeFixture(DataTypeEnum.Sfdr, sfdrDisplayConfiguration, [preparedFixture]);

    cy.contains("td.headers-bg", "Data Date").should("exist");
  });
});
