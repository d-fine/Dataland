import { describeIf } from '@e2e/support/TestUtility';
import { admin_name, admin_pw, reader_name, reader_pw, reader_userId } from '@e2e/utils/Cypress';
import { ensureLoggedIn, getKeycloakToken } from '@e2e/utils/Auth';
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
        cy.get(`[data-test="${frameworkName}-provide-data-button"]`).should('exist');
      });
    }

    /**
     * Helper to create a company and assign the company owner role to the reader user.
     */
    function setupCompanyAndAssignRole(
      companyName: string
    ): Cypress.Chainable<{ token: string; company: StoredCompany }> {
      return cy.wrap(null).then(() => {
        return getKeycloakToken(admin_name, admin_pw).then((token) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((company) => {
            return assignCompanyRole(token, CompanyRole.CompanyOwner, company.companyId, reader_userId).then(() => ({
              token,
              company,
            }));
          });
        });
      });
    }

    before(() => {
      const uniqueCompanyMarker = Date.now().toString();
      testCompanyName = 'Company-Created-In-Company-Owner-Test-' + uniqueCompanyMarker;

      setupCompanyAndAssignRole(testCompanyName).then(({ token, company }) => {
        storedCompany = company;
        cy.request({
          method: 'GET',
          url: `/community/company-role-assignments?role=${CompanyRole.CompanyOwner}&companyId=${storedCompany.companyId}&userId=${reader_userId}`,
          headers: { Authorization: `Bearer ${token}` },
          failOnStatusCode: false,
        })
          .its('body')
          .should((body) => {
            expect(
              Array.isArray(body) &&
                body.some((companyRoleAssignment) => companyRoleAssignment.companyRole === CompanyRole.CompanyOwner)
            ).to.be.true;
          });
      });
    });

    it('Upload a company, set a user as the company owner and then verify that the upload pages are displayed for that user', () => {
      ensureLoggedIn(reader_name, reader_pw);
      cy.intercept('GET', '**/community/company-role-assignments**').as('getCompanyRoles');
      cy.visitAndCheckAppMount('/companies/' + storedCompany.companyId);
      cy.wait('@getCompanyRoles');
      cy.get('h1').should('contain', testCompanyName);
      cy.get('[data-test=toggleShowAll]').scrollIntoView();
      cy.get('[data-test=toggleShowAll]').contains('SHOW ALL').click();
      cy.scrollTo('top');
      checkFrameworks();
      cy.get(`[data-test="lksg-provide-data-button"]`).should('exist').click();
      cy.get(`div[data-pc-section="title"]`).should('contain', 'New Dataset - LkSG');
    });
  }
);
