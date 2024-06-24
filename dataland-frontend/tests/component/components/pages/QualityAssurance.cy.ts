// @ts-nocheck
import QualityAssurance from '@/components/pages/QualityAssurance.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { type DataMetaInformation, DataTypeEnum, type PathwaysToParisData } from '@clients/backend';
import { QaStatus } from '@clients/qaservice';
import ViewFrameworkData from '@/components/pages/ViewFrameworkData.vue';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakUtils';

describe('Component tests for the Quality Assurance page', () => {
  let p2pFixtureForTest: FixtureData<PathwaysToParisData>;
  let mockDataMetaInfoForActiveDataset: DataMetaInformation;

  before(function () {
    cy.fixture('CompanyInformationWithP2pPreparedFixtures').then(function (jsonContent) {
      const preparedP2pFixtures = jsonContent as Array<FixtureData<PathwaysToParisData>>;
      p2pFixtureForTest = getPreparedFixture('P2p-date-2023-04-18', preparedP2pFixtures);
      cy.fixture('MetaInfoDataMocksForOneCompany.json').then((metaInfos: Array<DataMetaInformation>) => {
        mockDataMetaInfoForActiveDataset = metaInfos[0];
      });
    });
  });

  const keycloakMockWithUploaderAndReviewerRoles = minimalKeycloakMock({
    roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_REVIEWER],
  });
  const mockDataMetaInfoForP2pTestDataset = {
    dataId: 'p2pTestDataId',
    companyId: 'testCompanyId',
    dataType: DataTypeEnum.P2p,
    uploadTime: 1672531200,
    reportingPeriod: '2023',
    currentlyActive: false,
    qaStatus: QaStatus.Pending,
  } as DataMetaInformation;

  it('Mock a pending P2P-dataset to check if datasets appear on the QA-overview-page', () => {
    cy.intercept('**/qa/datasets', [mockDataMetaInfoForP2pTestDataset.dataId]);
    cy.intercept(`**/api/metadata/${mockDataMetaInfoForP2pTestDataset.dataId}`, mockDataMetaInfoForP2pTestDataset);
    cy.intercept(`**/api/companies/${mockDataMetaInfoForP2pTestDataset.companyId}`, {
      companyId: mockDataMetaInfoForP2pTestDataset.companyId,
      companyInformation: p2pFixtureForTest.companyInformation,
      dataRegisteredByDataland: [mockDataMetaInfoForP2pTestDataset],
    });
    cy.mountWithPlugins<typeof QualityAssurance>(QualityAssurance, {
      keycloak: keycloakMockWithUploaderAndReviewerRoles,
    });
    cy.contains('td', `${mockDataMetaInfoForP2pTestDataset.dataId}`);
  });

  it('Mock a pending P2P-dataset to check if dataset can be reviewed', () => {
    cy.intercept(`**/api/metadata?companyId=${mockDataMetaInfoForP2pTestDataset.companyId}`, [
      mockDataMetaInfoForActiveDataset,
    ]);
    cy.intercept(
      `**/api/companies/${mockDataMetaInfoForP2pTestDataset.companyId}/info`,
      p2pFixtureForTest.companyInformation
    );
    cy.intercept(`**/api/metadata/${mockDataMetaInfoForP2pTestDataset.dataId}`, mockDataMetaInfoForP2pTestDataset);
    cy.intercept(`**/api/data/${DataTypeEnum.P2p}/${mockDataMetaInfoForP2pTestDataset.dataId}`, {
      companyId: mockDataMetaInfoForP2pTestDataset.companyId,
      reportingPeriod: mockDataMetaInfoForP2pTestDataset.reportingPeriod,
      data: p2pFixtureForTest.t,
    }).as('fetchP2pData');

    cy.mountWithDialog<typeof ViewFrameworkData>(
      ViewFrameworkData,
      {
        keycloak: keycloakMockWithUploaderAndReviewerRoles,
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
      },
      {
        companyId: mockDataMetaInfoForP2pTestDataset.companyId,
        dataType: DataTypeEnum.P2p,
        dataId: mockDataMetaInfoForP2pTestDataset.dataId,
      }
    ).then(() => {
      cy.get('h1').contains(p2pFixtureForTest.companyInformation.companyName).should('be.visible');

      cy.get('#framework_data_search_bar_standard').should('not.exist');
      cy.get('#chooseFrameworkDropdown').should('not.exist');
      cy.get('a[data-test="gotoNewDatasetButton"]').should('not.exist');

      cy.get('div[data-test="datasetDisplayStatusContainer"] span').contains(
        'This dataset is currently pending review'
      );

      cy.intercept(
        'POST',
        `**/qa/datasets/${mockDataMetaInfoForP2pTestDataset.dataId}?qaStatus=${QaStatus.Accepted}`,
        (request) => {
          request.reply(200, {});
        }
      ).as('approveDataset');
      cy.get('button[data-test="qaApproveButton"]').should('exist').click();
      cy.wait('@approveDataset');
      cy.get('div[data-test="qaReviewSubmittedMessage"]').should('exist');
      cy.get('.p-dialog-header-close').click();

      cy.intercept(
        'POST',
        `**/qa/datasets/${mockDataMetaInfoForP2pTestDataset.dataId}?qaStatus=${QaStatus.Rejected}`,
        (request) => {
          request.reply(200, {});
        }
      ).as('rejectDataset');
      cy.get('button[data-test="qaRejectButton"]').should('exist').click();
      cy.wait('@rejectDataset');
      cy.get('div[data-test="qaReviewSubmittedMessage"]').should('exist');
      cy.get('.p-dialog-header-close').click();
    });
  });
});
