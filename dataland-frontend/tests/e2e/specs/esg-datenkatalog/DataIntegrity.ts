import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type EsgDatenkatalogData,
  EsgDatenkatalogDataControllerApi,
} from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { uploadGenericFrameworkData } from '@e2e/utils/FrameworkUpload';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetApproved } from '@e2e/utils/CompanyRolesUtils';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';

let esgDatenkatalogFixtureForTest: FixtureData<EsgDatenkatalogData>;
before(function () {
  cy.fixture('CompanyInformationWithEsgDatenkatalogPreparedFixtures').then(function (jsonContent) {
    const preparedFixturesEsgDatenkatalog = jsonContent as Array<FixtureData<EsgDatenkatalogData>>;
    esgDatenkatalogFixtureForTest = getPreparedFixture(
      'EsgDatenkatalog-dataset-with-no-null-fields',
      preparedFixturesEsgDatenkatalog
    );
  });
});

describeIf(
  'As a user, I expect to be able to edit and submit ESG Datenkatalog data via the upload form',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      Cypress.env('excludeBypassQaIntercept', true);
    });

    it(
      'Create a company and a ESG Datenkatalog dataset via api, then re-upload it with the upload form in ' +
        'Edit mode and assure that the re-uploaded dataset equals the pre-uploaded one',
      () => {
        const uniqueCompanyMarkerWithDate = Date.now().toString();
        const testCompanyNameEsgDatenkatalog =
          'Company-Created-In-EsgDatenkatalog-Blanket-Test-' + uniqueCompanyMarkerWithDate;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyNameEsgDatenkatalog)).then(
            (storedCompany) => {
              return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
                return uploadGenericFrameworkData(
                  token,
                  storedCompany.companyId,
                  '2021',
                  esgDatenkatalogFixtureForTest.t,
                  (config) =>
                    getBasePublicFrameworkDefinition(DataTypeEnum.EsgDatenkatalog)!.getPublicFrameworkApiClient(config)
                ).then((dataMetaInformation) => {
                  cy.intercept(`**/api/data/${DataTypeEnum.EsgDatenkatalog}/${dataMetaInformation.dataId}`).as(
                    'fetchDataForPrefill'
                  );
                  cy.visitAndCheckAppMount(
                    '/companies/' +
                      storedCompany.companyId +
                      '/frameworks/' +
                      DataTypeEnum.EsgDatenkatalog +
                      '/upload?templateDataId=' +
                      dataMetaInformation.dataId
                  );
                  cy.wait('@fetchDataForPrefill', { timeout: Cypress.env('medium_timeout_in_ms') as number });
                  cy.get('h1').should('contain', testCompanyNameEsgDatenkatalog);
                  cy.intercept({
                    url: `**/api/data/${DataTypeEnum.EsgDatenkatalog}?bypassQa=true`,
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
                    return new EsgDatenkatalogDataControllerApi(new Configuration({ accessToken: token }))
                      .getCompanyAssociatedEsgDatenkatalogData(dataMetaInformationOfReuploadedDataset.dataId)
                      .then((axiosResponse) => {
                        const frontendSubmittedEsgDatenkatalogDataset = axiosResponse.data.data;
                        esgDatenkatalogFixtureForTest.t.allgemein?.richtlinienDesUnternehmens?.existenzVonRichtlinienZuSpezifischenThemen?.sort();
                        frontendSubmittedEsgDatenkatalogDataset.allgemein?.richtlinienDesUnternehmens?.existenzVonRichtlinienZuSpezifischenThemen?.sort();
                        compareObjectKeysAndValuesDeep(
                          esgDatenkatalogFixtureForTest.t as unknown as Record<string, object>,
                          frontendSubmittedEsgDatenkatalogDataset as unknown as Record<string, object>
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
