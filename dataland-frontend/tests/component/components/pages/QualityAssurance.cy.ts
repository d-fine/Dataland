import QualityAssurance from '@/components/pages/QualityAssurance.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import {
  type CompanyAssociatedDataPathwaysToParisData,
  type DataMetaInformation,
  DataTypeEnum,
  type PathwaysToParisData,
} from '@clients/backend';
import { QaStatus, type ReviewQueueResponse } from '@clients/qaservice';
import ViewFrameworkData from '@/components/pages/ViewFrameworkData.vue';
import { KEYCLOAK_ROLE_REVIEWER, KEYCLOAK_ROLE_USER } from '@/utils/KeycloakUtils';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';

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

  /**
   * Builds a review queue element.
   * @param dataId to include
   * @param companyName to include
   * @param companyId to include
   * @param framework to include
   * @param reportingPeriod to include
   * @param receptionTime to include
   * @returns the element
   */
  function buildReviewQueueElement(
    dataId: string,
    companyName?: string,
    companyId?: string,
    framework?: string,
    reportingPeriod?: string,
    receptionTime: number = Date.now()
  ): ReviewQueueResponse {
    return {
      dataId: dataId,
      receptionTime: receptionTime,
      companyName: companyName,
      companyId: companyId,
      framework: framework,
      reportingPeriod: reportingPeriod,
    };
  }

  it('Check if datasets appear on the QA-overview-page and filtering works as expected', () => {
    const dataIdAlpha = crypto.randomUUID();
    const companyNameAlpha = 'Alpha Company AG';
    const companyIdAlpha = crypto.randomUUID();
    const reviewQueueElementAlpha = buildReviewQueueElement(
      dataIdAlpha,
      companyNameAlpha,
      companyIdAlpha,
      DataTypeEnum.P2p,
      '2022'
    );

    const dataIdBeta = crypto.randomUUID();
    const companyNameBeta = 'Beta Corporate Ltd.';
    const companyIdBeta = crypto.randomUUID();
    const reviewQueueElementBeta = buildReviewQueueElement(
      dataIdBeta,
      companyNameBeta,
      companyIdBeta,
      DataTypeEnum.Sfdr,
      '2023'
    );

    const mockReviewQueue = [reviewQueueElementAlpha, reviewQueueElementBeta];
    cy.intercept(`**/qa/datasets?chunkSize=10&chunkIndex=0`, mockReviewQueue);
    cy.intercept(`**/qa/numberOfUnreviewedDatasets`, mockReviewQueue.length.toString());

    getMountingFunction({ keycloak: keycloakMockWithUploaderAndReviewerRoles })(QualityAssurance);
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`);
    cy.contains('span', 'Showing results 1-2 of 2.');

    const companySearchTerm = 'Alpha';
    cy.intercept(`**/qa/datasets?companyName=${companySearchTerm}&chunkSize=10&chunkIndex=0`, [
      reviewQueueElementAlpha,
    ]);
    cy.intercept(`**/qa/numberOfUnreviewedDatasets?companyName=${companySearchTerm}`, '1');
    cy.get(`input[data-test="companyNameSearchbar"]`).type(companySearchTerm);
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');
    cy.contains('span', 'Showing results 1-1 of 1.');

    cy.get(`input[data-test="companyNameSearchbar"]`).clear();
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`);

    const frameworkToFilterFor = DataTypeEnum.P2p;
    const frameworkHumanReadableName = humanizeStringOrNumber(frameworkToFilterFor);
    cy.intercept(`**/qa/datasets?dataTypes=${DataTypeEnum.P2p}&chunkSize=10&chunkIndex=0`, [reviewQueueElementAlpha]);
    cy.intercept(`**/qa/numberOfUnreviewedDatasets?dataTypes=${DataTypeEnum.P2p}`, '1');
    cy.get(`div[data-test="framework-picker"]`).click();
    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();

    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`).should('not.exist');

    cy.get(`li[aria-label="${frameworkHumanReadableName}"]`).click();
    cy.contains('td', `${dataIdAlpha}`);
    cy.contains('td', `${dataIdBeta}`);
    cy.get(`div[data-test="framework-picker"]`).click();

    // TODO reporting period filter (typing + date picker?)
    // TODO combined filter
    // TODO cleanup test code (functions for better readability)
  });

  it('Check if dataset can be reviewed on the view page', () => {
    const mockDataMetaInfo: DataMetaInformation = {
      dataId: 'p2pTestDataId',
      companyId: 'testCompanyId',
      dataType: DataTypeEnum.P2p,
      uploadTime: 1672531200,
      reportingPeriod: '2023',
      currentlyActive: false,
      qaStatus: QaStatus.Pending,
    };

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
