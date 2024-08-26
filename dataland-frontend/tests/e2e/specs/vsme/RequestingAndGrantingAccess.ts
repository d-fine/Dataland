import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, getBaseUrl, reader_name, reader_pw } from '@e2e/utils/Cypress';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { DataTypeEnum, type StoredCompany, type VsmeData } from '@clients/backend';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { uploadVsmeFrameworkData } from '@e2e/utils/FrameworkUpload';
import { type FixtureData } from '@sharedUtils/Fixtures';

describeIf(
  'As a user, I expect to request access to private datasets or grant or decline access to my private datasets ',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
    onlyExecuteWhenEurodatIsLive: true,
  },
  function (): void {
    let storedTestCompany: StoredCompany;
    let vsmeFixtures: FixtureData<VsmeData>[];
    const uniqueCompanyMarker = Date.now().toString();
    const testCompanyName = 'Company-Created-In-Request-And-Grant-Test-' + uniqueCompanyMarker;

    const reportingPeriodToBeGranted = '2022';
    const reportingPeriodToBeDeclined = '2023';
    const reportingPeriodWithoutRequest = '2024';

    before(() => {
      cy.fixture('CompanyInformationWithVsmeData').then(function (jsonContent) {
        vsmeFixtures = jsonContent as Array<FixtureData<VsmeData>>;
      });

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName))
          .then((storedCompany) => {
            storedTestCompany = storedCompany;
            return uploadVsmeFrameworkData(
              token,
              storedTestCompany.companyId,
              reportingPeriodToBeGranted,
              vsmeFixtures[0].t,
              []
            );
          })
          .then(() =>
            uploadVsmeFrameworkData(
              token,
              storedTestCompany.companyId,
              reportingPeriodToBeDeclined,
              vsmeFixtures[1].t,
              []
            )
          )
          .then(() =>
            uploadVsmeFrameworkData(
              token,
              storedTestCompany.companyId,
              reportingPeriodWithoutRequest,
              vsmeFixtures[1].t,
              []
            )
          );
      });
    });

    /**
     * Selects the row with the provided reporting period in the table to send an access request for it.
     * @param reportingPeriod defines which row will be selected to request data access
     */
    function selectRowInRequestableReportingPeriodsTable(reportingPeriod: string): void {
      cy.contains('td', reportingPeriod).siblings('td[data-p-selection-column="true"]').click();
    }

    /**
     * Validates that the button to submit access requests is enabled or disabled.
     * @param isExpectedToBeEnabled sets if the button is expected to be enabled or not
     * @returns the submit button
     */
    function validateSubmitButton(isExpectedToBeEnabled: boolean): Cypress.Chainable {
      const check = isExpectedToBeEnabled ? 'be.enabled' : 'be.disabled';
      return cy.get('button[data-test="requestAccessButton"]').should(check);
    }

    /**
     * Clicks the button to submit data access requests.
     */
    function clickSubmitButton(): void {
      validateSubmitButton(true).click();
    }

    /**
     * Validates that there are two access requests.
     * One for reportingPeriodToBeGranted and one for reportingPeriodToBeDeclined.
     */
    function validateThatAccessRequestsAreDisplayedInTable(): void {
      cy.get('tbody.p-datatable-tbody').within(() => {
        cy.get('tr')
          .eq(0)
          .within(() => {
            cy.get('td').eq(0).should('have.text', testCompanyName);
            cy.get('td').eq(1).should('have.text', 'VSME');
            cy.get('td').eq(2).should('have.text', reportingPeriodToBeGranted);
          });

        cy.get('tr')
          .eq(1)
          .within(() => {
            cy.get('td').eq(0).should('have.text', testCompanyName);
            cy.get('td').eq(1).should('have.text', 'VSME');
            cy.get('td').eq(2).should('have.text', reportingPeriodToBeDeclined);
          });
      });
    }

    /**
     * Clicks on the button in the access request table in specific row to grant or decline a request
     * @param reportingPeriod the reporting period to decline or grant
     * @param buttonText the text of the button that should be clicked
     */
    function clickButtonInAccessRequestTableForReportingPeriod(reportingPeriod: string, buttonText: string): void {
      cy.get('tbody.p-datatable-tbody')
        .find('tr')
        .contains('td', reportingPeriod)
        .parent()
        .contains('button', buttonText)
        .click();
    }

    /**
     * Checks that a specific row in the access request table has a certain badge with a give class and text.
     * @param reportingPeriod the reporting period to identify the row
     * @param badgeClass the badge class that should exist
     * @param badgeText the text the badge should have
     */
    function validateAccessRequestForReportingPeriodTableHasBadgeWithText(
      reportingPeriod: string,
      badgeClass: string,
      badgeText: string
    ): void {
      cy.get('tbody.p-datatable-tbody')
        .find('tr')
        .contains('td', reportingPeriod)
        .parent()
        .find('div.' + badgeClass)
        .should('have.text', badgeText);
    }

    /**
     * Checks that on a company page for a dataset the reportingPeriodToBeGranted appears in the header,
     * which indicates that the dataset is visible to the user.
     */
    function validateThatReportingPeriodWithGrantedAccessIsDisplayed(): void {
      cy.get('table.p-datatable-table thead th').contains('span', reportingPeriodToBeGranted).should('exist');
    }

    /**
     * On a company page for a dataset this method clicks on the request access to more datasets button
     * and then checks that in the dialog the right reportingPeriods appear.
     */
    function clickToRequestMoreReportingPeriodsAndVerifyThatTheCorrectYearsAreDisplayed(): void {
      cy.contains('button', 'REQUEST ACCESS TO MORE DATASETS').click();

      cy.get('div.p-dialog table.p-datatable-table tbody').within(() => {
        cy.get('tr').eq(0).contains('td', reportingPeriodToBeDeclined).should('exist');
        cy.get('tr').eq(1).contains('td', reportingPeriodWithoutRequest).should('exist');
      });
    }

    /**
     * Performs the first steps of the data reader of this test.
     */
    function dataReaderChecksReportingPeriodTableAndCreatesAccessRequests(): void {
      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.visitAndCheckAppMount('/companies/' + storedTestCompany.companyId + '/frameworks/' + DataTypeEnum.Vsme);
      validateSubmitButton(false);

      selectRowInRequestableReportingPeriodsTable(reportingPeriodToBeGranted);
      validateSubmitButton(true);

      selectRowInRequestableReportingPeriodsTable(reportingPeriodToBeGranted);
      validateSubmitButton(false);

      selectRowInRequestableReportingPeriodsTable(reportingPeriodToBeGranted);
      validateSubmitButton(true);

      selectRowInRequestableReportingPeriodsTable(reportingPeriodToBeDeclined);
      clickSubmitButton();

      cy.url().should('eq', getBaseUrl() + '/requests');

      validateThatAccessRequestsAreDisplayedInTable();
    }

    /**
     * Performs the steps of the data admin in its role as the company owner.
     */
    function dataAdminGrantsOneAndDeclinesOneAccessRequest(): void {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.wait(10000) // TODO debugging test in CD
      cy.visitAndCheckAppMount('/companyrequests');
      cy.wait(10000) // TODO debugging test in CD

      clickButtonInAccessRequestTableForReportingPeriod(reportingPeriodToBeGranted, 'Grant');
      validateAccessRequestForReportingPeriodTableHasBadgeWithText(
        reportingPeriodToBeGranted,
        'badge-light-green',
        'Granted'
      );

      clickButtonInAccessRequestTableForReportingPeriod(reportingPeriodToBeDeclined, 'Decline');
      validateAccessRequestForReportingPeriodTableHasBadgeWithText(
        reportingPeriodToBeDeclined,
        'badge-brown',
        'Declined'
      );
    }

    /**
     * Performs the last steps of the data reader in this test.
     */
    function dataReaderChecksThatOneReportingPeriodIsVisibleAndTwoReportingPeriodsCanBeRequested(): void {
      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.visitAndCheckAppMount('/companies/' + storedTestCompany.companyId + '/frameworks/' + DataTypeEnum.Vsme);
      validateThatReportingPeriodWithGrantedAccessIsDisplayed();
      clickToRequestMoreReportingPeriodsAndVerifyThatTheCorrectYearsAreDisplayed();
    }

    it('Request access to private datasets, grant and decline those requests, then validate the access ', () => {
      dataReaderChecksReportingPeriodTableAndCreatesAccessRequests();
      dataAdminGrantsOneAndDeclinesOneAccessRequest();
      dataReaderChecksThatOneReportingPeriodIsVisibleAndTwoReportingPeriodsCanBeRequested();
    });
  }
);
