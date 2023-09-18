import QualityAssurance from "@/components/pages/QualityAssurance.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { type DataMetaInformation, DataTypeEnum, type PathwaysToParisData } from "@clients/backend";
import { QaStatus } from "@clients/qaservice";
import ViewFrameworkData from "@/components/pages/ViewFrameworkData.vue";
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_USER } from "@/utils/KeycloakUtils";

describe("Component tests for the Quality Assurance page", () => {
  let p2pFixtureForTest: FixtureData<PathwaysToParisData>;
  let mockDataMetaInfoForActiveDataset: DataMetaInformation;

  before(function () {
    cy.fixture("CompanyInformationWithP2pPreparedFixtures").then(function (jsonContent) {
      const preparedP2pFixtures = jsonContent as Array<FixtureData<PathwaysToParisData>>;
      p2pFixtureForTest = getPreparedFixture("P2p-date-2023-04-18", preparedP2pFixtures);
      cy.fixture("MetaInfoDataForCompany.json").then((metaInfos: Array<DataMetaInformation>) => {
        mockDataMetaInfoForActiveDataset = metaInfos[0];
      });
    });
  });

  const keycloakMockWithUploaderAndReviewerRoles = minimalKeycloakMock({
    roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_REVIEWER],
  });
  const mockDataMetaInfoForP2pTestDataset = {
    dataId: "p2pTestDataId",
    companyId: "testCompanyId",
    dataType: DataTypeEnum.P2p,
    uploadTime: 1672531200,
    reportingPeriod: "2023",
    currentlyActive: false,
    qaStatus: QaStatus.Pending,
  } as DataMetaInformation;

  it("Mock a pending P2P-dataset to check if datasets appear on the QA-overview-page", () => {
    cy.intercept("**/qa/datasets", [mockDataMetaInfoForP2pTestDataset.dataId]);
    cy.intercept(`**/api/metadata/${mockDataMetaInfoForP2pTestDataset.dataId}`, mockDataMetaInfoForP2pTestDataset);
    cy.intercept(`**/api/companies/${mockDataMetaInfoForP2pTestDataset.companyId}`, {
      companyId: mockDataMetaInfoForP2pTestDataset.companyId,
      companyInformation: p2pFixtureForTest.companyInformation,
      dataRegisteredByDataland: [mockDataMetaInfoForP2pTestDataset],
    });
    cy.mountWithPlugins<typeof QualityAssurance>(QualityAssurance, {
      keycloak: keycloakMockWithUploaderAndReviewerRoles,
    });
    cy.contains("td", `${mockDataMetaInfoForP2pTestDataset.dataId}`);
  });

  it.only("Mock a pending P2P-dataset to check if dataset can be reviewed", () => {
    // TODO remove "Only" tag
    cy.intercept(`**/api/metadata?companyId=${mockDataMetaInfoForP2pTestDataset.companyId}`, [
      mockDataMetaInfoForActiveDataset,
    ]);
    cy.intercept(`**/api/companies/${mockDataMetaInfoForP2pTestDataset.companyId}`, {
      companyId: mockDataMetaInfoForP2pTestDataset.companyId,
      companyInformation: p2pFixtureForTest.companyInformation,
      dataRegisteredByDataland: [mockDataMetaInfoForActiveDataset, mockDataMetaInfoForP2pTestDataset],
    });
    cy.intercept(`**/api/metadata/${mockDataMetaInfoForP2pTestDataset.dataId}`, mockDataMetaInfoForP2pTestDataset);
    cy.intercept(`**/api/data/${DataTypeEnum.P2p}/${mockDataMetaInfoForP2pTestDataset.dataId}`, {
      companyId: mockDataMetaInfoForP2pTestDataset.companyId,
      reportingPeriod: mockDataMetaInfoForP2pTestDataset.reportingPeriod,
      data: p2pFixtureForTest.t,
    }).as("fetchP2pData");

    cy.mountWithPlugins<typeof ViewFrameworkData>(ViewFrameworkData, {
      keycloak: keycloakMockWithUploaderAndReviewerRoles,
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        companyId: mockDataMetaInfoForP2pTestDataset.companyId,
        dataType: DataTypeEnum.P2p,
        dataId: mockDataMetaInfoForP2pTestDataset.dataId,
      },
    }).then(() => {
      // cy.get('#framework_data_search_bar_standard').should('not.exist');
      cy.get("#chooseFrameworkDropdown").should("not.exist");
      // cy.get('a[data-test="gotoNewDatasetButton"]')
      //   .should('not.exist');

      cy.wait(90000);

      // TODO test absence of hidden stuff (search bar, dropdowns...)
      // TODO check existence of expected stuff (pending-dataset-info, REJECT and APPROVE button)
      // TODO test that REJECT and APPROVE buttons stick while scrolling  =>  First test if cypress can actually assert this
      // TODO click "REJECT" and spy on the correct and expected API-call
      // TODO revisit the page and this time click on "APPROVE" and spy on the correct and expected API-call
    });
  });
});
