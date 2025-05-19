import { getStringCypressEnv } from '@e2e/utils/Cypress';

describe('As a user I expect the admin console only to be reachable using admin-proxy and not from remote', (): void => {
  /**
   * Verifies that an error page is shown when navigating to the provided url
   * @param url the url to navigate to expecting an error
   */
  function checkThatUrlResolvesToErrorPage(url: string): void {
    cy.visitAndCheckExternalAdminPage({
      url: url,
      elementSelector: 'h2',
      containsText: 'Sorry an error occurred!',
      urlShouldInclude: 'nocontent',
    });
  }

  it(`Test Admin Console not reachable from remote`, () => {
    checkThatUrlResolvesToErrorPage('/keycloak/admin');
  });

  it(`Master Realm not reachable from remote`, () => {
    checkThatUrlResolvesToErrorPage('/keycloak/realms/master');
  });

  it(`Test Admin Console is reachable via dataland-admin`, () => {
    cy.visitAndCheckExternalAdminPage({
      url: 'http://dataland-admin:6789/keycloak/admin',
      interceptPattern: '**/keycloak/**',
      elementSelector: 'h1',
      containsText: 'Sign in to your account',
      urlShouldInclude: 'realms/master',
    });
    cy.get('#username').should('exist').type(getStringCypressEnv('KEYCLOAK_ADMIN'), { force: true });
    cy.get('#password').should('exist').type(getStringCypressEnv('KEYCLOAK_ADMIN_PASSWORD'), { force: true });
    cy.get('#kc-login').should('exist').click();
    cy.get('h1').should('exist').should('contain', 'master realm');
  });
});
