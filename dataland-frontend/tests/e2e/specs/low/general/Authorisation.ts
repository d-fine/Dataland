import {visitAndCheckAppMount} from "../../../support/commands";

describe("Authentication Buttons", () => {
  it("Checks that normal and logout work", () => {
    visitAndCheckAppMount("/");
    cy.login();
    cy.logout();
  });

  it("Checks that registering works", () => {
    cy.register();
    cy.logout();
  });

  it("Checks that user dropdown menu logout works", () => {
    cy.login();
    cy.logoutDropdown();
  });
});
