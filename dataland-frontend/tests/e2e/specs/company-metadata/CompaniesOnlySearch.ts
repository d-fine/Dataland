describe("As a user, I want to be able to search companies existing on Dataland", function () {
  beforeEach(() => {
    cy.ensureLoggedIn();
  });

  it("Check if the search bar is available, and if the show-all-companies button works as expected", function () {
    function verifyCompanySearchResultTable(): void {
      cy.get("table.p-datatable-table").contains("th", "COMPANY");
      cy.get("table.p-datatable-table").contains("th", "SECTOR");
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
});
