import { getCompanyAndDataIds } from "@e2e/utils/ApiUtils";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { DataTypeEnum } from "@clients/backend";
import { getStringCypressEnv } from "@e2e/utils/Cypress";

describe("As a user, I expect the back button to work properly", () => {
  it("company eu taxonomy page should be present and contain back button", function () {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/companies");
    getKeycloakToken("data_reader", getStringCypressEnv("KEYCLOAK_READER_PASSWORD")).then((token) => {
      cy.browserThen(getCompanyAndDataIds(token, DataTypeEnum.EutaxonomyNonFinancials)).then((dataSetNonFinancial) => {
        cy.visitAndCheckAppMount(
          "/companies/" + dataSetNonFinancial[0].companyId + "/frameworks/eutaxonomy-non-financials"
        );
        cy.get("span.text-primary[title=back_button]")
          .parent(".cursor-pointer.grid.align-items-center")
          .click()
          .url()
          .should("include", "/companies");
      });
    });
  });
});
