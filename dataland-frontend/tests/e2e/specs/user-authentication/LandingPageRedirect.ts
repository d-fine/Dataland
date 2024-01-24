import { getBaseUrl } from "@e2e/utils/Cypress";

describe("As a user, I expects not to be automatically redirected to the company search page when I am logged in and visit the landing page", () => {
  it("Checks that the redirect works", () => {
    cy.ensureLoggedIn();
    cy.visit("/")
      .url()
      .should("eq", getBaseUrl() + "/");
    cy.get("[data-test='backToPlatformLink']").should("exist").click();
    cy.url().should("eq", getBaseUrl() + "/companies");
  });
});
