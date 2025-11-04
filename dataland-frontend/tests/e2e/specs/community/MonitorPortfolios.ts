import { IdentifierType } from '@clients/backend';
import { describeIf } from '@e2e/support/TestUtility.ts';
import { getKeycloakToken } from '@e2e/utils/Auth.ts';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload.ts';
import { admin_name, admin_pw } from '@e2e/utils/Cypress.ts';

/**
 * Test function for creating portfolio and monitor it
 */
function testPatchMonitoring(portfolioName: string, permId: string, frameworkValue: string): void {
  cy.get('[data-test="add-portfolio"]').click({
    timeout: Cypress.env('medium_timeout_in_ms'),
  });

  // Add portfolio dialog
  cy.get('.p-dialog').find('.p-dialog-header').contains('Add Portfolio');

  cy.get('.p-dialog')
    .find('.portfolio-dialog-content')
    .within(() => {
      cy.get('[data-test="portfolio-name-input"]').type(portfolioName);
      cy.get('[data-test="portfolio-dialog-save-button"]').should('be.disabled');
      cy.get('[data-test="company-identifiers-input"]:visible').type(permId);
      cy.get('[data-test="portfolio-dialog-add-companies"]').click();
      cy.wait('@forCompanyValidation');
      cy.get('[data-test="portfolio-dialog-save-button"]').click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
    });

  // Navigate to portfolio and open monitoring
  cy.wait(['@getEnrichedPortfolio', '@getPortfolioNames']);
  cy.get(`[data-test="${portfolioName}"]`).click();
  cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="monitor-portfolio"]`).click({
    timeout: Cypress.env('medium_timeout_in_ms') as number,
  });

  // Monitoring dialog
  cy.get('.p-dialog').find('.p-dialog-header').contains(`Monitoring of`);

  cy.get('.p-dialog')
    .find('.portfolio-monitoring-content')
    .within(() => {
      cy.get('[data-test="activateMonitoringToggle"]').click();
    });

  cy.get('.p-dialog')
    .find('.portfolio-monitoring-content')
    .within(() => {
      cy.get('[data-test="frameworkSelection"]')
        .contains('EU Taxonomy')
        .parent()
        .find('input[type="checkbox"]')
        .click();
      cy.get('[data-test="saveChangesButton"]').click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
    });

  // Assert patchMonitoring payload
  cy.wait('@patchMonitoring')
    .its('request.body')
    .should((body) => {
      expect(body.isMonitored).to.be.true;
      expect(body.monitoredFrameworks).to.include(frameworkValue);
    });

  // Cleanup: delete portfolio
  cy.visitAndCheckAppMount('/portfolios');
  cy.wait(['@getEnrichedPortfolio', '@getPortfolioNames']);

  cy.get(`[data-test="${portfolioName}"]`).click();
  cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="edit-portfolio"]`).click({
    timeout: Cypress.env('medium_timeout_in_ms') as number,
  });

  cy.get('.p-dialog')
    .find('.portfolio-dialog-content')
    .within(() => {
      cy.get('[data-test="portfolio-dialog-delete-button"]').click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
    });

  cy.wait(['@getEnrichedPortfolio', '@getPortfolioNames']);
  cy.get(`[data-test="${portfolioName}"]`).should('not.exist');
}

describe('Portfolio Monitoring Modal', () => {
  describeIf(
    'As a user I want to be able to monitor a portfolio',
    { executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'] },
    () => {
      let permIdNonFinancial: string;
      let permIdFinancial: string;
      let permIdNoSector: string;
      const companyTimestamp = Date.now();
      let portfolioTimestamp: number;
      let nonFinancialPortfolio: string;
      let financialPortfolio: string;
      let nonSectorPortfolio: string;
      const companyNameNonFinancial = `Test Co. NonFin ${companyTimestamp}`;
      const companyNameFinancial = `Test Co. Fin ${companyTimestamp}`;
      const companyNameNoSector = `Test Co. NoSec ${companyTimestamp}`;

      before(() => {
        getKeycloakToken(admin_name, admin_pw).then(async (token) => {
          const nonFinancialCompany = generateDummyCompanyInformation(companyNameNonFinancial);
          const financialCompany = generateDummyCompanyInformation(companyNameFinancial, 'financials');
          const noSectorCompany = generateDummyCompanyInformation(companyNameNoSector, null as unknown as string);

          permIdNonFinancial = nonFinancialCompany.identifiers[IdentifierType.PermId][0]!;
          await uploadCompanyViaApi(token, nonFinancialCompany);

          permIdFinancial = financialCompany.identifiers[IdentifierType.PermId][0]!;
          await uploadCompanyViaApi(token, financialCompany);

          permIdNoSector = noSectorCompany.identifiers[IdentifierType.PermId][0]!;
          await uploadCompanyViaApi(token, noSectorCompany);
        });
      });

      beforeEach(() => {
        cy.ensureLoggedIn(admin_name, admin_pw);
        cy.visitAndCheckAppMount('/portfolios');
        cy.intercept('PATCH', '**/users/portfolios/**/monitoring').as('patchMonitoring');
        cy.intercept('GET', '**/users/portfolios/names').as('getPortfolioNames');
        cy.intercept('GET', '**/users/portfolios/**/enriched-portfolio').as('getEnrichedPortfolio');
        cy.intercept('POST', '**/api/companies/validation').as('forCompanyValidation');

        portfolioTimestamp = Date.now();
        nonFinancialPortfolio = `nonFinancialPortfolio ${portfolioTimestamp}`;
        financialPortfolio = `financialPortfolio ${portfolioTimestamp}`;
        nonSectorPortfolio = `NonSectorPortfolio ${portfolioTimestamp}`;
      });

      it('Monitoring yields bulk data request when inputs are valid for non financial company', () => {
        testPatchMonitoring(nonFinancialPortfolio, permIdNonFinancial, 'eutaxonomy');
      });

      it('Monitoring yields bulk data request when inputs are valid for financial company', () => {
        testPatchMonitoring(financialPortfolio, permIdFinancial, 'eutaxonomy');
      });

      it('Monitoring yields bulk data request when inputs are valid for non sector company', () => {
        testPatchMonitoring(nonSectorPortfolio, permIdNoSector, 'eutaxonomy');
      });
    }
  );
});
