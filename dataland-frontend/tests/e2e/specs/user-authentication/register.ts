import { login, logout } from "./login";

describe("As a user I want to be able to create an account", () => {
  const email = `test_user${Date.now()}@dataland.com`;
  const password = "test";

  it("Checks that registering works", () => {
    cy.visitAndCheckAppMount("/")
      .get("button[name='join_dataland_button']")
      .click()
      .get("#email")
      .should("exist")
      .type(email, { force: true })

      .get("#password")
      .should("exist")
      .type(password, { force: true })
      .get("#password-confirm")
      .should("exist")
      .type(password, { force: true })

      .get("input[type='submit']")
      .should("exist")
      .click()

      .get("#accept_terms")
      .should("exist")
      .click()
      .get("#accept_privacy")
      .should("exist")
      .click()
      .get("button[name='accept_button']")
      .should("exist")
      .click()

      .url()
      .should("eq", Cypress.config("baseUrl") + "/searchtaxonomy");
    logout();
  });

  it("Checks that one can login to the newly registered account", () => {
    login(email, password);
    logout();
  });
});
