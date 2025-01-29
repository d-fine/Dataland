import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  type AdditionalCompanyInformationData,
  AdditionalCompanyInformationDataControllerApi,
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

let additionalCompanyInformationFixtureForTest: FixtureData<AdditionalCompanyInformationData>;
before(function () {
  cy.fixture('CompanyInformationWithAdditionalCompanyInformationPreparedFixtures').then(function (jsonContent) {
    const preparedFixtureAdditionalCompanyInformation = jsonContent as Array<
      FixtureData<AdditionalCompanyInformationData>
    >;
    additionalCompanyInformationFixtureForTest = getPreparedFixture(
      'additional-company-information-dataset-with-no-null-fields',
      preparedFixtureAdditionalCompanyInformation
    );
  });
});

describeIf(
  'As a user, I expect to be able to edit and submit Additional company information data via the upload form',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      Cypress.env('excludeBypassQaIntercept', true);
    });

    it(
      'Create a company and a Additional Company Information dataset via api, ' +
        'then re-upload it with the upload form in Edit mode ' +
        'and assure that the re-uploaded dataset equals the pre-uploaded one',
      () => {
        const uniqueCompanyMarkerWithDate = Date.now().toString();
        const testCompanyName =
          'Company-Created-In-Additional-Company-Information-Blanket-Test-' + uniqueCompanyMarkerWithDate;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
              return uploadGenericFrameworkData(
                token,
                storedCompany.companyId,
                '2021',
                additionalCompanyInformationFixtureForTest.t,
                (config) =>
                  getBasePublicFrameworkDefinition(
                    DataTypeEnum.AdditionalCompanyInformation
                  )!.getPublicFrameworkApiClient(config)
              ).then((dataMetaInformation) => {
                cy.intercept(`**/api/data/${DataTypeEnum.AdditionalCompanyInformation}/${dataMetaInformation.dataId}**`)
                  .as('fetchDataForPrefill')
                  .visitAndCheckAppMount(
                    '/companies/' +
                      storedCompany.companyId +
                      '/frameworks/' +
                      DataTypeEnum.AdditionalCompanyInformation +
                      '/upload?templateDataId=' +
                      dataMetaInformation.dataId
                  );
                cy.wait('@fetchDataForPrefill', { timeout: Cypress.env('medium_timeout_in_ms') as number });
                cy.get('h1').should('contain', testCompanyName);
                cy.intercept({
                  url: `**/api/data/${DataTypeEnum.AdditionalCompanyInformation}?bypassQa=true`,
                  times: 1,
                }).as('postCompanyAssociatedData');
                submitButton.clickButton();
                cy.wait('@postCompanyAssociatedData', {
                  timeout: Cypress.env('medium_timeout_in_ms') as number,
                }).then((postInterception) => {
                  cy.url().should('eq', getBaseUrl() + '/datasets');
                  isDatasetApproved();
                  const dataMetaInformationOfReuploadedDataset = postInterception.response?.body as DataMetaInformation;
                  return new AdditionalCompanyInformationDataControllerApi(new Configuration({ accessToken: token }))
                    .getCompanyAssociatedAdditionalCompanyInformationData(dataMetaInformationOfReuploadedDataset.dataId)
                    .then((axiosResponse) => {
                      const frontendSubmittedAdditionalCompanyInformationDataset = axiosResponse.data.data;

                      compareObjectKeysAndValuesDeep(
                        additionalCompanyInformationFixtureForTest.t as Record<string, object>,
                        frontendSubmittedAdditionalCompanyInformationDataset as Record<string, object>,
                        '',
                        ['publicationDate']
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
