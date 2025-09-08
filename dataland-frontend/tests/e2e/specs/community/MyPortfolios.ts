import { admin_name, admin_pw } from '@e2e/utils/Cypress';
import { describeIf } from '@e2e/support/TestUtility';
import { IdentifierType } from '@clients/backend';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { generateDummyCompanyInformation, uploadCompanyViaApi } from '@e2e/utils/CompanyUpload';
import { assertDefined } from '@/utils/TypeScriptUtils';

describeIf(
  'As a user I want to be able to create, edit, and delete my portfolios',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let permIdOfExistingCompany: string;
    let permIdOfSecondCompany: string;
    const invalidCompanyId = ',this1san1nval1d1d';
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
    });

    it('Creates, edits and deletes two portfolios, verifying correct display and deletion', () => {
      // Create first portfolio
      cy.get('button[data-test="add-portfolio"]').click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
      cy.get('.p-dialog').within(() => {
        cy.get('.p-dialog-header').contains('Add Portfolio');
        cy.get('.portfolio-dialog-content').should('exist');
        cy.get('[data-test="portfolio-name-input"]:visible').type(portfolioName);
        cy.get('[data-test="portfolio-dialog-save-button"]').should('be.disabled');
        cy.get('[data-test="invalidIdentifierErrorMessage"]').should('not.exist');
        cy.get('[data-test="company-identifiers-input"]:visible').type(invalidCompanyId);
        cy.get('[data-test="portfolio-dialog-add-companies"]').click();
        cy.wait('@companyValidation');
        cy.get('[data-test="invalidIdentifierErrorMessage"]').should('be.visible');
        cy.get('[data-test="company-identifiers-input"]:visible').clear();
        cy.get('[data-test="company-identifiers-input"]:visible').type(permIdOfExistingCompany);
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
      cy.get(`[data-test="${portfolioName}"]`).should('exist');

      // Create second portfolio

      cy.get('button[data-test="add-portfolio"]').click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
      cy.get('.p-dialog').within(() => {
        cy.get('.p-dialog-header').contains('Add Portfolio');
        cy.get('.portfolio-dialog-content').should('exist');
        cy.get('[data-test="portfolio-name-input"]:visible').type(secondPortfolioName);
        cy.get('[data-test="portfolio-dialog-save-button"]').should('be.disabled');
        cy.get('[data-test="invalidIdentifierErrorMessage"]').should('not.exist');
        cy.get('[data-test="company-identifiers-input"]:visible').type(permIdOfExistingCompany);
        cy.get('[data-test="portfolio-dialog-add-companies"]').click();
        cy.wait('@companyValidation');
        cy.get('[data-test="invalidIdentifierErrorMessage"]').should('not.exist');
        cy.get('[data-test="portfolio-dialog-save-button"]').should('not.be.disabled');
        cy.get('[data-test="portfolio-dialog-save-button"]').click({
          timeout: Cypress.env('medium_timeout_in_ms') as number,
        });
      });
      cy.wait(['@getEnrichedPortfolio', '@getPortfolioNames']);
      cy.get(`[data-test="${secondPortfolioName}"]`).should('exist');
      cy.get(`[data-test="portfolio-${secondPortfolioName}"]`).should('be.visible');
      cy.get(`[data-test="portfolio-${portfolioName}"]`).should('not.be.visible');

      // Edit second portfolio
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
          cy.get('[data-test="portfolio-dialog-save-button"]').click({
            timeout: Cypress.env('medium_timeout_in_ms') as number,
          });
        });
      });
      cy.wait(['@getEnrichedPortfolio', '@getPortfolioNames']);

      // Check that the second portfolio is displayed and not the first
      cy.get(`[data-test="portfolio-${editedSecondPortfolioName}"]`).should('be.visible');
      cy.get(`[data-test="portfolio-${portfolioName}"]`).should('not.be.visible');

      // Go to a company in the second portfolio, return and check that the second portfolio is still active
      cy.get(`[data-test="portfolio-${editedSecondPortfolioName}"] a`).first().click();
      cy.url().should('include', '/companies/');
      cy.visit('/portfolios');
      cy.wait(['@getEnrichedPortfolio', '@getPortfolioNames']);
      cy.get(`[data-test="portfolio-${editedSecondPortfolioName}"]`).should('be.visible');
      cy.get(`[data-test="portfolio-${portfolioName}"]`).should('not.be.visible');

      // Delete the second portfolio
      cy.get(`[data-test="portfolio-${editedSecondPortfolioName}"] [data-test="edit-portfolio"]`).click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
      cy.window().then((win) => {
        cy.stub(win, 'confirm').returns(true);
      });
      cy.get('[data-test="portfolio-dialog-delete-button"]').click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
      cy.get(`[data-test="${editedSecondPortfolioName}"]`).should('not.exist');

      // Delete the first portfolio
      cy.get(`[data-test="${portfolioName}"]`).should('exist').click();
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="edit-portfolio"]`).click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
      cy.window().then((win) => {
        cy.stub(win, 'confirm').returns(true);
      });
      cy.get('[data-test="portfolio-dialog-delete-button"]').click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
      cy.get(`[data-test="${portfolioName}"]`).should('not.exist');
    });
  }
);
