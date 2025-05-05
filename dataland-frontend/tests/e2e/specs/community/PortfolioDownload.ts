import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { IdentifierType } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assertDefined } from '@/utils/TypeScriptUtils';

describeIf(
  'As a user I want to download a portfolio report file with dynamically loaded reporting periods',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    const portfolioName = `Download Portfolio ${Date.now()}`;
    let permId: string;

    before(() => {
      getKeycloakToken(admin_name, admin_pw).then(async (token) => {
        const company = generateDummyCompanyInformation(`Company ${Date.now()}`);
        permId = assertDefined(company.identifiers[IdentifierType.PermId][0]);
        await uploadCompanyViaApi(token, company);
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/portfolios');
    });

    it('Creates a portfolio and downloads a report using dynamically loaded reporting periods', () => {
      cy.closeCookieBannerIfItExists();

      cy.get('[data-test="addNewPortfolio"]').click();
      cy.get('[name="portfolioName"]').type(portfolioName);
      cy.get('[name="company-identifiers"]').type(permId);
      cy.get('[data-test="addCompanies"]').click();
      cy.get('[data-test="saveButton"]').click();

      cy.get('[data-test="portfolios"] [data-pc-name="tabpanel"]').contains(portfolioName).click();
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="download-portfolio"]`).click();

      /**
       * Helper function to select a framework and validate reporting periods
       * @param framework selected framework
       * @param periods selected periods
       */
      function selectFrameworkAndValidatePeriods(framework: string, periods: string[]): void {
        cy.get('[data-test="FrameworkSelection"]').click();
        cy.get('.p-dropdown-item').contains(framework).click();

        cy.get('[data-test="ReportingPeriods"]')
          .should('exist')
          .within(() => {
            periods.forEach((period) => {
              cy.contains(period).should('exist');
            });
          });
      }

      selectFrameworkAndValidatePeriods('EU Taxonomy Nuclear and Gas', [
        'No reporting periods available for Nuclear and Gas and selected Portfolio.',
      ]);
      selectFrameworkAndValidatePeriods('EU Taxonomy Financials', ['2021', '2024']);
      selectFrameworkAndValidatePeriods('EU Taxonomy Non-Financials', ['2020', '2023']);
      selectFrameworkAndValidatePeriods('SFDR', ['2020', '2024']);

      cy.get('.toggle-chip', { timeout: 5000 }).should('exist');
      cy.get('.toggle-chip').first().click();
      cy.get('[name="fileType"]').click();
      cy.get('.p-dropdown-item').contains('CSV').click();

      cy.window().then((win) => {
        cy.stub(win, 'Blob');
        cy.stub(win.URL, 'createObjectURL').returns('blob:http://localhost/fake-download-url');
      });

      cy.get('[data-test="downloadButton"]').should('not.be.disabled').click();

      cy.get('[data-test="errorMessage"]').should('not.exist');
    });
  }
);
