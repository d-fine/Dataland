import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  type CompanyAssociatedDataNuclearAndGasData,
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type NuclearAndGasData,
  NuclearAndGasDataControllerApi,
} from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetAccepted } from '@e2e/utils/CompanyRolesUtils';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';
import EuTaxonomyNuclearAndGasBaseFrameworkDefinition from '@/frameworks/nuclear-and-gas/BaseFrameworkDefinition';

let euTaxonomyForNuclearAndGasFixtureForTest: FixtureData<NuclearAndGasData>;

type UploadedDatasetContext = {
  token: string;
  companyId: string;
  dataId: string;
  dataType: string;
};

type DatasetsComparisonContext = {
  datasetFromPrefillRequest: NuclearAndGasData;
  reuploadedDatasetFromBackend: NuclearAndGasData;
};

/**
 * Creates a company, assigns ownership to the admin and uploads the initial Nuclear and Gas dataset.
 * @param token keycloak access token
 * @param testCompanyName name used for the generated dummy company
 * @returns token, company id, dataset id and dataset type
 */
async function createCompanyAndUploadDataset(token: string, testCompanyName: string): Promise<UploadedDatasetContext> {
  const storedCompany = await uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
  await assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId);
  const dataMetaInformation = await uploadFrameworkDataForPublicToolboxFramework(
    EuTaxonomyNuclearAndGasBaseFrameworkDefinition,
    token,
    storedCompany.companyId,
    '2021',
    euTaxonomyForNuclearAndGasFixtureForTest.t
  );
  return {
    token,
    companyId: storedCompany.companyId,
    dataId: dataMetaInformation.dataId,
    dataType: dataMetaInformation.dataType,
  };
}

/**
 * Fetches a previously uploaded/reuploaded Nuclear and Gas dataset from backend.
 * @param token keycloak access token
 * @param dataId id of the dataset to fetch
 * @returns backend dataset payload
 */
async function fetchReuploadedDataset(token: string, dataId: string): Promise<NuclearAndGasData> {
  const axiosResponse = await new NuclearAndGasDataControllerApi(
    new Configuration({ accessToken: token })
  ).getCompanyAssociatedNuclearAndGasData(dataId);
  return axiosResponse.data.data;
}

/**
 * Opens the upload form in edit mode and captures the prefilled dataset from the API response.
 * @param companyId id of the company
 * @param dataType data type used in the prefill request endpoint
 * @param dataId id of the dataset used for template prefill
 * @param testCompanyName expected company name shown in the header
 * @returns dataset from the prefill request
 */
function openEditFormAndCapturePrefillDataset(
  companyId: string,
  dataType: string,
  dataId: string,
  testCompanyName: string
): Cypress.Chainable<NuclearAndGasData> {
  cy.ensureLoggedIn(admin_name, admin_pw);
  cy.intercept({
    url: `api/data/${dataType}/${dataId}`,
    times: 1,
  }).as('getDataToPrefillForm');
  cy.visitAndCheckAppMount(
    '/companies/' + companyId + '/frameworks/' + DataTypeEnum.NuclearAndGas + '/upload?templateDataId=' + dataId
  );
  return cy
    .wait('@getDataToPrefillForm', { timeout: Cypress.env('medium_timeout_in_ms') as number })
    .then((interception) => {
      cy.get('h1').should('contain', testCompanyName);
      return (interception.response?.body as CompanyAssociatedDataNuclearAndGasData).data;
    });
}

/**
 * Submits the edit form and fetches the resulting reuploaded dataset from backend.
 * @param token keycloak access token
 * @returns reuploaded dataset payload
 */
function submitAndFetchReuploadedDataset(token: string): Cypress.Chainable<NuclearAndGasData> {
  cy.intercept({
    url: `**/api/data/${DataTypeEnum.NuclearAndGas}?bypassQa=true`,
    times: 1,
  }).as('postCompanyAssociatedData');
  submitButton.clickButton();
  return cy
    .wait('@postCompanyAssociatedData', { timeout: Cypress.env('medium_timeout_in_ms') as number })
    .then((interception) => {
      cy.url().should('eq', getBaseUrl() + '/datasets');
      isDatasetAccepted();
      const dataMetaInformationOfReuploadedDataset = interception.response?.body as DataMetaInformation;
      return fetchReuploadedDataset(token, dataMetaInformationOfReuploadedDataset.dataId);
    });
}

before(function () {
  cy.fixture('CompanyInformationWithNuclearAndGasPreparedFixtures.json').then(function (jsonContent) {
    const preparedFixtures = jsonContent as Array<FixtureData<NuclearAndGasData>>;
    euTaxonomyForNuclearAndGasFixtureForTest = getPreparedFixture(
      'All-fields-defined-for-EU-NuclearAndGas-Framework',
      preparedFixtures
    );
  });
});

describeIf(
  'As a user, I expect to be able to upload EU taxonomy data for Nuclear and Gas via the api, and that the uploaded data is displayed ' +
    'correctly in the frontend',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    before(() => {
      Cypress.env('excludeBypassQaIntercept', true);
    });

    it(
      'Create a company and an EU taxonomy for Nuclear and Gas dataset via api, then re-upload it with the ' +
        'upload form in Edit mode and assure that it worked by validating a couple of values',
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = 'Company-Created-In-Eu-Taxo-Nuclear-and-Gas-Blanket-Test-' + uniqueCompanyMarker;

        getKeycloakToken(admin_name, admin_pw)
          .then((token: string) => {
            return createCompanyAndUploadDataset(token, testCompanyName);
          })
          .then(({ token, companyId, dataId, dataType }) => {
            return openEditFormAndCapturePrefillDataset(companyId, dataType, dataId, testCompanyName).then(
              (datasetFromPrefillRequest) => {
                return submitAndFetchReuploadedDataset(token).then((reuploadedDatasetFromBackend) => {
                  return {
                    datasetFromPrefillRequest,
                    reuploadedDatasetFromBackend,
                  } as DatasetsComparisonContext;
                });
              }
            );
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
