import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type EutaxonomyFinancialsData,
  EutaxonomyFinancialsDataControllerApi,
} from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetAccepted } from '@e2e/utils/CompanyRolesUtils';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials/BaseFrameworkDefinition';

let euTaxonomyFinancialsFixtureForTest: FixtureData<EutaxonomyFinancialsData>;

type UploadedDatasetContext = {
  token: string;
  companyId: string;
  dataId: string;
};

/**
 * Creates a company, assigns ownership to the admin and uploads the initial framework dataset.
 *
 * @param token keycloak access token
 * @param testCompanyName name used for the generated dummy company
 * @param reportingPeriod reporting period for the uploaded dataset
 * @returns token, company id and uploaded dataset id
 */
async function createCompanyAndUploadDataset(
  token: string,
  testCompanyName: string,
  reportingPeriod: string
): Promise<UploadedDatasetContext> {
  const storedCompany = await uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
  await assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId);
  const dataMetaInformation = await uploadFrameworkDataForPublicToolboxFramework(
    EuTaxonomyFinancialsBaseFrameworkDefinition,
    token,
    storedCompany.companyId,
    reportingPeriod,
    euTaxonomyFinancialsFixtureForTest.t
  );
  return {
    token: token,
    companyId: storedCompany.companyId,
    dataId: dataMetaInformation.dataId,
  };
}

/**
 * Fetches a previously uploaded/reuploaded Eu Taxonomy Financials dataset from the backend API.
 *
 * @param token keycloak access token
 * @param dataId id of the dataset to fetch
 * @returns backend dataset payload
 */
async function fetchReuploadedDataset(token: string, dataId: string): Promise<EutaxonomyFinancialsData> {
  const axiosResponse = await new EutaxonomyFinancialsDataControllerApi(
    new Configuration({ accessToken: token })
  ).getCompanyAssociatedEutaxonomyFinancialsData(dataId);
  return axiosResponse.data.data as unknown as EutaxonomyFinancialsData;
}

/**
 * Opens the upload form in edit mode, submits it and fetches the resulting reuploaded dataset.
 *
 * @param token keycloak access token
 * @param companyId id of the company
 * @param dataId id of the dataset used for form prefill
 * @param testCompanyName expected company name displayed in the header
 * @returns reuploaded dataset from backend
 */
function submitInEditModeAndFetchReuploadedDataset(
  token: string,
  companyId: string,
  dataId: string,
  testCompanyName: string
): Cypress.Chainable<EutaxonomyFinancialsData> {
  cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyFinancials}/${dataId}**`).as('fetchDataForPrefill');
  cy.visitAndCheckAppMount(
    '/companies/' + companyId + '/frameworks/' + DataTypeEnum.EutaxonomyFinancials + '/upload?templateDataId=' + dataId
  );
  cy.wait('@fetchDataForPrefill', { timeout: Cypress.env('medium_timeout_in_ms') as number });
  cy.get('h1').should('contain', testCompanyName);
  cy.intercept({
    url: `**/api/data/${DataTypeEnum.EutaxonomyFinancials}?bypassQa=true`,
    times: 1,
  }).as('postCompanyAssociatedData');
  submitButton.clickButton();
  return cy
    .wait('@postCompanyAssociatedData', { timeout: Cypress.env('medium_timeout_in_ms') as number })
    .then((postInterception) => {
      const dataMetaInformationOfReuploadedDataset = postInterception.response?.body as DataMetaInformation;
      cy.url().should('eq', getBaseUrl() + '/datasets');
      isDatasetAccepted();
      return cy.then(() => fetchReuploadedDataset(token, dataMetaInformationOfReuploadedDataset.dataId));
    });
}

before(function () {
  cy.fixture('CompanyInformationWithEutaxonomyFinancialsPreparedFixtures').then(function (jsonContent) {
    const preparedFixturesEuTaxonomyFinancials = jsonContent as Array<FixtureData<EutaxonomyFinancialsData>>;
    euTaxonomyFinancialsFixtureForTest = getPreparedFixture(
      'eutaxonomy-financials-dataset-with-no-null-fields',
      preparedFixturesEuTaxonomyFinancials
    );
  });
});

describeIf(
  'As a user, I expect to be able to edit and submit Eu Taxonomy Financials data via the upload form',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      Cypress.env('excludeBypassQaIntercept', true);
    });

    it(
      'Create a company and a Eu Taxonomy Financials dataset via api, then re-upload it with the upload form in Edit mode and ' +
        'assure that the re-uploaded dataset equals the pre-uploaded one',
      () => {
        const testCompanyName = 'Company-Created-In-Eu-Taxonomy-Financials-Blanket-Test-Company';
        const reportingPeriod = '2023';
        getKeycloakToken(admin_name, admin_pw)
          .then((token: string) => {
            return createCompanyAndUploadDataset(token, testCompanyName, reportingPeriod);
          })
          .then(({ token, companyId, dataId }) => {
            return submitInEditModeAndFetchReuploadedDataset(token, companyId, dataId, testCompanyName);
          })
          .then((frontendSubmittedEuTaxonomyFinancialsDataset) => {
            compareObjectKeysAndValuesDeep(
              euTaxonomyFinancialsFixtureForTest.t as unknown as Record<string, object>,
              frontendSubmittedEuTaxonomyFinancialsDataset as Record<string, object>,
              undefined,
              ['publicationDate']
            );
          });
      }
    );
  }
);
