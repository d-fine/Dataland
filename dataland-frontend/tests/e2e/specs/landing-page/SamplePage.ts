import { getBaseUrl } from "@e2e/utils/Cypress";

describe("As a user, I expect the sample page to be functional and reachable without logging in", () => {
  it("Check that the sample section works properly without authentication", () => {
    cy.visitAndCheckAppMount("/");
    cy.get("h2").should("contain.text", "Explore Dataland");

    cy.intercept("**/api/companies/teaser")
      .as("getTeaserCompanies")
      .intercept("**/api/metadata*")
      .as("getMetaDataOfFirstTeaserCompany")
      .intercept("**/api/companies/*")
      .as("getCompanyById")
      .intercept("**/api/data/**")
      .as("getDataById");
    cy.get("button[name=preview_button]")
      .should("be.visible")
      .should("contain.text", "Preview data")
      .click({ force: true })
      .wait(["@getTeaserCompanies", "@getMetaDataOfFirstTeaserCompany", "@getCompanyById", "@getDataById"], {
        timeout: Cypress.env("medium_timeout_in_ms") as number,
      });

    cy.url().should("eq", getBaseUrl() + "/preview");
    cy.get("h2").should("contain.text", "EU Taxonomy");
    cy.get('span[data-test="Revenue"]').should("exist");
    cy.get('span[data-test="OpEx"]').should("exist");
    cy.get("#framework_data_search_bar_standard").should("not.exist");
    cy.get('div[data-test="reportsBanner"]').should("not.exist");
    cy.get("[title=back_button]")
      .should("be.visible")
      .click({ force: true })
      .url()
      .should("eq", getBaseUrl() + "/");
  });
});
