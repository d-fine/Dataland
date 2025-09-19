import { getBaseUrl, admin_name, admin_pw } from '@e2e/utils/Cypress';
import { createPortfolio, deleteAllPortfolios } from '@e2e/utils/PortfolioUtils.ts';

describe('As a user, I expect to find a backToPlatformLink when logged in and visiting the landing page', () => {
  beforeEach(() => {
    deleteAllPortfolios();
  });

  afterEach(() => {
    deleteAllPortfolios();
  });

  it('Checks that the redirect work when the user has no portfolio', () => {
    cy.ensureLoggedIn(admin_name, admin_pw);
    cy.visit('/')
      .url()
      .should('eq', getBaseUrl() + '/');
    cy.get("[data-test='backToPlatformLink']").should('exist').click();
    cy.url().should('eq', getBaseUrl() + '/companies');
  });

  it('Checks that the redirect work when the user has a portfolio', () => {
    createPortfolio();
    cy.ensureLoggedIn(admin_name, admin_pw);
    cy.visit('/')
      .url()
      .should('eq', getBaseUrl() + '/');
    cy.get("[data-test='backToPlatformLink']").should('exist').click();
    cy.url().should('eq', getBaseUrl() + '/portfolios');
  });
});
