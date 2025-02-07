import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, reader_name, reader_pw, reader_userId } from '@e2e/utils/Cypress';
import { getKeycloakToken, login, logout } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { FRAMEWORKS_WITH_UPLOAD_FORM } from '@/utils/Constants';
import { assignCompanyRole } from '@e2e/utils/CompanyRolesUtils';
import { CompanyRole } from '@clients/communitymanager';

describeIf(
  'As a user, I expect to be able to upload data for one company for which I am company owner',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    /**
     * This method verifies that the summary panel for each framework is presented as expected
     */
    function checkFrameworks(): void {
      FRAMEWORKS_WITH_UPLOAD_FORM.forEach((frameworkName) => {
        const frameworkSummaryPanelSelector = `div[data-test="${frameworkName}-summary-panel"]`;
        cy.get(frameworkSummaryPanelSelector).should('exist');
        cy.get(`${frameworkSummaryPanelSelector} a[data-test="${frameworkName}-provide-data-button"]`).should('exist');
      });
    }
    it('Upload a company, set a user as the company owner and then verify that the upload pages are displayed for that user', () => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = 'Company-Created-In-Company-Owner-Test-' + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
          cy.intercept('**/company-role-assignments/**').as('postCompanyOwner');
          void assignCompanyRole(token, CompanyRole.CompanyOwner, storedCompany.companyId, reader_userId);
          cy.wait('@postCompanyOwner', { timeout: Cypress.env('medium_timeout_in_ms') as number });
          logout();
          login(reader_name, reader_pw);
          cy.visitAndCheckAppMount('/companies/' + storedCompany.companyId);
          cy.get('h1').should('contain', testCompanyName);
          checkFrameworks();
          cy.get(`div[data-test="lksg-summary-panel"] a[data-test="lksg-provide-data-button"]`).should('exist').click();

          cy.get(`div[data-pc-section="title"]`).should('contain', 'New Dataset - LkSG');
        });
      });
    });
  }
);
