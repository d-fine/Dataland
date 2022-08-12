import { checkViewButtonWorks } from "../../utils/CompanySearch";

describe("As a user, I want to be able to search companies on /search", function () {
  beforeEach(() => {
    cy.ensureLoggedIn();
  });

  it("page should be present", function () {
    function verifyCompanySearchResultTable(): void {
      cy.get("table.p-datatable-table").contains("th", "COMPANY");
      cy.get("table.p-datatable-table").contains("th", "SECTOR");
      cy.get("table.p-datatable-table").contains("th", "MARKET CAP");
    }

    cy.visitAndCheckAppMount("/search");
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
    checkViewButtonWorks();
  });
});
