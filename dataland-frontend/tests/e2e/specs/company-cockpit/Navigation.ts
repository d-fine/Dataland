import { reader_name, reader_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { searchBasicCompanyInformationForDataType } from "@e2e/utils/GeneralApiUtils";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { type CompanyIdAndName, DataTypeEnum } from "@clients/backend";

describe("As a user, I expect the navigation around the company cockpit to work as expected", () => {
  let someCompanyIdAndName: CompanyIdAndName;
  let otherCompanyIdAndName: CompanyIdAndName;

  const companyCockpitRegex = /\/companies\/[0-9a-fA-F-]{36}$/;

  before(() => {
    getKeycloakToken(reader_name, reader_pw)
      .then((token: string) => {
        return searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyNonFinancials);
      })
      .then((basicCompanyInfos) => {
        expect(basicCompanyInfos).to.be.not.empty;
        someCompanyIdAndName = {
          companyId: basicCompanyInfos[0].companyId,
          companyName: basicCompanyInfos[0].companyName,
        };
        otherCompanyIdAndName = {
          companyId: basicCompanyInfos[1].companyId,
          companyName: basicCompanyInfos[1].companyName,
        };
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
    searchCompanyAndInterceptRequest(otherCompanyIdAndName);
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

  /** TODO comment wrong
   * Searches for a company and check if GET request for framework data is sent
   * @param companyToSearch the company to navigate to via the search bar
   */
  function searchCompanyAndInterceptRequest(companyToSearch: CompanyIdAndName): void {
    cy.intercept("GET", `**/api/companies/${companyToSearch.companyId}/aggregated-framework-data-summary`).as(
      "apiRequest",
    );
    searchCompanyAndChooseFirstSuggestion(companyToSearch.companyName);
    cy.wait("@apiRequest");
  }
});
