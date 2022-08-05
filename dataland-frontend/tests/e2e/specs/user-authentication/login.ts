describe("As a user I want to be able to login and I want the login page to behave as I expect", () => {
  it("Checks that login works", () => {
    login();
    logout();
  });

  it("Checks that the back button on the login page works as expected", () => {
    cy.visit("/searchtaxonomy")
      .get("#back_button")
      .should("exist")
      .click()
      .url()
      .should("eq", Cypress.config("baseUrl") + "/");
  });
});

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
