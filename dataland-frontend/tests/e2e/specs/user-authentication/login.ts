import Chainable = Cypress.Chainable;
import { login, logout } from "./login.functions";

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
