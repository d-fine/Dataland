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
import { submitButton } from '@sharedUtils/components/SubmitButton';
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
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    it(
      'Create a company and a PCAF dataset via api, then re-upload it with the ' +
        'upload form in Edit mode and assure that it worked by validating a couple of values',
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = 'Company-Created-In-PCAF-Blanket-Test-' + uniqueCompanyMarker;

        getKeycloakToken(admin_name, admin_pw).then(async (token: string) => {
          // Create company and upload PCAF prepared fixture data via API
          const storedCompanyId = (await uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)))
            .companyId;
          await assignCompanyOwnershipToDatalandAdmin(token, storedCompanyId);

          const dataMetaInformation: DataMetaInformation = await uploadFrameworkDataForPublicToolboxFramework(
            PcafFrameworkDefinition,
            token,
            storedCompanyId,
            pcafFixtureData.reportingPeriod,
            pcafFixtureData.t
          );
          console.log(dataMetaInformation);
          cy.pause();

          // Define intercepts for test
          cy.intercept(
            'GET',
            `**/api/data/${DataTypeEnum.Pcaf}?reportingPeriod=${pcafFixtureData.reportingPeriod}&companyId=${storedCompanyId}`
          ).as('getInitiallyUploadedData');

          cy.intercept({
            url: `**/api/data/${DataTypeEnum.Pcaf}?bypassQa=true`,
            times: 1,
          }).as('resubmitPcafData');

          // Log in via frontend, visit edit page for PCAF dataset uploaded via API, and re-submit data
          cy.ensureLoggedIn(admin_name, admin_pw);
          let initiallyUploadedData: PcafData;
          cy.visitAndCheckAppMount(
            `/companies/${storedCompanyId}/frameworks/${DataTypeEnum.Pcaf}` +
              `/upload?reportingPeriod=${pcafFixtureData.reportingPeriod}`
          );

          cy.wait('@getInitiallyUploadedData', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then(
            (interception) => {
              initiallyUploadedData = (interception.response?.body as CompanyAssociatedDataPcafData).data;
            }
          );

          cy.get('h1').should('contain', testCompanyName);
          submitButton.clickButton();

          // Intercept the re-submit, and compare the interception's data with the dataset retrieved from the backend
          cy.wait('@resubmitPcafData', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then(
            async (interception) => {
              cy.url().should('eq', getBaseUrl() + '/datasets');
              isDatasetAccepted();
              const dataMetaInformationOfReuploadedDataset = interception.response?.body as DataMetaInformation;
              const pcafDataControllerApi = new PcafDataControllerApi(new Configuration({ accessToken: token }));

              const response = await pcafDataControllerApi.getCompanyAssociatedPcafData(
                dataMetaInformationOfReuploadedDataset.dataId
              );
              const reuploadedDatasetFromBackend = response.data.data;

              compareObjectKeysAndValuesDeep(
                initiallyUploadedData as Record<string, object>,
                reuploadedDatasetFromBackend as Record<string, object>
              );

              cy.url().should('eq', getBaseUrl() + '/datasets');
              cy.get('[data-test="datasets-table"]').should('be.visible');
            }
          );
        });
      }
    );
  }
);
