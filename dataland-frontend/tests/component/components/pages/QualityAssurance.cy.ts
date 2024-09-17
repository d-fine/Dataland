import QualityAssurance from '@/components/pages/QualityAssurance.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import {
  type CompanyAssociatedDataPathwaysToParisData,
  type DataMetaInformation,
  DataTypeEnum,
  type PathwaysToParisData,
  type StoredCompany,
} from '@clients/backend';
import { QaStatus } from '@clients/qaservice';
import ViewFrameworkData from '@/components/pages/ViewFrameworkData.vue';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakUtils';
import { getMountingFunction } from '@ct/testUtils/Mount';

describe('Component tests for the Quality Assurance page', () => {
  let p2pFixture: FixtureData<PathwaysToParisData>;
  let mockDataMetaInfoForActiveDataset: DataMetaInformation;

  before(function () {
    cy.fixture('CompanyInformationWithP2pPreparedFixtures').then(function (jsonContent) {
      const preparedP2pFixtures = jsonContent as Array<FixtureData<PathwaysToParisData>>;
      p2pFixture = getPreparedFixture('P2p-date-2023-04-18', preparedP2pFixtures);
      cy.fixture('MetaInfoDataMocksForOneCompany.json').then((metaInfos: Array<DataMetaInformation>) => {
        mockDataMetaInfoForActiveDataset = metaInfos[0];
      });
    });
  });

  const keycloakMockWithUploaderAndReviewerRoles = minimalKeycloakMock({
    roles: [KEYCLOAK_ROLE_USER, KEYCLOAK_ROLE_REVIEWER],
  });
  const mockDataMetaInfo: DataMetaInformation = {
    dataId: 'p2pTestDataId',
    companyId: 'testCompanyId',
    dataType: DataTypeEnum.P2p,
    uploadTime: 1672531200,
    reportingPeriod: '2023',
    currentlyActive: false,
    qaStatus: QaStatus.Pending,
  };

  it('Mock a pending P2P-dataset to check if datasets appear on the QA-overview-page', () => {
    cy.intercept('**/qa/datasets', [mockDataMetaInfo.dataId]);
    cy.intercept(`**/api/metadata/${mockDataMetaInfo.dataId}`, mockDataMetaInfo);

    const mockStoredCompany: StoredCompany = {
      companyId: mockDataMetaInfo.companyId,
      companyInformation: p2pFixture.companyInformation,
      dataRegisteredByDataland: [mockDataMetaInfo],
    };
    cy.intercept(`**/api/companies/${mockDataMetaInfo.companyId}`, mockStoredCompany);

    getMountingFunction({
      keycloak: keycloakMockWithUploaderAndReviewerRoles,
    })(QualityAssurance);
    cy.contains('td', `${mockDataMetaInfo.dataId}`);
  });

  it('Mock a pending P2P-dataset to check if dataset can be reviewed', () => {
    cy.intercept(`**/api/metadata?companyId=${mockDataMetaInfo.companyId}`, [mockDataMetaInfoForActiveDataset]);
    cy.intercept(`**/api/companies/${mockDataMetaInfo.companyId}/info`, p2pFixture.companyInformation);
    cy.intercept(`**/api/metadata/${mockDataMetaInfo.dataId}`, mockDataMetaInfo);
    const mockCompanyAssociatedP2pData: CompanyAssociatedDataPathwaysToParisData = {
      companyId: mockDataMetaInfo.companyId,
      reportingPeriod: mockDataMetaInfo.reportingPeriod,
      data: p2pFixture.t,
    };
    cy.intercept(`**/api/data/${DataTypeEnum.P2p}/${mockDataMetaInfo.dataId}`, mockCompanyAssociatedP2pData).as(
      'fetchP2pData'
    );

    getMountingFunction({
      keycloak: keycloakMockWithUploaderAndReviewerRoles,
      dialogOptions: {
        mountWithDialog: true,
        propsToPassToTheMountedComponent: {
          companyId: mockDataMetaInfo.companyId,
          dataType: DataTypeEnum.P2p,
          dataId: mockDataMetaInfo.dataId,
        },
      },
    })(ViewFrameworkData);
    cy.get('h1').contains(p2pFixture.companyInformation.companyName).should('be.visible');

    cy.get('#framework_data_search_bar_standard').should('not.exist');
    cy.get('#chooseFrameworkDropdown').should('not.exist');
    cy.get('a[data-test="gotoNewDatasetButton"]').should('not.exist');

    cy.get('div[data-test="datasetDisplayStatusContainer"] span').contains('This dataset is currently pending review');

    cy.intercept('POST', `**/qa/datasets/${mockDataMetaInfo.dataId}?qaStatus=${QaStatus.Accepted}`, (request) => {
      request.reply(200, {});
    }).as('approveDataset');
    cy.get('button[data-test="qaApproveButton"]').should('exist').click();
    cy.wait('@approveDataset');
    cy.get('div[data-test="qaReviewSubmittedMessage"]').should('exist');
    cy.get('.p-dialog-header-close').click();

    cy.intercept('POST', `**/qa/datasets/${mockDataMetaInfo.dataId}?qaStatus=${QaStatus.Rejected}`, (request) => {
      request.reply(200, {});
    }).as('rejectDataset');
    cy.get('button[data-test="qaRejectButton"]').should('exist').click();
    cy.wait('@rejectDataset');
    cy.get('div[data-test="qaReviewSubmittedMessage"]').should('exist');
    cy.get('.p-dialog-header-close').click();
  });
});
