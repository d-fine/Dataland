import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type HeimathafenData,
  HeimathafenDataControllerApi,
} from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { uploadGenericFrameworkData } from '@e2e/utils/FrameworkUpload';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetApproved } from '@e2e/utils/CompanyRolesUtils';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';

let heimathafenFixtureForTest: FixtureData<HeimathafenData>;
before(function () {
  cy.fixture('CompanyInformationWithHeimathafenPreparedFixtures').then(function (jsonContent) {
    const preparedFixturesHeimathafen = jsonContent as Array<FixtureData<HeimathafenData>>;
    heimathafenFixtureForTest = getPreparedFixture(
      'Heimathafen-dataset-with-no-null-fields',
      preparedFixturesHeimathafen
    );
  });
});

describeIf(
  'As a user, I expect to be able to edit and submit Heimathafen data via the upload form',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      Cypress.env('excludeBypassQaIntercept', true);
    });

    it(
      'Create a company and a Heimathafen dataset via api, then re-upload it with the upload form in Edit mode and ' +
        'assure that the re-uploaded dataset equals the pre-uploaded one',
      () => {
        const uniqueCompanyMarkerWithDate = Date.now().toString();
        const testCompanyNameHeimathafen = 'Company-Created-In-Heimathafen-Blanket-Test-' + uniqueCompanyMarkerWithDate;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyNameHeimathafen)).then(
            (storedCompany) => {
              return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
                return uploadGenericFrameworkData(
                  token,
                  storedCompany.companyId,
                  '2021',
                  heimathafenFixtureForTest.t,
                  (config) =>
                    getBasePublicFrameworkDefinition(DataTypeEnum.Heimathafen)!.getPublicFrameworkApiClient(config)
                ).then((dataMetaInformation) => {
                  cy.intercept(`**/api/data/${DataTypeEnum.Heimathafen}/${dataMetaInformation.dataId}**`)
                    .as('fetchDataForPrefill')
                    .visitAndCheckAppMount(
                      '/companies/' +
                        storedCompany.companyId +
                        '/frameworks/' +
                        DataTypeEnum.Heimathafen +
                        '/upload?templateDataId=' +
                        dataMetaInformation.dataId
                    );
                  cy.wait('@fetchDataForPrefill', { timeout: Cypress.env('medium_timeout_in_ms') as number });
                  cy.get('h1').should('contain', testCompanyNameHeimathafen);
                  cy.intercept({
                    url: `**/api/data/${DataTypeEnum.Heimathafen}?bypassQa=true`,
                    times: 1,
                  }).as('postCompanyAssociatedData');
                  submitButton.clickButton();
                  cy.wait('@postCompanyAssociatedData', {
                    timeout: Cypress.env('medium_timeout_in_ms') as number,
                  }).then((postInterception) => {
                    cy.url().should('eq', getBaseUrl() + '/datasets');
                    isDatasetApproved();
                    const dataMetaInformationOfReuploadedDataset = postInterception.response
                      ?.body as DataMetaInformation;
                    return new HeimathafenDataControllerApi(new Configuration({ accessToken: token }))
                      .getCompanyAssociatedHeimathafenData(dataMetaInformationOfReuploadedDataset.dataId)
                      .then((axiosResponse) => {
                        const frontendSubmittedHeimathafenDataset = axiosResponse.data.data;

                        compareObjectKeysAndValuesDeep(
                          heimathafenFixtureForTest.t as Record<string, object>,
                          frontendSubmittedHeimathafenDataset as Record<string, object>
                        );
                      });
                  });
                });
              });
            }
          );
        });
      }
    );
  }
);
