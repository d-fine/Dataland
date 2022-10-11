import Chainable = Cypress.Chainable;
import { getBaseUrl, getStringCypressEnv } from "./Cypress";

export function logout(): void {
  cy.visitAndCheckAppMount("/companies")
    .get("div[id='profile-picture-dropdown-toggle']")
    .click()
    .get("a[id='profile-picture-dropdown-toggle']")
    .click()
    .url()
    .should("eq", `${getBaseUrl()}/`)
    .get("button[name='login_dataland_button']")
    .should("exist")
    .should("be.visible");
}

export function login(
  username = "data_reader",
  password: string = getStringCypressEnv("KEYCLOAK_READER_PASSWORD") as string
): void {
  cy.visitAndCheckAppMount("/")
    .get("button[name='login_dataland_button']")
    .click()
    .get("#username")
    .should("exist")
    .type(username, { force: true })
    .get("#password")
    .should("exist")
    .type(password, { force: true })

    .get("#kc-login")
    .should("exist")
    .click()

    .url()
    .should("eq", `${getBaseUrl()}/companies`);
}

export function ensureLoggedIn(username?: string, password?: string): void {
  cy.session(
    [username, password],
    (): void => {
      login(username, password);
    },
    {
      validate: (): void => {
        cy.visit("/").url().should("eq", `${getBaseUrl()}/companies`);
      },
    }
  );
}

export function getKeycloakToken(
  username = "data_reader",
  password: string = getStringCypressEnv("KEYCLOAK_READER_PASSWORD") as string,
  client_id = "dataland-public"
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
