import { PortfolioControllerApi } from '@clients/userservice';
import { Configuration } from '@clients/backend';
import {
  getBaseUrl,
  reader_name,
  getReaderPw,
  uploader_name,
  getUploaderPw,
  judge_name,
  getJudgePw,
  reviewer_name,
  getReviewerPw,
  admin_name,
  getAdminPw,
} from '@e2e/utils/Cypress';

const longTimeoutInMs = Number(Cypress.expose('long_timeout_in_ms') ?? 100000);
const shortTimeoutInMs = Number(Cypress.expose('short_timeout_in_ms') ?? 10000);

/**
 * Navigates to the /companies page and logs the user out via the dropdown menu. Verifies that the logout worked
 */
export function logout(): void {
  cy.intercept({ times: 1, url: '**/api-keys/getApiKeyMetaInfoForUser' })
    .as('apikey')
    .visitAndCheckAppMount('/api-key')
    .wait('@apikey', { timeout: shortTimeoutInMs });
  cy.get("[data-test='user-profile-toggle']").click();
  cy.get('.p-menu-item-link').contains('LOG OUT').click();
  cy.url().should('eq', getBaseUrl() + '/');
  cy.get("[data-test='login-dataland-button']").should('exist').should('be.visible');
}

let globalJwt = '';

/**
 * Logs in via the keycloak login form with the provided credentials. Verifies that the login worked.
 * @param username the username to use
 * @param password the password to use
 * @param otpGenerator an optional function for obtaining a TOTP code if 2FA is enabled
 */
export function login(username: string, password: string, otpGenerator?: () => string | Promise<string>): void {
  cy.intercept({ url: 'https://www.youtube.com/**' }, { forceNetworkError: false }).as('youtube');
  cy.intercept({ times: 1, url: '/users/portfolios/names' }).as('getPortfolios');

  cy.visitAndCheckAppMount('/');
  cy.get("[data-test='login-dataland-button']", { timeout: longTimeoutInMs }).click();

  loginWithCredentials(username, password);

  if (otpGenerator) {
    cy.get("input[id='otp']")
      .should('exist')
      .then((it) => {
        // cy.then used to ensure that the OTP is only generated right before it is entered
        return cy.wrap(otpGenerator()).then((otp) => {
          return cy.wrap(it).type(otp as string);
        });
      })
      .get('#kc-login')
      .should('exist')
      .click();
  }
  let urlToRedirectTo = getBaseUrl() + '/companies';
  if (!otpGenerator) {
    let doesUserHavePortfolios = false;
    getKeycloakToken(username, password).then(async (token) => {
      const allUserPortfolios = await new PortfolioControllerApi(
        new Configuration({ accessToken: token })
      ).getAllPortfolioNamesForCurrentUser();
      doesUserHavePortfolios = allUserPortfolios.data.length > 0;
      if (doesUserHavePortfolios) {
        urlToRedirectTo = getBaseUrl() + '/portfolios';
      }
      cy.url({ timeout: longTimeoutInMs }).should('eq', urlToRedirectTo);
      cy.wait('@getPortfolios', { timeout: longTimeoutInMs }).then((interception) => {
        globalJwt = interception.request.headers['authorization'] as string;
      });
    });
  }
}
/**
 * Logs in via the keycloak login form with the provided credentials. Verifies that the login worked.
 * @param username the username to use
 * @param password the password to use
 */
export function loginWithCredentials(username: string, password: string): void {
  cy.get('#username').should('exist').type(username, { force: true });
  cy.get('#password').should('exist').type(password, { force: true });
  cy.get('#kc-login').should('exist').click();
}

/**
 * Logs in with credentials after fetching the reader password via the keycloak login form.
 */
export function loginWithCredentialsOfReader(): void {
  getReaderPw().then((pw) => {
    loginWithCredentials(reader_name, pw);
  });
}

/**
 * Performs a login if required to ensure that the user is logged in with the credentials.
 * Sessions are cached for enhanced performance
 * @param username the username to use
 * @param password the password to use
 */
