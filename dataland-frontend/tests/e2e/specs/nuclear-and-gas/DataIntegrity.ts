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

        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
              return uploadFrameworkDataForPublicToolboxFramework(
                EuTaxonomyNuclearAndGasBaseFrameworkDefinition,
                token,
                storedCompany.companyId,
                '2021',
                euTaxonomyForNuclearAndGasFixtureForTest.t
              ).then((dataMetaInformation) => {
                let datasetFromPrefillRequest: NuclearAndGasData;
                cy.ensureLoggedIn(admin_name, admin_pw);
                cy.intercept({
                  url: `api/data/${dataMetaInformation.dataType}/${dataMetaInformation.dataId}`,
                  times: 1,
                }).as('getDataToPrefillForm');
                cy.visitAndCheckAppMount(
                  '/companies/' +
                    storedCompany.companyId +
                    '/frameworks/' +
                    DataTypeEnum.NuclearAndGas +
                    '/upload?templateDataId=' +
                    dataMetaInformation.dataId
                );

                cy.wait('@getDataToPrefillForm', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then(
                  (interception) => {
                    datasetFromPrefillRequest = (interception.response?.body as CompanyAssociatedDataNuclearAndGasData)
                      .data;
                  }
                );
                cy.get('h1').should('contain', testCompanyName);
                cy.intercept({
                  url: `**/api/data/${DataTypeEnum.NuclearAndGas}?bypassQa=true`,
                  times: 1,
                }).as('postCompanyAssociatedData');
                submitButton.clickButton();
                cy.wait('@postCompanyAssociatedData', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then(
                  (interception) => {
                    cy.url().should('eq', getBaseUrl() + '/datasets');
                    isDatasetAccepted();
                    const dataMetaInformationOfReuploadedDataset = interception.response?.body as DataMetaInformation;
                    return new NuclearAndGasDataControllerApi(new Configuration({ accessToken: token }))
                      .getCompanyAssociatedNuclearAndGasData(dataMetaInformationOfReuploadedDataset.dataId)
                      .then((axiosResponse) => {
                        const reuploadedDatasetFromBackend = axiosResponse.data.data;
                        compareObjectKeysAndValuesDeep(
                          datasetFromPrefillRequest as Record<string, object>,
                          reuploadedDatasetFromBackend as Record<string, object>
                        );
                        cy.url().should('eq', getBaseUrl() + '/datasets');
                        cy.get('[data-test="datasets-table"]').should('be.visible');
                      });
                  }
                );
              });
            });
          });
        });
      }
    );
  }
);
