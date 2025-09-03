import PcafFrameworkDefinition from '@/frameworks/pcaf/BaseFrameworkDefinition.ts';
import {
  type CompanyAssociatedDataPcafData,
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type PcafData,
  PcafDataControllerApi,
  type StoredCompany,
} from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { assignCompanyOwnershipToDatalandAdmin, isDatasetAccepted } from '@e2e/utils/CompanyRolesUtils';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { admin_name, admin_pw, getBaseUrl } from '@e2e/utils/Cypress';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { compareObjectKeysAndValuesDeep } from '@e2e/utils/GeneralUtils';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { type CompanyRoleAssignment } from '@clients/communitymanager';

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

    /**
     * Helper to get Keycloak token.
     */
    function getToken(): Cypress.Chainable<string> {
      return getKeycloakToken(admin_name, admin_pw);
    }

    /**
     * Helper to create a company.
     */
    function createCompany(token: string, testCompanyName: string): Promise<StoredCompany> {
      return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
    }

    /**
     * Helper to assign company ownership.
     */
    function assignOwnership(token: string, companyId: string): Promise<CompanyRoleAssignment> {
      return assignCompanyOwnershipToDatalandAdmin(token, companyId);
    }

    /**
     * Helper to upload framework data.
     */
    function uploadFrameworkData(token: string, companyId: string): Promise<DataMetaInformation> {
      return uploadFrameworkDataForPublicToolboxFramework(
        PcafFrameworkDefinition,
        token,
        companyId,
        pcafFixtureData.reportingPeriod,
        pcafFixtureData.t
      );
    }

    /**
     * Sets up a company and uploads the PCAF framework data for testing.
     * @param testCompanyName The name of the test company to create.
     * @returns A Promise resolving to an object containing the token and storedCompany.
     */
    function setupCompanyAndFramework(
      testCompanyName: string
    ): Cypress.Chainable<{ token: string; storedCompany: StoredCompany }> {
      let token: string;
      let storedCompany: StoredCompany;
      return getToken()
        .then((receivedToken) => {
          token = receivedToken;
          return createCompany(token, testCompanyName);
        })
        .then((company) => {
          storedCompany = company;
          return assignOwnership(token, storedCompany.companyId);
        })
        .then(() => {
          return uploadFrameworkData(token, storedCompany.companyId);
        })
        .then(() => ({ token, storedCompany }));
    }

    /**
     * Validates that the re-uploaded dataset matches the initially uploaded data.
     * @param token The access token for API calls.
     * @param dataId The ID of the re-uploaded dataset.
     * @param initiallyUploadedData The original data to compare against.
     * @returns A Promise resolving when validation is complete.
     */
    function validateReuploadedDataset(token: string, dataId: string, initiallyUploadedData: PcafData): Promise<void> {
      return new PcafDataControllerApi(new Configuration({ accessToken: token }))
        .getCompanyAssociatedPcafData(dataId)
        .then((response) => {
          const reuploadedDatasetFromBackend = response.data.data;
          compareObjectKeysAndValuesDeep(
            initiallyUploadedData as Record<string, object>,
            reuploadedDatasetFromBackend as Record<string, object>
          );
        });
    }

    /**
     * Handles the validation and navigation after data resubmission.
     * @param interception The intercepted request response.
     * @param token The access token for API calls.
     * @param initiallyUploadedData The original data to compare against.
     */
    function handleDataResubmissionValidation(
      interception: { response?: { body?: DataMetaInformation } },
      token: string,
      initiallyUploadedData: PcafData
    ): void {
      cy.url().should('eq', getBaseUrl() + '/datasets');
      isDatasetAccepted();

      const dataMetaInformationOfReuploadedDataset = interception.response?.body as DataMetaInformation;
      cy.wrap(null).then(() =>
        validateReuploadedDataset(token, dataMetaInformationOfReuploadedDataset.dataId, initiallyUploadedData)
      );

      cy.url().should('eq', getBaseUrl() + '/datasets');
      cy.get('[data-test="datasets-table"]').should('be.visible');
    }

    it(
      'Create a company and a PCAF dataset via api, then re-upload it with the ' +
        'upload form in Edit mode and assure that it worked by validating a couple of values',
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = 'Company-Created-In-PCAF-Blanket-Test-' + uniqueCompanyMarker;

        cy.wrap(null)
          .then(() => setupCompanyAndFramework(testCompanyName))
          .then(({ token, storedCompany }) => {
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
            let initiallyUploadedData: PcafData;
            cy.wait('@getInitiallyUploadedData', {
              timeout: Cypress.env('medium_timeout_in_ms') as number,
            }).then((interception) => {
              initiallyUploadedData = (interception.response?.body as CompanyAssociatedDataPcafData).data;
            });
            cy.get('h1').should('contain', testCompanyName);
            cy.get('[data-test="submitButton"]').click();
            cy.wait('@resubmitPcafData', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then(
              (interception) => {
                handleDataResubmissionValidation(interception, token, initiallyUploadedData);
              }
            );
          });
      }
    );
  }
);