export function ensureLoggedIn(username: string, password: string): void {
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
 * @param username the username to use
 * @param password the password to use
 * @param client_id the keycloak client id to use (defaults to dataland-public)
 * @returns a cypress string chainable containing the JWT
 */
export function getKeycloakToken(
  username: string,
  password: string,
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

/**
 * Logs in as the reader user via the keycloak login form.
 * @param otpGenerator an optional function for obtaining a TOTP code if 2FA is enabled
 */
export function loginAsReader(otpGenerator?: () => string | Promise<string>): Cypress.Chainable<void> {
  return getReaderPw().then((pw) => login(reader_name, pw, otpGenerator));
}

/**
 * Ensures the reader user is logged in, using a cached session if available.
 */
export function ensureLoggedInAsReader(): Cypress.Chainable<string> {
  return getReaderPw().then((pw) => {
    ensureLoggedIn(reader_name, pw);
  });
}

/**
 * Obtains a fresh JWT token for the reader user from keycloak via a password grant.
 * @param client_id the keycloak client id to use (defaults to dataland-public)
 * @returns a cypress string chainable containing the JWT
 */
export function getReaderToken(client_id = 'dataland-public'): Cypress.Chainable<string> {
  return getReaderPw().then((pw) => {
    return getKeycloakToken(reader_name, pw, client_id);
  });
}

/**
 * Logs in as the reviewer user via the keycloak login form.
 * @param otpGenerator an optional function for obtaining a TOTP code if 2FA is enabled
 */
export function loginAsReviewer(otpGenerator?: () => string | Promise<string>): Cypress.Chainable<string> {
  return getReviewerPw().then((pw) => {
    login(reviewer_name, pw, otpGenerator);
  });
}

/**
 * Ensures the reviewer user is logged in, using a cached session if available.
 */
export function ensureLoggedInAsReviewer(): Cypress.Chainable<string> {
  return getReviewerPw().then((pw) => {
    ensureLoggedIn(reviewer_name, pw);
  });
}

/**
 * Obtains a fresh JWT token for the reviewer user from keycloak via a password grant.
 * @param client_id the keycloak client id to use (defaults to dataland-public)
 * @returns a cypress string chainable containing the JWT
 */
export function getReviewerToken(client_id = 'dataland-public'): Cypress.Chainable<string> {
  return getReviewerPw().then((pw) => {
    return getKeycloakToken(reviewer_name, pw, client_id);
  });
}

/**
 * Logs in as the uploader user via the keycloak login form.
 * @param otpGenerator an optional function for obtaining a TOTP code if 2FA is enabled
 */
export function loginAsUploader(otpGenerator?: () => string | Promise<string>): Cypress.Chainable<string> {
  return getUploaderPw().then((pw) => {
    login(uploader_name, pw, otpGenerator);
  });
}

/**
 * Ensures the uploader user is logged in, using a cached session if available.
 */
export function ensureLoggedInAsUploader(): Cypress.Chainable<string> {
  return getUploaderPw().then((pw) => {
    ensureLoggedIn(uploader_name, pw);
  });
}

/**
 * Obtains a fresh JWT token for the uploader user from keycloak via a password grant.
 * @param client_id the keycloak client id to use (defaults to dataland-public)
 * @returns a cypress string chainable containing the JWT
 */
export function getUploaderToken(client_id = 'dataland-public'): Cypress.Chainable<string> {
  return getUploaderPw().then((pw) => {
    return getKeycloakToken(uploader_name, pw, client_id);
  });
}

/**
 * Logs in as the judge user via the keycloak login form.
 * @param otpGenerator an optional function for obtaining a TOTP code if 2FA is enabled
 */
export function loginAsJudge(otpGenerator?: () => string | Promise<string>): Cypress.Chainable<string> {
  return getJudgePw().then((pw) => {
    login(judge_name, pw, otpGenerator);
  });
}

/**
 * Ensures the judge user is logged in, using a cached session if available.
 */
export function ensureLoggedInAsJudge(): Cypress.Chainable<string> {
  return getJudgePw().then((pw) => {
    ensureLoggedIn(judge_name, pw);
  });
}

/**
 * Obtains a fresh JWT token for the judge user from keycloak via a password grant.
 * @param client_id the keycloak client id to use (defaults to dataland-public)
 * @returns a cypress string chainable containing the JWT
 */
export function getJudgeToken(client_id = 'dataland-public'): Cypress.Chainable<string> {
  return getJudgePw().then((pw) => {
    return getKeycloakToken(judge_name, pw, client_id);
  });
}

/**
 * Logs in as the admin user via the keycloak login form.
 */
export function loginAsAdmin(): Cypress.Chainable<string> {
  return getAdminPw().then((pw) => {
    login(admin_name, pw);
  });
}

/**
 * Ensures the admin user is logged in, using a cached session if available.
 */
export function ensureLoggedInAsAdmin(): Cypress.Chainable<string> {
  return getAdminPw().then((pw) => {
    ensureLoggedIn(admin_name, pw);
  });
}

/**
 * Obtains a fresh JWT token for the admin user from keycloak via a password grant.
 * @param client_id the keycloak client id to use (defaults to dataland-public)
 * @returns a cypress string chainable containing the JWT
 */
export function getAdminToken(client_id = 'dataland-public'): Cypress.Chainable<string> {
  return getAdminPw().then((pw) => {
    return getKeycloakToken(admin_name, pw, client_id);
  });
}
