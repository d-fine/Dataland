import QualityAssurance from "@/components/pages/QualityAssurance.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { DataMetaInformation, DataTypeEnum, PathwaysToParisData, SmeData } from "@clients/backend";
import { QaStatus } from "@clients/qaservice";

describe("Component tests for the Quality Assurance page", () => {
  const keycloakMock = minimalKeycloakMock({
    roles: ["ROLE_USER", "ROLE_UPLOADER", "ROLE_REVIEWER"],
  });

  /**
   * Mock a data meta information object for a dataset specified by its fixture data, data Id and data type
   * @param fixtureData The fixture data corresponding to review
   * @param dataId The Id corresponding to the dataset to review
   * @param dataType The type of the dataset to review
   * @returns the data meta information object created
   */
  function mockDataMetaInformationForFixtureDataWithIdAndType(
    fixtureData: FixtureData<PathwaysToParisData | SmeData>,
    dataId: string,
    dataType: DataTypeEnum,
  ): DataMetaInformation {
    return {
      dataId: dataId,
      companyId: "testCompanyId",
      dataType: dataType,
      uploadTime: 1672531200 /* 01.01.2023 */,
      reportingPeriod: fixtureData.reportingPeriod,
      currentlyActive: false,
      qaStatus: QaStatus.Pending,
    } as DataMetaInformation;
  }

  /**
   * Create a QA data object from the fixture data and the data id, configure the intercepts, mount the component and
   * check that clicking leads to the expected request
   * @param fixtureData The fixture data corresponding to review
   * @param dataId The Id corresponding to the dataset to review
   * @param dataType The type of the dataset to review
   */
  function createQaDataObjectAndPerformRequestCheck(
    fixtureData: FixtureData<PathwaysToParisData | SmeData>,
    dataId: string,
    dataType: DataTypeEnum,
  ): void {
    const qaDataObject = {
      dataId: dataId,
      metaInformation: mockDataMetaInformationForFixtureDataWithIdAndType(fixtureData, dataId, dataType),
      companyInformation: fixtureData.companyInformation,
    };
    cy.intercept("**/qa/datasets", [dataId]);
    cy.intercept("**/api/metadata/*", qaDataObject.metaInformation);
    cy.intercept("**/api/companies/*", {
      companyId: qaDataObject.metaInformation.companyId,
      companyInformation: qaDataObject.companyInformation,
      dataRegisteredByDataland: [qaDataObject.metaInformation],
    });
    cy.intercept(`**/api/data/${dataType}/*`, {
      companyId: qaDataObject.metaInformation.companyId,
      reportingPeriod: fixtureData.reportingPeriod,
      data: fixtureData.t,
    }).as("fetchData");
    cy.mountWithPlugins<typeof QualityAssurance>(QualityAssurance, {
      keycloak: keycloakMock,
    });
    cy.contains("td", `${dataId}`).click();
    cy.wait("@fetchData");
  }

  it("Checks that P2P datasets can be reviewed as expected", () => {
    cy.fixture("CompanyInformationWithP2pPreparedFixtures").then(function (jsonContent) {
      const preparedP2pFixtures = jsonContent as Array<FixtureData<PathwaysToParisData>>;
      const p2pFixtureData = getPreparedFixture("P2p-date-2023-04-18", preparedP2pFixtures);
      const p2pDataId = "p2pTestDataId";
      createQaDataObjectAndPerformRequestCheck(p2pFixtureData, p2pDataId, DataTypeEnum.P2p);
    });
  });

  it("Checks that SME datasets can be reviewed as expected", () => {
    cy.fixture("CompanyInformationWithSmePreparedFixtures").then(function (jsonContent) {
      const preparedSmeFixtures = jsonContent as Array<FixtureData<SmeData>>;
      const smeFixtureData = getPreparedFixture("SME-year-2023", preparedSmeFixtures);
      const smeDataId = "smeTestDataId";
      createQaDataObjectAndPerformRequestCheck(smeFixtureData, smeDataId, DataTypeEnum.Sme);
    });
  });
});
