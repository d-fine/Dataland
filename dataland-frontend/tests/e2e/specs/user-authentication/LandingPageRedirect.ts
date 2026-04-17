import { getBaseUrl } from '@e2e/utils/Cypress';
import { createPortfolio, deleteAllPortfolios } from '@e2e/utils/PortfolioUtils.ts';

describe('As a user, I expect to find a backToPlatformLink when logged in and visiting the landing page', () => {
  beforeEach(() => {
    deleteAllPortfolios();
  });

  afterEach(() => {
    deleteAllPortfolios();
  });

  it('Checks that the redirect work when the user has no portfolio', () => {
    cy.ensureLoggedInAsAdmin();
    cy.visit('/')
      .url()
      .should('eq', getBaseUrl() + '/');
    cy.get("[data-test='backToPlatformLink']:visible").should('exist').click();
    cy.url().should('eq', getBaseUrl() + '/companies');
  });

  it('Checks that the redirect work when the user has a portfolio', () => {
    createPortfolio();
    cy.ensureLoggedInAsAdmin();
    cy.visit('/')
      .url()
      .should('eq', getBaseUrl() + '/');
    cy.get("[data-test='backToPlatformLink']:visible").should('exist').click();
    cy.url().should('eq', getBaseUrl() + '/portfolios');
  });
});
