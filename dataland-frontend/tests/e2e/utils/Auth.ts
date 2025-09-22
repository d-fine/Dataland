import { getBaseUrl, reader_name, reader_pw } from '@e2e/utils/Cypress';
import { PortfolioControllerApi } from '@clients/userservice';
import { Configuration } from '@clients/backend';
/**
 * Navigates to the /companies page and logs the user out via the dropdown menu. Verifies that the logout worked
 */
export function logout(): void {
  cy.intercept({ times: 1, url: '**/api-keys/getApiKeyMetaInfoForUser' })
    .as('apikey')
    .visitAndCheckAppMount('/api-key')
    .wait('@apikey', { timeout: Cypress.env('short_timeout_in_ms') as number });
  cy.get("[data-test='user-profile-toggle']").click();
  cy.get('.p-menu-item-link').contains('LOG OUT').click();
  cy.url().should('eq', getBaseUrl() + '/');
  cy.get("[data-test='login-dataland-button']").should('exist').should('be.visible');
}

let globalJwt = '';

/**
 * Logs in via the keycloak login form with the provided credentials. Verifies that the login worked.
 * @param username the username to use (defaults to data_reader)
 * @param password the password to use (defaults to the password of data_reader)
 * @param otpGenerator an optional function for obtaining a TOTP code if 2FA is enabled
 */
export function login(username = reader_name, password = reader_pw, otpGenerator?: () => string): void {
  cy.intercept({ url: 'https://www.youtube.com/**' }, { forceNetworkError: false }).as('youtube');
  cy.intercept({ times: 1, url: '/users/portfolios/names' }).as('getPortfolios');

  cy.visitAndCheckAppMount('/');
  cy.get("[data-test='login-dataland-button']", { timeout: Cypress.env('long_timeout_in_ms') as number }).click();

  loginWithCredentials(username, password);

  if (otpGenerator) {
    cy.get("input[id='otp']")
      .should('exist')
      .then((it) => {
        // cy.then used to ensure that the OTP is only generated right before it is entered
        return cy.wrap(it).type(otpGenerator());
      })
      .get('#kc-login')
      .should('exist')
      .click();
  }
  let doesUserHavePortfolios = false;
  getKeycloakToken(username, password).then(async (token) => {
    const allUserPortfolios = await new PortfolioControllerApi(
      new Configuration({ accessToken: token })
    ).getAllPortfolioNamesForCurrentUser();
    doesUserHavePortfolios = allUserPortfolios.data.length > 0;
  });

  cy.url({ timeout: Cypress.env('long_timeout_in_ms') as number }).should(
    'eq',
    getBaseUrl() + (doesUserHavePortfolios ? '/portfolios' : '/companies')
  );
  cy.wait('@getPortfolios', { timeout: Cypress.env('long_timeout_in_ms') as number }).then((interception) => {
    globalJwt = interception.request.headers['authorization'] as string;
  });
}
/**
 * Logs in via the keycloak login form with the provided credentials. Verifies that the login worked.
 * @param username the username to use (defaults to data_reader)
 * @param password the password to use (defaults to the password of data_reader)
 */
export function loginWithCredentials(username = reader_name, password = reader_pw): void {
  cy.get('#username').should('exist').type(username, { force: true });
  cy.get('#password').should('exist').type(password, { force: true });
  cy.get('#kc-login').should('exist').click();
}
/**
 * Performs a login if required to ensure that the user is logged in with the credentials.
 * Sessions are cached for enhanced performance
 * @param username the username to use (defaults to data_reader)
 * @param password the password to use (defaults to the password of data_reader)
 */
export function ensureLoggedIn(username?: string, password?: string): void {
  cy.session(
    [username, password],
    () => {
      console.log('Session not ok - logging in again');
      login(username, password);
    },
    {
      validate: () => {
        cy.request({
          url: '/api/token',
          headers: {
            Authorization: globalJwt,
          },
        })
          .its('status')
          .should('eq', 200);
      },
      cacheAcrossSpecs: true,
    }
  );
}

/**
 * Obtains a fresh JWT token from keycloak by using a password grant. The result is wrapped in a cypress chainable
 * @param username the username to use (defaults to data_reader)
 * @param password the password to use (defaults to the password of data_reader)
 * @param client_id the keycloak client id to use (defaults to dataland-public)
 * @returns a cypress string chainable containing the JWT
 */
export function getKeycloakToken(
  username = reader_name,
  password = reader_pw,
  client_id = 'dataland-public'
): Cypress.Chainable<string> {
  return (
    cy
      .request({
        url: '/keycloak/realms/datalandsecurity/protocol/openid-connect/token',
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body:
          'username=' +
          encodeURIComponent(username) +
          '&password=' +
          encodeURIComponent(password) +
          '&grant_type=password&client_id=' +
          encodeURIComponent(client_id) +
          '',
      })
      .should('have.a.property', 'body')
      .should('have.a.property', 'access_token')
      // eslint-disable-next-line @typescript-eslint/no-base-to-string
      .then((token): string => token.toString())
  );
}
