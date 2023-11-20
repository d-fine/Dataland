import { reader_name, reader_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getStoredCompaniesForDataType } from "@e2e/utils/GeneralApiUtils";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { type CompanyIdAndName, DataTypeEnum } from "@clients/backend";

describe("As a user, I expect the navigation around the company cockpit to work as expected", () => {
  let someCompanyIdAndName: CompanyIdAndName;
  let otherCompanyName: string;

  const companyCockpitRegex = RegExp("/companies/[0-9a-fA-F-]{36}$");

  before(() => {
    getKeycloakToken(reader_name, reader_pw)
      .then((token: string) => {
        return getStoredCompaniesForDataType(token, DataTypeEnum.EutaxonomyNonFinancials);
      })
      .then((storedCompanies) => {
        expect(storedCompanies).to.be.not.empty;
        someCompanyIdAndName = {
          companyId: storedCompanies[0].companyId,
          companyName: storedCompanies[0].companyInformation.companyName,
        };
        otherCompanyName = storedCompanies[1].companyInformation.companyName;
      });
  });

  it("From the landing page visit the company cockpit via the searchbar", () => {
    cy.visitAndCheckAppMount("/");
    searchCompanyAndChooseFirstSuggestion(someCompanyIdAndName.companyName);
    cy.url().should("match", companyCockpitRegex);
    cy.get("h1").should("exist");
  });

  it("From the company cockpit page visit the company cockpit of a different company", () => {
    visitSomeCompanyCockpit();
    searchCompanyAndChooseFirstSuggestion(otherCompanyName);
    cy.url().should("not.contain", `/companies/${someCompanyIdAndName.companyId}`);
    cy.url().should("match", companyCockpitRegex);
  });

  it("From the company cockpit page visit a view page", () => {
    cy.ensureLoggedIn(uploader_name, uploader_pw);
    visitSomeCompanyCockpit();
    cy.get("[data-test='eutaxonomy-non-financials-summary-panel']").click();
    cy.url().should(
      "contain",
      `/companies/${someCompanyIdAndName.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`,
    );
  });

  it("From the company cockpit page visit a view page", () => {
    cy.ensureLoggedIn(uploader_name, uploader_pw);
    visitSomeCompanyCockpit();
    cy.get("[data-test='eutaxonomy-financials-summary-panel'] a").click();
    cy.url().should(
      "contain",
      `/companies/${someCompanyIdAndName.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`,
    );
  });

  /**
   * Visit the company cockpit of a predefined company
   */
  function visitSomeCompanyCockpit(): void {
    cy.visit(`/companies/${someCompanyIdAndName.companyId}`);
    cy.contains("h1", someCompanyIdAndName.companyName).should("exist");
  }

  /**
   * Searches for a specified term in the companies search bar and selects the first autocomplete suggestion
   * @param searchTerm the term to search for
   */
  function searchCompanyAndChooseFirstSuggestion(searchTerm: string): void {
    cy.get("input#company_search_bar_standard").type(searchTerm);
    cy.get(".p-autocomplete-item").first().click();
  }
});
