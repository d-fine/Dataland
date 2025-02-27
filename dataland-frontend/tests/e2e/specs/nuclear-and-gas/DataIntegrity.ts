import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  type NuclearAndGasData,
  NuclearAndGasDataControllerApi,
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
} from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { uploadGenericFrameworkData } from '@e2e/utils/FrameworkUpload';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetApproved } from '@e2e/utils/CompanyRolesUtils';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';

let nuclearAndGasFixtureForTest: FixtureData<NuclearAndGasData>;
before(function () {
  cy.fixture('CompanyInformationWithNuclearAndGasPreparedFixtures').then(function (jsonContent) {
    const preparedFixtureNuclearAndGas = jsonContent as Array<FixtureData<NuclearAndGasData>>;
    nuclearAndGasFixtureForTest = getPreparedFixture(
      'All-fields-defined-for-EU-NuclearAndGas-Framework',
      preparedFixtureNuclearAndGas
    );
  });
});

describeIf(
  'As a user, I expect to be able to edit and submit Nuclear and Gas data via the upload form',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      Cypress.env('excludeBypassQaIntercept', true);
    });

    it(
      'Create a company and a Nuclear and Gas dataset via api, ' +
        'then re-upload it with the upload form in Edit mode ' +
        'and assure that the re-uploaded dataset equals the pre-uploaded one',
      () => {
        const uniqueCompanyMarkerWithDate = Date.now().toString();
        const testCompanyName = 'Company-Created-In-Nuclear-and-Gas-Blanket-Test-' + uniqueCompanyMarkerWithDate;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
              return uploadGenericFrameworkData(
                token,
                storedCompany.companyId,
                '2021',
                nuclearAndGasFixtureForTest.t,
                (config) =>
                  getBasePublicFrameworkDefinition(DataTypeEnum.NuclearAndGas)!.getPublicFrameworkApiClient(config)
              ).then((dataMetaInformation) => {
                cy.intercept(`**/api/data/${DataTypeEnum.NuclearAndGas}/${dataMetaInformation.dataId}**`)
                  .as('fetchDataForPrefill')
                  .visitAndCheckAppMount(
                    '/companies/' +
                      storedCompany.companyId +
                      '/frameworks/' +
                      DataTypeEnum.NuclearAndGas +
                      '/upload?templateDataId=' +
                      dataMetaInformation.dataId
                  );
                cy.wait('@fetchDataForPrefill', { timeout: Cypress.env('medium_timeout_in_ms') as number });
                cy.get('h1').should('contain', testCompanyName);
                cy.intercept({
                  url: `**/api/data/${DataTypeEnum.NuclearAndGas}?bypassQa=true`,
                  times: 1,
                }).as('postCompanyAssociatedData');
                submitButton.clickButton();
                cy.wait('@postCompanyAssociatedData', {
                  timeout: Cypress.env('medium_timeout_in_ms') as number,
                }).then((postInterception) => {
                  cy.url().should('eq', getBaseUrl() + '/datasets');
                  isDatasetApproved();
                  const dataMetaInformationOfReuploadedDataset = postInterception.response?.body as DataMetaInformation;
                  return new NuclearAndGasDataControllerApi(new Configuration({ accessToken: token }))
                    .getCompanyAssociatedNuclearAndGasData(dataMetaInformationOfReuploadedDataset.dataId)
                    .then((axiosResponse) => {
                      const frontendSubmittedNuclearAndGasDataset = axiosResponse.data.data;

                      compareObjectKeysAndValuesDeep(
                        nuclearAndGasFixtureForTest.t as Record<string, object>,
                        frontendSubmittedNuclearAndGasDataset as Record<string, object>
                      );
                    });
                });
              });
            });
          });
        });
      }
    );
  }
);
