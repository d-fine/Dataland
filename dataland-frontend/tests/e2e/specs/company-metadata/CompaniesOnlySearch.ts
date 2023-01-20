import { EuTaxonomyDataForFinancials } from "@clients/backend";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";

describe("As a user, I want to be able to search companies existing on Dataland", function () {
  beforeEach(() => {
    cy.ensureLoggedIn();
  });

  let companiesWithEuTaxonomyDataForFinancials: Array<FixtureData<EuTaxonomyDataForFinancials>>;

  before(function () {
    cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (jsonContent) {
      companiesWithEuTaxonomyDataForFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
    });
  });

  it("Check if the search bar is available, and if the 'Show all companies'-button works as expected", function () {
    const inputValue = "dummy";
    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get(".p-card-title").should("contain", "Company Search");
    cy.get("input[name=companyName]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .should("have.value", inputValue);
    cy.get("button[name=show_all_companies_button].p-button").click({ force: true });
    cy.get("table.p-datatable-table").contains("th", "COMPANY");
    cy.get("table.p-datatable-table").contains("th", "SECTOR");
  });

  function getCompanyWithAlternativeName(): FixtureData<EuTaxonomyDataForFinancials> {
    return companiesWithEuTaxonomyDataForFinancials.filter((it) => {
      return (
        it.companyInformation.companyAlternativeNames !== undefined &&
        it.companyInformation.companyAlternativeNames.length > 0
      );
    })[0];
  }

  it("Search for company by its alternative name", () => {
    const testCompany = getCompanyWithAlternativeName();
    const searchValue = testCompany.companyInformation.companyAlternativeNames![0];
    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get("input[name=companyName]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(searchValue)
      .should("have.value", searchValue);
    cy.get("button[name=getCompanies]").click({ force: true });
    cy.get("table.p-datatable-table").contains(testCompany.companyInformation.companyName);
  });

  it("Company Name Input field exists and works", () => {
    const inputValue = companiesWithEuTaxonomyDataForFinancials[0].companyInformation.companyName;
    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get("input[name=companyName]")
      .should("not.be.disabled")
      .type(inputValue, { force: true })
      .should("have.value", inputValue);
    cy.intercept("**/api/companies*").as("retrieveCompany");
    cy.get("button[name=getCompanies]").click();
    cy.wait("@retrieveCompany", { timeout: Cypress.env("long_timeout_in_ms") }).then(() => {
      cy.get("td").contains(companiesWithEuTaxonomyDataForFinancials[0].companyInformation.companyName);
    });
  });

  it("'Show all companies'-button exists and is enabled", () => {
    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get("button.p-button").contains("Show all companies").should("not.be.disabled").click();
  });
});
