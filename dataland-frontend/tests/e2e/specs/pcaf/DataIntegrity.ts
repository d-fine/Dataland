import PcafFrameworkDefinition from '@/frameworks/pcaf/BaseFrameworkDefinition.ts';
import {
  type CompanyAssociatedDataPcafData,
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type PcafData,
  PcafDataControllerApi,
} from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetAccepted } from '@e2e/utils/CompanyRolesUtils';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';

let pcafFixtureData: FixtureData<PcafData>;
before(function () {
  cy.fixture('CompanyInformationWithPcafPreparedFixtures.json').then(function (jsonContent) {
    const preparedFixtures = jsonContent as Array<FixtureData<PcafData>>;
    pcafFixtureData = getPreparedFixture('All-fields-defined-for-PCAF-Framework-Company', preparedFixtures);
  });
});

describeIf(
  'As a user, I expect to be able to upload PCAF data via the api, and that the uploaded data is displayed ' +
    'correctly in the frontend',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    before(() => {
      Cypress.env('excludeBypassQaIntercept', true);
    });

    it(
      'Create a company and a PCAF dataset via api, then re-upload it with the ' +
        'upload form in Edit mode and assure that it worked by validating a couple of values',
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = 'Company-Created-In-PCAF-Blanket-Test-' + uniqueCompanyMarker;

        getKeycloakToken(admin_name, admin_pw).then(async (token: string) => {
          await uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then(
            async (storedCompany) => {
              await assignCompanyOwnershipToDatalandAdmin(token, storedCompany.companyId);
              await uploadFrameworkDataForPublicToolboxFramework(
                PcafFrameworkDefinition,
                token,
                storedCompany.companyId,
                pcafFixtureData.reportingPeriod,
                pcafFixtureData.t
              ).then(() => {
                let initiallyUploadedData: PcafData;
                cy.ensureLoggedIn(admin_name, admin_pw);
                cy.intercept({
                  url: `**/api/data/${DataTypeEnum.Pcaf}/**`,
                  times: 1,
                }).as('getInitiallyUploadedData');
                cy.intercept({
                  url: `**/api/data/${DataTypeEnum.Pcaf}?bypassQa=true`,
                  times: 1,
                }).as('resubmitPcafData');
                cy.visitAndCheckAppMount(
                  `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.Pcaf}` +
                    `/upload?reportingPeriod=${pcafFixtureData.reportingPeriod}`
                );
                cy.wait('@getInitiallyUploadedData', {
                  timeout: Cypress.env('medium_timeout_in_ms') as number,
                }).then((interception) => {
                  initiallyUploadedData = (interception.response?.body as CompanyAssociatedDataPcafData).data;
                });
                cy.get('h1').should('contain', testCompanyName);
                cy.get('[data-test="submitButton"]').click();
                cy.wait('@resubmitPcafData', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then(
                  async (interception) => {
                    cy.url().should('eq', getBaseUrl() + '/datasets');
                    isDatasetAccepted();
                    const dataMetaInformationOfReuploadedDataset = interception.response?.body as DataMetaInformation;
                    await new PcafDataControllerApi(new Configuration({ accessToken: token }))
                      .getCompanyAssociatedPcafData(dataMetaInformationOfReuploadedDataset.dataId)
                      .then((response) => {
                        const reuploadedDatasetFromBackend = response.data.data;
                        compareObjectKeysAndValuesDeep(
                          initiallyUploadedData as Record<string, object>,
                          reuploadedDatasetFromBackend as Record<string, object>
                        );
                        cy.url().should('eq', getBaseUrl() + '/datasets');
                        cy.get('[data-test="datasets-table"]').should('be.visible');
                      });
                  }
                );
              });
            }
          );
        });
      }
    );
  }
);
