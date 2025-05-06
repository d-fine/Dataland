import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, reader_name, reader_pw, reader_userId } from '@e2e/utils/Cypress';
import { getKeycloakToken, login } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { FRAMEWORKS_WITH_UPLOAD_FORM } from '@/utils/Constants';
import { assignCompanyRole } from '@e2e/utils/CompanyRolesUtils';
import { CompanyRole } from '@clients/communitymanager';
import { type StoredCompany } from '@clients/backend';

describeIf(
  'As a user, I expect to be able to upload data for one company for which I am company owner',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let storedCompany: StoredCompany;
    let testCompanyName: string;

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

    /*
     * Upload a company and set reader as companyOwner
     */
    before(() => {
      cy.intercept('**/company-role-assignments/**').as('postCompanyOwner');

      const uniqueCompanyMarker = Date.now().toString();
      testCompanyName = 'Company-Created-In-Company-Owner-Test-' + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw).then(async (token: string) => {
        storedCompany = await uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
        await assignCompanyRole(token, CompanyRole.CompanyOwner, storedCompany.companyId, reader_userId);
      });
      cy.wait('@postCompanyOwner', { timeout: Cypress.env('medium_timeout_in_ms') as number });
    });

    it('Upload a company, set a user as the company owner and then verify that the upload pages are displayed for that user', () => {
      login(reader_name, reader_pw);
      cy.visitAndCheckAppMount('/companies/' + storedCompany.companyId);
      cy.get('h1').should('contain', testCompanyName);
      cy.get('[data-test=toggleShowAll]').scrollIntoView();
      cy.get('[data-test=toggleShowAll]').contains('SHOW ALL').click();
      cy.scrollTo('top');
      checkFrameworks();
      cy.get(`div[data-test="lksg-summary-panel"] a[data-test="lksg-provide-data-button"]`).should('exist').click();
      cy.get(`div[data-pc-section="title"]`).should('contain', 'New Dataset - LkSG');
    });
  }
);
