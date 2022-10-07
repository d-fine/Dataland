import { CompanyInformation, EuTaxonomyDataForFinancials } from "@clients/backend";

beforeEach(() => {
  cy.ensureLoggedIn();
});

describe("As a user, I want to be able to search companies existing on Dataland", function () {
  it("Check if the search bar is available, and if the show-all-companies button works as expected", () => {
    const inputValue = "dummy";
    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get(".p-card-title").should("contain", "Company Search");
    cy.get("input[name=companyName]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .should("have.value", inputValue);
    cy.get("button[name=show_all_companies_button].p-button")
      .should("not.be.disabled")
      .should("contain", "Show all companies")
      .click({ force: true });
    cy.get("table.p-datatable-table").contains("th", "COMPANY");
    cy.get("table.p-datatable-table").contains("th", "SECTOR");
  });
});

describe("As a user, I want to be able to search companies by their alternative names", function () {
  let fixtureData: Array<{
    companyInformation: CompanyInformation;
    t: EuTaxonomyDataForFinancials;
  }>;

  before(function () {
    cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (companies) {
      fixtureData = companies;
    });
  });

  function getCompanyWithAlternativeName() {
    return fixtureData.filter((it) => {
      return (
        it.companyInformation.companyAlternativeNames !== undefined &&
        it.companyInformation.companyAlternativeNames.length > 0
      );
    })[0];
  }

  it("Search for company by its alternative name", () => {
    const testCompany = getCompanyWithAlternativeName();
    const searchValue = testCompany.companyInformation.companyAlternativeNames!![0];
    cy.visitAndCheckAppMount("/companies-only-search");
    cy.get("input[name=companyName]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(searchValue)
      .should("have.value", searchValue);
    cy.get("button[name=getCompanies]").click({ force: true });
    cy.get("table.p-datatable-table").contains(testCompany.companyInformation.companyName);
  });
});
