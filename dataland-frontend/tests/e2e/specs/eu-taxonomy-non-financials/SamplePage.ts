import { getBaseUrl } from "@e2e/utils/Cypress";
import {DataTypeEnum} from "@clients/backend";

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
      .as("getDataById")
      .get("button[name=eu_taxonomy_sample_button]")
      .should("be.visible")
      .should("contain.text", "EU Taxonomy")
      .click({ force: true })
      .wait(["@getTeaserCompanies", "@getMetaDataOfFirstTeaserCompany", "@getCompanyById", "@getDataById"], {
        timeout: Cypress.env("short_timeout_in_ms") as number,
      })
      .then(() => {
        cy.url()
          .should("include", `/${DataTypeEnum.EutaxonomyNonFinancials}`)
          .get("h2")
          .should("contain.text", "EU Taxonomy Data")
          .get(".p-button.p-button-rounded")
          .should("contain.text", "COMPANY DATA SAMPLE")
          .get("body")
          .should("contain.text", "Try Dataland with other")
          .get("[title=back_button")
          .should("be.visible")
          .click({ force: true })
          .url()
          .should("eq", getBaseUrl() + "/");
      });
  });
});
