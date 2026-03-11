import EutaxonomyFinancials202673BaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials-2026-73/BaseFrameworkDefinition';
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type EutaxonomyFinancials202673Data,
  EutaxonomyFinancials202673DataControllerApi,
  type StoredCompany,
} from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { assignCompanyOwnershipToDatalandAdmin } from '@e2e/utils/CompanyRolesUtils';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { type CompanyRoleAssignment } from '@clients/communitymanager';

let eutaxonomyFinancials202673FixtureData: FixtureData<EutaxonomyFinancials202673Data>;
before(function () {
  cy.fixture('CompanyInformationWithEutaxonomyFinancials202673PreparedFixtures.json').then(function (jsonContent) {
    const preparedFixtures = jsonContent as Array<FixtureData<EutaxonomyFinancials202673Data>>;
    eutaxonomyFinancials202673FixtureData = getPreparedFixture(
      'All-fields-defined-for-EU-Taxonomy-Financials-202673-Framework-Company',
      preparedFixtures
    );
  });
});

/**
 * Helper to get Keycloak token.
 */
function getToken(): Cypress.Chainable<string> {
  return getKeycloakToken(admin_name, admin_pw);
}

/**
 * Helper to create a company.
 */
function createCompany(token: string, testCompanyName: string): Promise<StoredCompany> {
  return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
}

/**
 * Helper to assign company ownership.
 */
function assignOwnership(token: string, companyId: string): Promise<CompanyRoleAssignment> {
  return assignCompanyOwnershipToDatalandAdmin(token, companyId);
}

/**
 * Helper to upload framework data.
 */
function uploadFrameworkData(token: string, companyId: string): Promise<DataMetaInformation> {
  return uploadFrameworkDataForPublicToolboxFramework(
    EutaxonomyFinancials202673BaseFrameworkDefinition,
    token,
    companyId,
    eutaxonomyFinancials202673FixtureData.reportingPeriod,
    eutaxonomyFinancials202673FixtureData.t
  );
}

/**
 * Validates that the uploaded dataset is retrievable and matches the originally uploaded data.
 * @param token The access token for API calls.
 * @param dataId The ID of the uploaded dataset.
 * @param initiallyUploadedData The original data to compare against.
 */
function validateUploadedDataset(
  token: string,
  dataId: string,
  initiallyUploadedData: EutaxonomyFinancials202673Data
): Promise<void> {
  return new EutaxonomyFinancials202673DataControllerApi(new Configuration({ accessToken: token }))
    .getCompanyAssociatedEutaxonomyFinancials202673Data(dataId)
    .then((response) => {
      const datasetFromBackend = response.data.data;
      compareObjectKeysAndValuesDeep(
        initiallyUploadedData as Record<string, object>,
        datasetFromBackend as Record<string, object>,
        undefined,
        ['publicationDate']
      );
    });
}

/**
 * Sets up a company and uploads EU Taxonomy Financials (2026/73) framework data for testing.
 * @param testCompanyName The name of the test company to create.
 * @returns A Promise resolving to an object containing the token, storedCompany and dataId.
 */
function setupCompanyAndFramework(
  testCompanyName: string
): Cypress.Chainable<{ token: string; storedCompany: StoredCompany; dataId: string }> {
  let token: string;
  let storedCompany: StoredCompany;
  return getToken()
    .then((receivedToken) => {
      token = receivedToken;
      return createCompany(token, testCompanyName);
    })
    .then((company) => {
      storedCompany = company;
      return assignOwnership(token, storedCompany.companyId);
    })
    .then(() => uploadFrameworkData(token, storedCompany.companyId))
    .then(({ dataId }) => ({ token, storedCompany, dataId }));
}

describeIf(
  'As a user, I expect to be able to upload EU Taxonomy Financials (2026/73) data via the api, and that the uploaded ' +
    'data is displayed correctly in the frontend',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    before(() => {
      Cypress.env('excludeBypassQaIntercept', true);
    });

    it(
      'Create a company and an EU Taxonomy Financials (2026/73) dataset via api and assure that the data is ' +
        'stored correctly by retrieving and comparing it',
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = 'Company-Created-In-EU-Taxonomy-Financials-202673-Blanket-Test-' + uniqueCompanyMarker;

        cy.wrap(null, { timeout: Cypress.env('long_timeout_in_ms') as number })
          .then(() => setupCompanyAndFramework(testCompanyName))
          .then(({ token, storedCompany, dataId }) => {
            cy.ensureLoggedIn(admin_name, admin_pw);
            cy.intercept({
              url: `**/api/data/${DataTypeEnum.EutaxonomyFinancials202673}/**`,
              times: 1,
            }).as('getUploadedData');
            cy.visitAndCheckAppMount(
              `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials202673}`
            );
            cy.wait('@getUploadedData', {
              timeout: Cypress.env('medium_timeout_in_ms') as number,
            });
            cy.get('h1').should('contain', testCompanyName);
            cy.wrap(null).then(() => validateUploadedDataset(token, dataId, eutaxonomyFinancials202673FixtureData.t));
          });
      }
    );
  }
);
