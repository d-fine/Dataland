import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { IdentifierType } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assertDefined } from '@/utils/TypeScriptUtils';

/**
 * Adds a portfolio via the UI.
 * @param name - The name of the portfolio to add.
 * @param companyId - The company identifier to add.
 */
function addPortfolio(name: string, companyId: string): void {
  cy.get('button[data-test="add-portfolio"]').click({
    timeout: Cypress.env('medium_timeout_in_ms') as number,
  });
  cy.get('.p-dialog').within(() => {
    cy.get('.p-dialog-header').contains('Add Portfolio');
    cy.get('.portfolio-dialog-content').should('exist');
    cy.get('[data-test="portfolio-name-input"]:visible').type(name);
    cy.get('[data-test="portfolio-dialog-save-button"]').should('be.disabled');
    cy.get('[data-test="invalidIdentifierErrorMessage"]').should('not.exist');
    cy.get('[data-test="company-identifiers-input"]:visible').type(companyId);
    cy.get('[data-test="invalidIdentifierErrorMessage"]:visible').should('not.exist');
    cy.get('[data-test="portfolio-dialog-add-companies"]').click();
    cy.waitUntil(() => cy.get('[data-test="company-identifiers-input"]:visible').should('be.empty'));
    cy.get('[data-test="invalidIdentifierErrorMessage"]').should('not.exist');
    cy.get('[data-test="portfolio-dialog-save-button"]').should('not.be.disabled');
    cy.get('[data-test="portfolio-dialog-save-button"]').click({
      timeout: Cypress.env('medium_timeout_in_ms') as number,
    });
  });
  cy.wait(['@getEnrichedPortfolio', '@getPortfolioNames']);
  cy.get(`[data-test="${name}"]`).should('exist');
}

/**
 * Deletes a portfolio via the UI.
 * @param name - The name of the portfolio to delete.
 */
function deletePortfolio(name: string): void {
  cy.get(`[data-test="${name}"]`).should('exist').click();
  cy.get(`[data-test="portfolio-${name}"] [data-test="edit-portfolio"]`).click({
    timeout: Cypress.env('medium_timeout_in_ms') as number,
  });
  cy.get('[data-test="portfolio-dialog-delete-button"]').click({
    timeout: Cypress.env('medium_timeout_in_ms') as number,
  });
  cy.get(`[data-test="${name}"]`).should('not.exist');
}

describeIf(
  'As a user I want to be able to create, edit, and delete my portfolios',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let permIdOfExistingCompany: string;
    let permIdOfSecondCompany: string;
    const companyTimestamp = Date.now();
    let portfolioTimestamp: number;
    let portfolioName: string;
    let secondPortfolioTimestamp: number;
    let secondPortfolioName: string;
    let editedSecondPortfolioName: string;

    before(() => {
      getKeycloakToken(admin_name, admin_pw).then(async (token) => {
        const companyToUpload = generateDummyCompanyInformation(`Test Co. ${companyTimestamp}`);
        permIdOfExistingCompany = assertDefined(companyToUpload.identifiers[IdentifierType.PermId][0]);
        await uploadCompanyViaApi(token, companyToUpload);
        const secondCompanyToUpload = generateDummyCompanyInformation(`Test Co.2 ${companyTimestamp}`);
        permIdOfSecondCompany = assertDefined(secondCompanyToUpload.identifiers[IdentifierType.PermId][0]);
        await uploadCompanyViaApi(token, secondCompanyToUpload);
      });
    });

    beforeEach(() => {
      portfolioTimestamp = Date.now();
      portfolioName = `E2E-Test-Portfolio-${portfolioTimestamp}`;
      secondPortfolioTimestamp = portfolioTimestamp + 1;
      secondPortfolioName = `E2E-Test-Portfolio-Second-${secondPortfolioTimestamp}`;
      editedSecondPortfolioName = `${secondPortfolioName}-Edited`;

      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/portfolios');
      cy.intercept('POST', '**/community/requests/bulk').as('postBulkRequest');
      cy.intercept('GET', '**/users/portfolios/names').as('getPortfolioNames');
      cy.intercept('GET', '**/users/portfolios/**/enriched-portfolio').as('getEnrichedPortfolio');
      cy.intercept('POST', '**/api/companies/validation').as('companyValidation');
      cy.window().then((win) => {
        cy.stub(win, 'confirm').returns(true);
      });
    });

    it('Creates, edits and deletes two portfolios, verifying correct display and deletion', () => {
      // Create two portfolios and verify correct display
      addPortfolio(portfolioName, permIdOfExistingCompany);
      addPortfolio(secondPortfolioName, permIdOfExistingCompany);
      cy.get(`[data-test="portfolio-${secondPortfolioName}"]`).should('be.visible');
      cy.get(`[data-test="portfolio-${portfolioName}"]`).should('not.be.visible');
      cy.get(`[data-test="portfolio-${secondPortfolioName}"] .p-datatable-tbody tr`).should('have.length', 1);

      // Edit the second portfolio and verify it is displayed afterward
      cy.get(`[data-test="${secondPortfolioName}"]`).click();
      cy.get(`[data-test="portfolio-${secondPortfolioName}"] [data-test="edit-portfolio"]`).click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
      cy.get('.p-dialog').within(() => {
        cy.get('.p-dialog-header').contains('Edit Portfolio');
        cy.get('.portfolio-dialog-content').within(() => {
          cy.get('[data-test="portfolio-name-input"]').clear();
          cy.get('[data-test="portfolio-name-input"]:visible').type(editedSecondPortfolioName);
          cy.get('[data-test="company-identifiers-input"]').type(permIdOfSecondCompany);
          cy.get('[data-test="portfolio-dialog-add-companies"]:visible').click();
          cy.get('#existing-company-identifiers li').should('have.length', 2);
          cy.get('[data-test="portfolio-dialog-save-button"]').click({
            timeout: Cypress.env('medium_timeout_in_ms') as number,
          });
        });
      });
      cy.wait(['@getEnrichedPortfolio', '@getPortfolioNames'], { responseTimeout: 300000 });
      cy.get(`[data-test="portfolio-${editedSecondPortfolioName}"]`).should('be.visible');
      cy.get(`[data-test="portfolio-${portfolioName}"]`).should('not.be.visible');
      cy.get(`[data-test="portfolio-${editedSecondPortfolioName}"] .p-datatable-tbody tr`).should('have.length', 2);

      // Go to a company in the second portfolio, return, and verify the second portfolio tab is displayed
      cy.get(`[data-test="view-company-button"]:visible`).first().find('.p-button-label').click();
      cy.url().should('include', '/companies/');
      cy.visit('/portfolios');
      cy.wait(['@getEnrichedPortfolio', '@getPortfolioNames']);
      cy.get(`[data-test="portfolio-${editedSecondPortfolioName}"]`).should('be.visible');
      cy.get(`[data-test="portfolio-${portfolioName}"]`).should('not.be.visible');

      deletePortfolio(editedSecondPortfolioName);
      deletePortfolio(portfolioName);
    });
  }
);
