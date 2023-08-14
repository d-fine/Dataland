import Chainable = Cypress.Chainable;
import {getBaseUrl, reader_name, reader_pw} from "@e2e/utils/Cypress";

/**
 * Navigates to the /companies page and logs the user out via the dropdown menu. Verifies that the logout worked
 */
export function logout(): void {
  cy.visitAndCheckAppMount("/companies")
    .get("div[id='profile-picture-dropdown-toggle']")
    .click()
    .wait(1000)
    .get("a[id='profile-picture-dropdown-logout-anchor']")
    .click()
    .wait(1000)
    .url()
    .should("eq", getBaseUrl() + "/")
    .get("button[name='login_dataland_button']")
    .should("exist")
    .should("be.visible");
}

let globalJwt: string = ""

/**
 * Logs in via the keycloak login form with the provided credentials. Verifies that the login worked.
 * @param username the username to use (defaults to data_reader)
 * @param password the password to use (defaults to the password of data_reader)
 * @param otpGenerator an optional function for obtaining a TOTP code if 2FA is enabled
 */
export function login(username = reader_name, password = reader_pw, otpGenerator?: () => string): void {
  cy.intercept("https://www.youtube-nocookie.com/**", {forceNetworkError: false}).as("youtube");
  cy.intercept({times: 1, url: "/api/companies*"}).as("asdf")
  cy.visitAndCheckAppMount("/")
    .wait("@youtube", {timeout: Cypress.env("medium_timeout_in_ms") as number})
    .get("button[name='login_dataland_button']")
    .click()
    .get("#username")
    .should("exist")
    .type(username, {force: true})
    .get("#password")
    .should("exist")
    .type(password, {force: true})

    .get("#kc-login")
    .should("exist")
    .click();

  if (otpGenerator) {
    cy.get("input[id='otp']")
      .should("exist")
      .then((it) => {
        // cy.then used to ensure that the OTP is only generated right before it is entered
        return cy.wrap(it).type(otpGenerator());
      })
      .get("#kc-login")
      .should("exist")
      .click();
  }
  cy.url().should("eq", getBaseUrl() + "/companies");
  cy.wait("@asdf").then((interception) => {
    globalJwt = interception.request.headers["authorization"] as string
  })
}

function createUUID() {
  var s = [];
  var hexDigits = '0123456789abcdef';
  for (var i = 0; i < 36; i++) {
    s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
  }
  s[14] = '4';
  s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);
  s[8] = s[13] = s[18] = s[23] = '-';
  var uuid = s.join('');
  return uuid;
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
      console.log("Session not ok - logging in again");
      login(username, password);
    },
    {
      validate: () => {
        // const kcRoot = 'https://local-dev.dataland.com';
        // const kcRealm = 'datalandsecurity';
        // const kcClient = 'dataland-public';
        // const kcRedirectUri = 'https://local-dev.dataland.com/';
        // const loginPageRequest = {
        //   url: `${kcRoot}/auth/realms/${kcRealm}/protocol/openid-connect/auth`,
        //   qs: {
        //     client_id: kcClient,
        //     redirect_uri: kcRedirectUri,
        //     nonce: createUUID(),
        //     response_mode: 'fragment',
        //     response_type: 'code',
        //     scope: 'openid',
        //     code_challenge: "L4ex8hTvHo-vYBbPDwvKCr3hmGsPj1V3iKHLTJEfibM",
        //     code_challenge_method: "S256"
        //   }
        // };
        // // Open the KC login page, fill in the form with username and password and submit.
        // cy.visit(loginPageRequest)
        // cy.url().should("contain", "/companies")

        cy.request({
          // url: '/api/companies',
          url: '/api/token',
          headers: {
            Authorization: globalJwt,
          }
        }).its('status').should('eq', 200)

        // cy.visit("/companies")
        // cy.contains("AVAILABLE DATASETS").should("exist")
        // cy.wait(10000)

        // console.log("step 1")
        // cy.visit("/keycloak/realms/datalandsecurity/protocol/openid-connect/3p-cookies/step1.html")
        //   .url()
        //   .should(
        //     "eq",
        //     getBaseUrl() + "/keycloak/realms/datalandsecurity/protocol/openid-connect/3p-cookies/step2.html",
        //   ).then(() => {
        //   console.log("step 2")
        // })
        // cy.window()
        //   .then((window): boolean => {
        //     if ("hasAccess" in window) {
        //       console.log("maybe true")
        //       return window.hasAccess as boolean;
        //     } else {
        //       console.log("false")
        //       return false;
        //     }
        //   })
        //   .should("be.true");
      },
      cacheAcrossSpecs: true,
    },
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
  client_id = "dataland-public",
): Chainable<string> {
  return cy
    .request({
      url: "/keycloak/realms/datalandsecurity/protocol/openid-connect/token",
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body:
        "username=" +
        encodeURIComponent(username) +
        "&password=" +
        encodeURIComponent(password) +
        "&grant_type=password&client_id=" +
        encodeURIComponent(client_id) +
        "",
    })
    .should("have.a.property", "body")
    .should("have.a.property", "access_token")
    .then((token): string => token.toString());
}
