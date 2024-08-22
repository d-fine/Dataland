import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, admin_userId, getBaseUrl, reader_name, reader_pw } from '@e2e/utils/Cypress';
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

    before(() => {
      cy.fixture('CompanyInformationWithVsmeData').then(function (jsonContent) {
        vsmeFixtures = jsonContent as Array<FixtureData<VsmeData>>;
      });

      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = 'Company-Created-In-Request-And-Grant-Test-' + uniqueCompanyMarker;

      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName))
          .then((storedCompany) => {
            storedTestCompany = storedCompany;
            return uploadVsmeFrameworkData(token, storedTestCompany.companyId, '2022', vsmeFixtures[0].t, []);
          })
          .then(() => uploadVsmeFrameworkData(token, storedTestCompany.companyId, '2023', vsmeFixtures[1].t, []))
          .then(() => uploadVsmeFrameworkData(token, storedTestCompany.companyId, '2024', vsmeFixtures[1].t, []));
      });
    });

    /**
     * Selects the row with the provided reporting period in the table to send an access request for it.
     * @param reportingPeriod
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

    it('Request access to private datasets, grant and decline those requests, then validate the access ', () => {
      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.visitAndCheckAppMount('/companies/' + storedTestCompany.companyId + '/frameworks/' + DataTypeEnum.Vsme);
      validateSubmitButton(false);

      selectRowInRequestableReportingPeriodsTable('2022');
      validateSubmitButton(true);

      selectRowInRequestableReportingPeriodsTable('2022');
      validateSubmitButton(false);

      selectRowInRequestableReportingPeriodsTable('2022');
      validateSubmitButton(true);

      selectRowInRequestableReportingPeriodsTable('2023');
      clickSubmitButton();

      cy.url().should('eq', getBaseUrl() + '/requests');
      // TODO => check dass deine zwei neuen requests für 2022 und 2023 in der Tabelle ganz oben stehen

      // TODO TIPP: use cy.pause() to pause at certain points in the test
      // TODO cy.ensureLoggedIn(admin_name, admin_pw); => might already be sufficient for re-login with different user

      /*

      TEST-PART-ALS-ADMIN
      TODO => log dich jetzt als admin ein => geh auf deine "request for my company" page
      TODO => du solltest jetzt die zwei access requests sehen => grante einen und decline einen
      TODO => checke dass durch deinen grant und decline die Status-badges jetzt korrekt sind

      TEST-PART-II-ALS-READER
      TODO => log dich jetzt wieder als reader ein
      TODO => gehe auf die view page für die test company und vsme
      TODO => du solltest jetzt den ge-granteten dataset Datensatz sehen können
      TODO => clicke auf den REQUEST ACCESS TO MORE DATASETS button => ein modal öffnet sich
      TODO => im modal sollte die declinete reporting period erscheinen, sowie die, für die du noch keine request gestellt hast

      Test zu Ende aus meiner Sicht
       */
    });
  }
);
