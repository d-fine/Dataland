import {
  type DataMetaInformation,
  DataTypeEnum,
  type EuTaxonomyDataForFinancials,
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
import {
  uploadFrameworkDataForLegacyFramework,
  uploadFrameworkDataForPublicToolboxFramework,
} from '@e2e/utils/FrameworkUpload';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';

describeIf(
  'As a user, I expect to be able to add a new dataset and see it as pending',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function () {
    let storedCompany: StoredCompany;
    let preparedEuTaxonomyFixtures: Array<FixtureData<EuTaxonomyDataForFinancials>>;
    let preparedLksgFixtures: Array<FixtureData<LksgData>>;

    before(function () {
      cy.fixture('CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures').then(function (jsonContent) {
        preparedEuTaxonomyFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
      });

      cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
        preparedLksgFixtures = jsonContent as Array<FixtureData<LksgData>>;
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const testCompany = generateDummyCompanyInformation(`company-for-testing-qa-${new Date().getTime()}`);
        return uploadCompanyViaApi(token, testCompany).then((newCompany) => (storedCompany = newCompany));
      });
    });

    it('Check whether newly added dataset has Pending status and can be approved by a reviewer', () => {
      const data = getPreparedFixture('company-for-all-types', preparedEuTaxonomyFixtures);
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadFrameworkDataForLegacyFramework(
          DataTypeEnum.EutaxonomyFinancials,
          token,
          storedCompany.companyId,
          '2022',
          data.t,
          false
        ).then(() => {
          cy.intercept(`**/api/companies/${storedCompany.companyId}`).as('getCompanyInformationOfUploadedCompany');
          testSubmittedDatasetIsInReviewListAndAcceptIt(storedCompany);
        });
      });
    });

    it('Check whether newly added dataset has Rejected status and can be edited', () => {
      const data = getPreparedFixture('lksg-all-fields', preparedLksgFixtures);
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadFrameworkDataForPublicToolboxFramework(
          LksgBaseFrameworkDefinition,
          token,
          storedCompany.companyId,
          '2022',
          data.t,
          false
        ).then((dataMetaInfo) => {
          cy.intercept(`**/api/companies/${storedCompany.companyId}`).as('getCompanyInformationOfUploadedCompany');
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

  testDatasetPresentWithCorrectStatus(companyName, 'PENDING');

  safeLogout();
  login(reviewer_name, reviewer_pw);

  viewRecentlyUploadedDatasetsInQaTable();

  cy.get('[data-test="qa-review-section"] .p-datatable-tbody')
    .last()
    .should('exist')
    .get('.qa-review-company-name')
    .should('contain', companyName);

  cy.get('[data-test="qa-review-section"] .p-datatable-tbody tr').last().click();

  cy.get('[data-test="qaRejectButton"').should('exist');
  cy.get('span[data-test="hideEmptyDataToggleCaption"]').should('exist');
  cy.get('span[class=p-inputswitch-slider]').should('exist');
  cy.get('div[data-test="hideEmptyDataToggleButton"]').should('not.have.class', 'p-inputswitch-checked');

  cy.get('[data-test="qaApproveButton"').should('exist').click();

  safeLogout();
  login(uploader_name, uploader_pw);

  testDatasetPresentWithCorrectStatus(companyName, 'APPROVED');
}

/**
 * Validates that the view page is in review mode by ensuring that at least one hidden-field icon is displayed
 */
function validateThatViewPageIsInReviewMode(): void {
  cy.get('i[data-test=hidden-icon]').should('exist');
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

  cy.contains('td', dataMetaInfo.dataId).click();

  validateThatViewPageIsInReviewMode();
  cy.get('[data-test="qaRejectButton"').should('exist').click();

  safeLogout();
  login(uploader_name, uploader_pw);

  testDatasetPresentWithCorrectStatus(storedCompany.companyInformation.companyName, 'REJECTED');

  cy.intercept(`**/api/data/lksg/${dataMetaInfo.dataId}`).as('getUploadedDataset');
  cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/lksg/${dataMetaInfo.dataId}`);
  cy.wait('@getUploadedDataset');
  cy.get('[data-test="datasetDisplayStatusContainer"]').should('exist');
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
  cy.visitAndCheckAppMount('/qualityassurance');
  cy.contains('span', 'REVIEW');
  cy.get('.p-paginator-last', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then((element) => {
    if (element.prop('disabled')) {
      return;
    }
    element.trigger('click');
  });
  cy.wait('@getCompanyInformationOfUploadedCompany');
}

/**
 * Visits the datasets page and verifies that the last dataset matches the company name and expected status
 * @param companyName The name of the company that just uploaded
 * @param status The current expected status of the dataset
 */
function testDatasetPresentWithCorrectStatus(companyName: string, status: string): void {
  cy.intercept('**/api/users/**').as('getMyDatasets');
  cy.visitAndCheckAppMount('/datasets');
  cy.wait('@getMyDatasets');

  cy.get('[data-test="datasets-table"] .p-datatable-tbody tr', {
    timeout: Cypress.env('medium_timeout_in_ms') as number,
  })
    .first()
    .find('.data-test-company-name')
    .should('contain', companyName);

  cy.get('[data-test="datasets-table"]').get('span[data-test="qa-status"]').should('contain', status);
}

/**
 * Logs the user out without testing the url
 */
function safeLogout(): void {
  cy.intercept('**/api-keys/getApiKeyMetaInfoForUser', { body: [] }).as('getApiKeyMetaInfoForUser');
  cy.visitAndCheckAppMount('/api-key').wait('@getApiKeyMetaInfoForUser');
  cy.get("div[id='profile-picture-dropdown-toggle']").click();
  cy.get("a[id='profile-picture-dropdown-logout-anchor']").click();
  cy.url().should('eq', getBaseUrl() + '/');
  cy.contains('a', 'Login');
}
