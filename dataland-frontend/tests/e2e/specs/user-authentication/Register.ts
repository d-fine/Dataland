import { login, logout } from "@e2e/utils/Auth";

describe("As a user I want to be able to register for an account and be able to log in and out of that account", () => {
  const email = `test_user${Date.now()}@dataland.com`;
  const passwordBytes = crypto.getRandomValues(new Uint32Array(32));
  const randomHexPassword = [...passwordBytes].map((x) => x.toString(16).padStart(2, "0")).join("");

  it("Checks that registering works", () => {
    cy.visitAndCheckAppMount("/")
      .get("button[name='join_dataland_button']")
      .click()
      .get("#email")
      .should("exist")
      .type(email, { force: true })

      .get("#password")
      .should("exist")
      .type(randomHexPassword, { force: true })
      .get("#password-confirm")
      .should("exist")
      .type(randomHexPassword, { force: true })

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
      .should("eq", Cypress.config("baseUrl") + "/companies");
    logout();
  });

  it("Checks that one can login to the newly registered account", () => {
    login(email, randomHexPassword);
    logout();
  });
});
