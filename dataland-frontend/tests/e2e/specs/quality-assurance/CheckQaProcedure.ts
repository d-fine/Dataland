import {
  type DataMetaInformation,
  type EutaxonomyFinancialsData,
  type LksgData,
  type StoredCompany,
} from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility';
import { getKeycloakToken, login } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import {
  admin_name,
  admin_pw,
  getBaseUrl,
  reviewer_name,
  reviewer_pw,
  uploader_name,
  uploader_pw,
} from '@e2e/utils/Cypress';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';

describeIf(
  'As a user, I expect to be able to add a new dataset and see it as pending',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    let storedCompany: StoredCompany;
    let preparedEuTaxonomyFixtures: Array<FixtureData<EutaxonomyFinancialsData>>;
    let preparedLksgFixtures: Array<FixtureData<LksgData>>;

    before(function () {
      cy.fixture('CompanyInformationWithEutaxonomyFinancialsPreparedFixtures').then(function (jsonContent) {
        preparedEuTaxonomyFixtures = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
      });

      cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
        preparedLksgFixtures = jsonContent as Array<FixtureData<LksgData>>;
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const testCompany = generateDummyCompanyInformation(`company-for-testing-qa-${Date.now()}`);
        return uploadCompanyViaApi(token, testCompany).then((newCompany) => (storedCompany = newCompany));
      });
    });

    it('Check whether newly added dataset has Pending status and can be approved by a reviewer', () => {
      const data = getPreparedFixture('lightweight-eu-taxo-financials-dataset', preparedEuTaxonomyFixtures);

      cy.intercept('POST', '**/api/data/eutaxonomy-financials*').as('uploadDataset');

      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadFrameworkDataForPublicToolboxFramework(
          EuTaxonomyFinancialsBaseFrameworkDefinition,
          token,
          storedCompany.companyId,
          '2022',
          data.t,
          false
        ).then(() => {
          cy.wait('@uploadDataset', { timeout: 15000 }).its('response.statusCode').should('be.oneOf', [200, 201]);

          cy.wait(2000);
          testSubmittedDatasetIsInReviewListAndAcceptIt(storedCompany);
        });
      });
    });

    it('Check whether newly added dataset has Rejected status and can be edited', () => {
      const data = getPreparedFixture('lksg-all-fields', preparedLksgFixtures);

      cy.intercept('POST', '**/api/data/lksg*').as('uploadLksgDataset');

      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadFrameworkDataForPublicToolboxFramework(
          LksgBaseFrameworkDefinition,
          token,
          storedCompany.companyId,
          '2022',
          data.t,
          false
        ).then((dataMetaInfo) => {
          cy.wait('@uploadLksgDataset', { timeout: 15000 }).its('response.statusCode').should('be.oneOf', [200, 201]);

          cy.wait(2000);
          testSubmittedDatasetIsInReviewListAndRejectIt(storedCompany, dataMetaInfo);
        });
      });
    });
  }
);

/**
 * Tests that the item was added and is visible on the QA list
 * @param storedCompany The company for which a dataset has been uploaded
 */
function testSubmittedDatasetIsInReviewListAndAcceptIt(storedCompany: StoredCompany): void {
  const companyName = storedCompany.companyInformation.companyName;
  login(uploader_name, uploader_pw);

  testDatasetPresentWithCorrectStatus(companyName, 'Pending');

  safeLogout();
  login(reviewer_name, reviewer_pw);

  viewRecentlyUploadedDatasetsInQaTable();

  cy.get('[data-test="qa-review-section"]', { timeout: 15000 }).should('be.visible');

  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .last()
    .should('exist')
    .get('[data-test="qa-review-company-name"]')
    .should('contain', companyName);

  cy.intercept('GET', '**/api/data/**').as('loadDatasetDetails');
  cy.get('[data-test="qa-review-section"] .p-datatable-tbody tr').last().click();
  cy.wait('@loadDatasetDetails', { timeout: 15000 });

  cy.get('[data-test="qaRejectButton"]').should('exist');
  cy.get('span[data-test="hideEmptyDataToggleCaption"]').should('exist');
  cy.get('.p-toggleswitch-slider').should('exist');
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should('not.have.class', 'p-toggleswitch-checked');

  cy.intercept('PUT', '**/api/data/**/qa-status').as('approveDataset');
  cy.get('[data-test="qaApproveButton"]').should('exist').click();
  cy.wait('@approveDataset', { timeout: 15000 }).its('response.statusCode').should('eq', 200);

  safeLogout();
  login(uploader_name, uploader_pw);

  testDatasetPresentWithCorrectStatus(companyName, 'Accepted');
}

