import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  type CompanyAssociatedDataEutaxonomyNonFinancialsData,
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type EutaxonomyNonFinancialsData,
  EutaxonomyNonFinancialsDataControllerApi,
} from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetAccepted } from '@e2e/utils/CompanyRolesUtils';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';
import EuTaxonomyNonFinancialsBaseFrameworkDefinition from '@/frameworks/eutaxonomy-non-financials/BaseFrameworkDefinition';

let euTaxonomyForNonFinancialsFixtureForTest: FixtureData<EutaxonomyNonFinancialsData>;

type UploadedDatasetContext = {
  token: string;
  companyId: string;
  dataId: string;
  dataType: string;
};

type DatasetsComparisonContext = {
  datasetFromPrefillRequest: EutaxonomyNonFinancialsData;
  reuploadedDatasetFromBackend: EutaxonomyNonFinancialsData;
};

/**
 * Creates a company, assigns ownership to the admin and uploads the initial non-financials dataset.
 * @param token keycloak access token
 * @param testCompanyName name used for the generated dummy company
 * @returns token, company id, dataset id and dataset type
 */
async function createCompanyAndUploadDataset(token: string, testCompanyName: string): Promise<UploadedDatasetContext> {
  const storedCompany = await uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
  await assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId);
  const dataMetaInformation = await uploadFrameworkDataForPublicToolboxFramework(
    EuTaxonomyNonFinancialsBaseFrameworkDefinition,
    token,
    storedCompany.companyId,
    '2021',
    euTaxonomyForNonFinancialsFixtureForTest.t
  );
  return {
    token,
    companyId: storedCompany.companyId,
    dataId: dataMetaInformation.dataId,
    dataType: dataMetaInformation.dataType,
  };
}

/**
 * Fetches a previously uploaded/reuploaded Eu Taxonomy Non-Financials dataset from backend.
 * @param token keycloak access token
 * @param dataId id of the dataset to fetch
 * @returns backend dataset payload
 */
async function fetchReuploadedDataset(token: string, dataId: string): Promise<EutaxonomyNonFinancialsData> {
  const axiosResponse = await new EutaxonomyNonFinancialsDataControllerApi(
    new Configuration({ accessToken: token })
  ).getCompanyAssociatedEutaxonomyNonFinancialsData(dataId);
  return axiosResponse.data.data;
}

/**
 * Opens the upload form in edit mode, submits it and returns both compared datasets.
 * @param token keycloak access token
 * @param companyId id of the company
 * @param dataType data type used in the prefill request endpoint
 * @param dataId id of the dataset used for template prefill
 * @param testCompanyName expected company name displayed in the header
 * @returns original prefill dataset and reuploaded backend dataset for comparison
 */
function submitInEditModeAndFetchReuploadedDataset(
  token: string,
  companyId: string,
  dataType: string,
  dataId: string,
  testCompanyName: string
): Cypress.Chainable<DatasetsComparisonContext> {
  cy.ensureLoggedIn(admin_name, admin_pw);
  cy.intercept({
    url: `api/data/${dataType}/${dataId}`,
    times: 1,
  }).as('getDataToPrefillForm');
  cy.visitAndCheckAppMount(
    '/companies/' +
      companyId +
      '/frameworks/' +
      DataTypeEnum.EutaxonomyNonFinancials +
      '/upload?templateDataId=' +
      dataId
  );
  return cy
    .wait('@getDataToPrefillForm', { timeout: Cypress.env('medium_timeout_in_ms') as number })
    .then((interception) => {
      const datasetFromPrefillRequest = (
        interception.response?.body as CompanyAssociatedDataEutaxonomyNonFinancialsData
      ).data;
      cy.get('h1').should('contain', testCompanyName);
      cy.intercept({
        url: `**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}?bypassQa=true`,
        times: 1,
      }).as('postCompanyAssociatedData');
      submitButton.clickButton();
      return cy
        .wait('@postCompanyAssociatedData', { timeout: Cypress.env('medium_timeout_in_ms') as number })
        .then((interceptionAfterPost) => {
          const dataMetaInformationOfReuploadedDataset = interceptionAfterPost.response?.body as DataMetaInformation;
          cy.url().should('eq', getBaseUrl() + '/datasets');
          isDatasetAccepted();
          return cy
            .then(() => fetchReuploadedDataset(token, dataMetaInformationOfReuploadedDataset.dataId))
            .then((reuploadedDatasetFromBackend) => {
              return {
                datasetFromPrefillRequest,
                reuploadedDatasetFromBackend,
              };
            });
        });
    });
}

before(function () {
  cy.fixture('CompanyInformationWithEutaxonomyNonFinancialsPreparedFixtures.json').then(function (jsonContent) {
    const preparedFixtures = jsonContent as Array<FixtureData<EutaxonomyNonFinancialsData>>;
    euTaxonomyForNonFinancialsFixtureForTest = getPreparedFixture(
      'all-fields-defined-for-eu-taxo-non-financials-alpha',
      preparedFixtures
    );
  });
});

describeIf(
  'As a user, I expect to be able to upload EU taxonomy data for non-financials via the api, and that the uploaded data is displayed ' +
    'correctly in the frontend',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    before(() => {
      Cypress.env('excludeBypassQaIntercept', true);
    });

    it(
      'Create a company and an EU taxonomy for non-financials dataset via api, then re-upload it with the ' +
        'upload form in Edit mode and assure that it worked by validating a couple of values',
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = 'Company-Created-In-Eu-Taxo-Non-Financials-Blanket-Test-' + uniqueCompanyMarker;

        getKeycloakToken(admin_name, admin_pw)
          .then((token: string) => {
            return createCompanyAndUploadDataset(token, testCompanyName);
          })
          .then(({ token, companyId, dataId, dataType }) => {
            return submitInEditModeAndFetchReuploadedDataset(token, companyId, dataType, dataId, testCompanyName);
          })
          .then(({ datasetFromPrefillRequest, reuploadedDatasetFromBackend }) => {
            compareObjectKeysAndValuesDeep(
              datasetFromPrefillRequest as Record<string, object>,
              reuploadedDatasetFromBackend as Record<string, object>
            );
            cy.url().should('eq', getBaseUrl() + '/datasets');
            cy.get('[data-test="datasets-table"]').should('be.visible');
          });
      }
    );
  }
);
