import { admin_name, admin_pw, premium_user_name, premium_user_pw } from '@e2e/utils/Cypress';
import { type Interception } from 'cypress/types/net-stubbing';
import { type SingleDataRequest } from '@clients/communitymanager';
import { describeIf } from '@e2e/support/TestUtility';
import { DataTypeEnum, type LksgData, type StoredCompany } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { uploadFrameworkDataForPublicToolboxFramework } from '@e2e/utils/FrameworkUpload';
import { type FixtureData, getPreparedFixture } from '@sharedUtils/Fixtures';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { singleDataRequestPage } from '@sharedUtils/components/SingleDataRequest';
import LksgBaseFrameworkDefinition from '@/frameworks/lksg/BaseFrameworkDefinition';
import { verifyOnSingleRequestPage } from '@sharedUtils/components/DataRequest.ts';

describeIf(
  'As a premium user, I want to be able to navigate to the single data request page and submit a request',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    const uniqueCompanyMarker = Date.now().toString();
    const testCompanyName = 'Company-for-single-data-request' + uniqueCompanyMarker;
    let testStoredCompany: StoredCompany;
    let lksgPreparedFixtures: Array<FixtureData<LksgData>>;
    const testMessage = 'Frontend test message';
    const testYear = '2023';
    const testEmail = 'dummy@example.com';

    /**
     * Uploads a company with lksg data
     * @param reportingPeriod the year for which the data is uploaded
     */
    function uploadCompanyWithData(reportingPeriod: string): void {
      getKeycloakToken(admin_name, admin_pw).then(async (token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
          testStoredCompany = storedCompany;
          return uploadFrameworkDataForCompany(storedCompany.companyId, reportingPeriod);
        });
      });
    }
    /**
     * Sets the status of a single data request from open to answered
     * @param companyId id of the company
     * @param reportingPeriod the year for which the framework is uploaded
     */
    function uploadFrameworkDataForCompany(companyId: string, reportingPeriod: string): void {
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadFrameworkDataForPublicToolboxFramework(
          LksgBaseFrameworkDefinition,
          token,
          companyId,
          reportingPeriod,
          getPreparedFixture('LkSG-date-2022-07-30', lksgPreparedFixtures).t
        );
      });
    }
    before(() => {
      cy.fixture('CompanyInformationWithLksgPreparedFixtures').then(function (jsonContent) {
        lksgPreparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
        uploadCompanyWithData('2020');
      });
    });
    beforeEach(() => {
      cy.ensureLoggedIn(premium_user_name, premium_user_pw);
    });

    it('Navigate to the single request page via the company cockpit', () => {
      cy.visitAndCheckAppMount(`/companies/${testStoredCompany.companyId}`);
      cy.get('[data-test="singleDataRequestButton"]').click();
      cy.url().should('contain', `/singledatarequest/${testStoredCompany.companyId}`);
    });

    it('Navigate to the single request page via the view page and verify that the viewed framework is preselected.', () => {
      cy.visitAndCheckAppMount(`/companies/${testStoredCompany.companyId}/frameworks/${DataTypeEnum.Lksg}`);
      cy.get('[data-test="singleDataRequestButton"]').click();
      cy.url().should('contain', `/singledatarequest/${testStoredCompany.companyId}`);
      cy.get('[data-test="datapoint-framework"]').find('span').should('have.text', 'LkSG');
    });

    it('Fill out the request page and check correct validation, request and success message', () => {
      cy.intercept('POST', '**/community/requests/single').as('postRequestData');
      cy.visitAndCheckAppMount(`/singleDataRequest/${testStoredCompany.companyId}`);
      checkCompanyInfoSheet();
      checkValidation();
      singleDataRequestPage.chooseReportingPeriod(testYear);
      checkDropdownLabels();
      singleDataRequestPage.chooseFrameworkLksg();

      cy.get('[data-test="contactEmail"]').type(testEmail);
      cy.get('[data-test="dataRequesterMessage"]').type(testMessage);
      cy.get('[data-test="acceptConditionsCheckbox"]').should('be.visible');
      cy.get('[data-test="acceptConditionsCheckbox"]').click();
      cy.get("button[type='submit']").click();
      cy.wait('@postRequestData', { timeout: Cypress.env('short_timeout_in_ms') as number }).then((interception) => {
        checkIfRequestBodyIsValid(interception);
      });
      checkCompanyInfoSheet();
      cy.get('[data-test=submittedDiv]').should('exist');
      cy.get('[data-test=requestStatusText]').should('contain.text', 'Submitting your data request was successful.');
      cy.get('[data-test="backToCompanyPageButton"]').click();
      cy.url().should('contain', '/companies/');
      checkCompanyInfoSheet();

      checkThatRequestIsOnRequestPage();
      withDrawRequestAndCheckThatItsWithdrawn();
    });

    /**
     * Verifies that the request appears on the overview and single request page
     */
    function checkThatRequestIsOnRequestPage(): void {
      cy.visit('/requests');
      cy.url({ timeout: Cypress.env('long_timeout_in_ms') as number }).should('contain', '/requests');
      cy.get(`td:contains("${testStoredCompany.companyInformation.companyName}")`).first().scrollIntoView();
      cy.get(`td:contains("${testStoredCompany.companyInformation.companyName}")`).first().click();

      verifyOnSingleRequestPage(testStoredCompany.companyInformation.companyName, false);
      cy.get('[data-test="notifyMeImmediatelyInput"]').click();
      cy.reload(); // Check if the data was persisted in the backend
      cy.get('[data-test="notifyMeImmediatelyInput"]').should('have.class', 'p-inputswitch-checked');
    }

    /**
     * Withdraw the request and check that it succeeded.
     */
    function withDrawRequestAndCheckThatItsWithdrawn(): void {
      cy.get('a:contains("Withdraw request")').scrollIntoView();
      cy.get('a:contains("Withdraw request")').click();
      cy.get('[data-test="successModal"] button:contains("CLOSE")').click();
      cy.get('[data-test="card_requestIs"]').should('contain.text', 'Request is:Withdrawnand Access is:Public');
      cy.get('[data-test="back-button"]').scrollIntoView();
      cy.get('[data-test="back-button"]').click();
      cy.get(`tr:contains("${testStoredCompany.companyInformation.companyName}")`).should('contain.text', 'Withdrawn');
    }

    /**
     * Checks if the request body that is sent to the backend is valid and matches the given information
     * @param interception the object of interception with the backend
     */
    function checkIfRequestBodyIsValid(interception: Interception): void {
      type SingleDataRequestTypeInInterception = Omit<SingleDataRequest, 'reportingPeriods' | 'contacts'> & {
        reportingPeriods: string[];
        contacts: string[];
      };
      if (interception.request !== undefined) {
        const requestBody = interception.request.body as SingleDataRequestTypeInInterception;
        const expectedRequest: SingleDataRequestTypeInInterception = {
          companyIdentifier: testStoredCompany.companyId,
          dataType: DataTypeEnum.Lksg,
          reportingPeriods: [testYear],
          contacts: [testEmail],
          message: testMessage,
          notifyMeImmediately: false,
        };
        expect(requestBody).to.deep.equal(expectedRequest);
      }
    }

    /**
     * Checks if all expected human-readable labels are visible in the dropdown options
     */
    function checkDropdownLabels(): void {
      cy.get("[data-test='datapoint-framework']").click();
      FRAMEWORKS_WITH_VIEW_PAGE.forEach((framework) => {
        cy.get('.p-dropdown-item').contains(humanizeStringOrNumber(framework)).should('exist');
      });
      cy.get("[data-test='datapoint-framework']").click();
    }

    /**
     * Checks basic validation
     */
    function checkValidation(): void {
      cy.get("button[type='submit']").click();
      cy.get("div[data-test='reportingPeriods'] p[data-test='reportingPeriodErrorMessage']")
        .should('be.visible')
        .should('contain.text', 'Select at least one reporting period to submit your request');

      cy.get("div[data-test='selectFramework'] li[data-message-type='validation']")
        .should('be.visible')
        .should('contain.text', 'Select a framework to submit your request');
    }

    /**
     * Checks if the information on the company banner is correct
     */
    function checkCompanyInfoSheet(): void {
      cy.get("[data-test='companyNameTitle']").should('contain.text', testCompanyName);
    }
  }
);