/**
 * Tests that the dataset is visible on the QA list and reject it and if the edit button is present on the view page
 * @param storedCompany the stored company owning the dataset
 * @param dataMetaInfo the data meta information of the dataset that that was uploaded before
 */
function testSubmittedDatasetIsInReviewListAndRejectIt(
  storedCompany: StoredCompany,
  dataMetaInfo: DataMetaInformation
): void {
  login(reviewer_name, reviewer_pw);

  viewRecentlyUploadedDatasetsInQaTable();

  cy.get('[data-test="qa-review-section"]', { timeout: 15000 }).should('be.visible');
  cy.intercept('GET', '**/api/data/**').as('loadDatasetDetails');

  cy.contains('td', dataMetaInfo.dataId).click();
  cy.wait('@loadDatasetDetails', { timeout: 15000 });

  cy.intercept('PUT', '**/api/data/**/qa-status').as('rejectDataset');
  cy.get('[data-test="qaRejectButton"]').should('exist').click();
  cy.wait('@rejectDataset', { timeout: 15000 }).its('response.statusCode').should('eq', 200);

  safeLogout();
  login(uploader_name, uploader_pw);

  testDatasetPresentWithCorrectStatus(storedCompany.companyInformation.companyName, 'Rejected');

  cy.intercept(`**/api/data/lksg/${dataMetaInfo.dataId}`).as('getUploadedDataset');
  cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/lksg/${dataMetaInfo.dataId}`);
  cy.wait('@getUploadedDataset', { timeout: 15000 });

  cy.get('[data-test="datasetDisplayStatusContainer"]', { timeout: 10000 }).should('exist');
  cy.get('button[data-test="editDatasetButton"]').should('exist').click();

  cy.url().should(
    'eq',
    getBaseUrl() + `/companies/${storedCompany.companyId}/frameworks/lksg/upload?templateDataId=${dataMetaInfo.dataId}`
  );
}

/**
 * Visits the quality assurance page and switches to the last table page
 */
function viewRecentlyUploadedDatasetsInQaTable(): void {
  cy.intercept('GET', '**/api/data-meta-information/qa-status/pending*').as('getPendingDatasets');
  cy.intercept('GET', '**/api/users/**').as('getUserInfo');

  cy.visitAndCheckAppMount('/qualityassurance');

  cy.wait('@getPendingDatasets', { timeout: 15000 });
  cy.wait('@getUserInfo', { timeout: 10000 }).then(() => {
    cy.log('getUserInfo request failed or timed out, continuing anyway');
  });

  cy.contains('span', 'REVIEW', { timeout: 10000 }).should('be.visible');

  cy.get('.p-paginator-last', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then((element) => {
    if (!element.prop('disabled')) {
      cy.wrap(element).click();
      cy.wait(1000);
    }
  });
}

/**
 * Visits the datasets page and verifies that the last dataset matches the company name and expected status
 * @param companyName The name of the company that just uploaded
 * @param status The current expected status of the dataset
 */
function testDatasetPresentWithCorrectStatus(companyName: string, status: string): void {
  cy.intercept('**/api/users/**').as('getMyDatasets');
  cy.intercept('GET', '**/api/data-meta-information**').as('getDatasetsMetaInfo');

  cy.visitAndCheckAppMount('/datasets');

  cy.wait('@getMyDatasets', { timeout: 15000 });
  cy.wait('@getDatasetsMetaInfo', { timeout: 15000 });

  cy.wait(1000);

  cy.get('[data-test="datasets-table"] .p-datatable-tbody tr', {
    timeout: Cypress.env('medium_timeout_in_ms') as number,
  })
    .first()
    .should('be.visible')
    .find('.data-test-company-name')
    .should('contain', companyName);

  cy.get('[data-test="datasets-table"]')
    .find('[data-test="qa-status"]')
    .first()
    .should('be.visible')
    .and('contain', status);
}

/**
 * Logs the user out without testing the url
 */
function safeLogout(): void {
  cy.intercept('**/api-keys/getApiKeyMetaInfoForUser', { body: [] }).as('getApiKeyMetaInfoForUser');
  cy.intercept('POST', '**/auth/realms/**/protocol/openid-connect/logout').as('keycloakLogout');

  cy.visitAndCheckAppMount('/api-key').wait('@getApiKeyMetaInfoForUser', { timeout: 10000 });

  cy.get('[data-test="user-profile-toggle"]').should('be.visible').click();
  cy.get('a:contains("LOG OUT")').should('be.visible').click();

  cy.wait('@keycloakLogout', { timeout: 10000 }).then(() => {
    cy.log('Keycloak logout intercept failed, but continuing');
  });

  cy.url().should('eq', getBaseUrl() + '/');
  cy.get("[data-test='login-dataland-button']", { timeout: 10000 }).should('exist');
}
