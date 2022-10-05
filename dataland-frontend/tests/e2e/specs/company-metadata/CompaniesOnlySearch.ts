import {
  CompanyInformation,
  EuTaxonomyDataForNonFinancials,
} from "@clients/backend";

describe("As a user, I want to be able to search companies existing on Dataland", function () {
  beforeEach(() => {
    cy.ensureLoggedIn();
  });

  let companiesWithEuTaxonomyDataForNonFinancials: Array<{
    companyInformation: CompanyInformation;
    t: EuTaxonomyDataForNonFinancials;
  }>;

  before(function () {
    cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (companies) {
      companiesWithEuTaxonomyDataForNonFinancials = companies;
    });
  });

  it("Check if the search bar is available, and if the show-all-companies button works as expected", function () {
    function verifyCompanySearchResultTable(): void {
      cy.get("table.p-datatable-table").contains("th", "COMPANY");
      cy.get("table.p-datatable-table").contains("th", "SECTOR");
      cy.get("table.p-datatable-table").contains("th", "MARKET CAP");
    }

    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get(".p-card-title").should("contain", "Company Search");
    const inputValue = "d-fine";
    cy.get("input[name=companyName]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .should("have.value", inputValue);
    cy.get("button[name=show_all_companies_button].p-button")
      .should("not.be.disabled")
      .should("contain", "Show all companies")
      .click({ force: true });
    verifyCompanySearchResultTable();
  });

  it("Company Name Input field exists and works", () => {
    const inputValue = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation.companyName;
    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get("input[name=companyName]")
      .should("not.be.disabled")
      .type(inputValue, { force: true })
      .should("have.value", inputValue);
    cy.intercept("**/api/companies*").as("retrieveCompany");
    cy.get("button[name=getCompanies]").click();
    cy.wait("@retrieveCompany", { timeout: 60 * 1000 }).then(() => {
      cy.get("td").contains(companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation.companyName);
    });
  });

  it("Show all companies button exists", () => {
    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get("button.p-button").contains("Show all companies").should("not.be.disabled").click();
  });
});
