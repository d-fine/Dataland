import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import {
    Configuration,
    type DataMetaInformation,
    DataTypeEnum,
    EuTaxonomyDataForFinancialsControllerApi, type EuTaxonomyFinancialsData,
} from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetApproved } from '@e2e/utils/CompanyRolesUtils';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import EuTaxonomyFinancialsBaseFrameworkDefinition from '@/frameworks/eu-taxonomy-financials/BaseFrameworkDefinition';

let euTaxonomyFinancialsFixtureForTest: FixtureData<EuTaxonomyFinancialsData>;
before(function () {
  cy.fixture('CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures').then(function (jsonContent) {
    const preparedFixturesEuTaxonomyFinancials = jsonContent as Array<FixtureData<EuTaxonomyFinancialsData>>;
    euTaxonomyFinancialsFixtureForTest = getPreparedFixture(
      'company-for-all-types',
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
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId).then(() => {
              return uploadFrameworkDataForPublicToolboxFramework(
                EuTaxonomyFinancialsBaseFrameworkDefinition,
                token,
                storedCompany.companyId,
                '2023',
                euTaxonomyFinancialsFixtureForTest.t,
                true
              ).then((dataMetaInformation) => {
                cy.intercept(`**/api/data/${DataTypeEnum.EuTaxonomyFinancials}/${dataMetaInformation.dataId}`).as(
                  'fetchDataForPrefill'
                );
                cy.visitAndCheckAppMount(
                  '/companies/' +
                    storedCompany.companyId +
                    '/frameworks/' +
                    DataTypeEnum.EuTaxonomyFinancials +
                    '/upload?templateDataId=' +
                    dataMetaInformation.dataId
                );
                cy.wait('@fetchDataForPrefill', { timeout: Cypress.env('medium_timeout_in_ms') as number });
                cy.get('h1').should('contain', testCompanyName);
                cy.intercept({
                  url: `**/api/data/${DataTypeEnum.EuTaxonomyFinancials}?bypassQa=true`,
                  times: 1,
                }).as('postCompanyAssociatedData');
                submitButton.clickButton();
                cy.wait('@postCompanyAssociatedData', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then(
                  (postInterception) => {
                    cy.url().should('eq', getBaseUrl() + '/datasets');
                    isDatasetApproved();
                    const dataMetaInformationOfReuploadedDataset = postInterception.response
                      ?.body as DataMetaInformation;
                    return new EuTaxonomyDataForFinancialsControllerApi(new Configuration({ accessToken: token }))
                      .getCompanyAssociatedEuTaxonomyDataForFinancials(dataMetaInformationOfReuploadedDataset.dataId)
                      .then((axiosResponse) => {
                        const frontendSubmittedEuTaxonomyFinancialsDataset = axiosResponse.data
                          .data as unknown as EuTaxonomyFinancialsData;

                        compareObjectKeysAndValuesDeep(
                          euTaxonomyFinancialsFixtureForTest.t as unknown as Record<string, object>,
                          frontendSubmittedEuTaxonomyFinancialsDataset as Record<string, object>
                        );
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
