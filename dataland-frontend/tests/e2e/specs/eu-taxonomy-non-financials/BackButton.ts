import { getStoredCompaniesForDataType } from "@e2e/utils/GeneralApiUtils";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { DataTypeEnum } from "@clients/backend";
import { reader_name, reader_pw } from "@e2e/utils/Cypress";

describe("As a user, I expect the back button to work properly", () => {
  it("company eu taxonomy page should be present and contain back button", function () {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/companies");
    getKeycloakToken(reader_name, reader_pw).then((token) => {
      cy.browserThen(getStoredCompaniesForDataType(token, DataTypeEnum.EutaxonomyNonFinancials)).then(
        (storedCompanies) => {
          cy.intercept("**/api/companies/**").as("getCompany");
          cy.intercept("**/api/metadata**").as("getMetaDataForCompany");
          cy.visitAndCheckAppMount(
            "/companies/" + storedCompanies[0].companyId + "/frameworks/eutaxonomy-non-financials"
          );
          cy.wait("@getCompany", { timeout: Cypress.env("medium_timeout_in_ms") as number });
          cy.wait("@getMetaDataForCompany", { timeout: Cypress.env("medium_timeout_in_ms") as number });

          cy.get("span.text-primary[title=back_button]")
            .parent(".cursor-pointer.grid.align-items-center")
            .click()
            .url()
            .should("include", "/companies");
        }
      );
    });
  });
});
