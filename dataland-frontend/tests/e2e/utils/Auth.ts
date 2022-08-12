import Chainable = Cypress.Chainable;

export function logout(): void {
  cy.visitAndCheckAppMount("/")
    .get("button[name='logout_dataland_button']")
    .click()
    .get("button[name='login_dataland_button']")
    .should("exist")
    .should("be.visible");
}

export function login(
  username: string = "data_reader",
  password: string = Cypress.env("KEYCLOAK_READER_PASSWORD")
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
    .should("eq", Cypress.config("baseUrl") + "/searchtaxonomy");
}

export function ensureLoggedIn(username?: string, password?: string): void {
  cy.session(
    [username, password],
    () => {
      login(username, password);
    },
    {
      validate: () => {
        cy.visitAndCheckAppMount("/").get("button[name='logout_dataland_button']").should("exist");
      },
    }
  );
}

export function getKeycloakToken(
  username: string = "data_reader",
  password: string = Cypress.env("KEYCLOAK_READER_PASSWORD"),
  client_id: string = "dataland-public"
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
    .then((token) => token.toString());
}
