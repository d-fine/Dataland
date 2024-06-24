import { getBaseUrl } from '@e2e/utils/Cypress';

describe('As a user, I expect to find a backToPlatformLink when logged in and visiting the landing page', () => {
  it('Checks that the redirect works', () => {
    cy.ensureLoggedIn();
    cy.visit('/')
      .url()
      .should('eq', getBaseUrl() + '/');
    cy.get("[data-test='backToPlatformLink']").should('exist').click();
    cy.url().should('eq', getBaseUrl() + '/companies');
  });
});
