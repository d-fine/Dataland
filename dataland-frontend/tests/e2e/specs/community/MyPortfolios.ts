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
    const companyTimestamp = Date.now();
    let portfolioTimestamp: number;
    let portfolioName: string;
    let editedPortfolioName: string;

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
      portfolioName = `E2E Test Portfolio ${portfolioTimestamp}`;
      editedPortfolioName = `${portfolioName} Edited ${portfolioTimestamp}`;

      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visitAndCheckAppMount('/portfolios');
      cy.intercept('POST', '**/community/requests/bulk').as('postBulkRequest');
      cy.intercept('GET', '**/users/portfolio/names').as('getPortfolioNames');
      cy.intercept('GET', '**/users/portfolios/**/enriched-portfolio').as('getEnrichedPortfolio');
    });

    it.only('Creates, edits and deletes a portfolio', () => {
      cy.get('button[data-test="add-portfolio"]').click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
      cy.get('.p-dialog').within(() => {
        cy.get('.p-dialog-header').contains('Add Portfolio');
        cy.get('.portfolio-dialog-content').within(() => {
          cy.get('[data-test="portfolio-name-input"]').type(portfolioName);
          cy.get('[data-test="portfolio-dialog-save-button"]').should('be.disabled');
          cy.get('[data-test="company-identifiers-input"]:visible').type(permIdOfExistingCompany);
          cy.get('[data-test="portfolio-dialog-add-companies"]').click();
          cy.get('[data-test="portfolio-dialog-save-button"]').should('not.be.disabled');
          cy.get('[data-test="portfolio-dialog-save-button"]').click({
            timeout: Cypress.env('medium_timeout_in_ms') as number,
          });
        });
      });

      cy.wait('@getEnrichedPortfolio');
      cy.get(`[data-test="${portfolioName}"]`).click({
        timeout: Cypress.env('medium_timeout_in_ms') as number,
      });
      cy.get(`[data-test="portfolio-${portfolioName}"] [data-test="edit-portfolio"]`).click();
      cy.get('[data-test="portfolio-name-input"]').clear();
      cy.get('[data-test="portfolio-name-input"]').type(editedPortfolioName);
      cy.get('[data-test="company-identifiers-input"]').type(permIdOfSecondCompany);
      cy.get('[data-test="portfolio-dialog-add-companies"]').click();
      cy.get('[data-test="portfolio-dialog-save-button"]').click();
      cy.get(`[data-test="${editedPortfolioName}"]`).click();
      cy.get(`[data-test="portfolio-${editedPortfolioName}"] [data-test="edit-portfolio"]`).click();

      cy.window().then((win) => {
        cy.stub(win, 'confirm').returns(true);
      });
      cy.get('[data-test="portfolio-dialog-delete-button"]').click();
      cy.get(`[data-test="${editedPortfolioName}"]`).should('not.exist');
    });
  }
);
