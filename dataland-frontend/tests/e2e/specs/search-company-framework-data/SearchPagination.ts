describe("As a user, I expect there to be multiple result pages if there are many results to be displayed", () => {
  beforeEach(() => {
    cy.ensureLoggedIn();
  });

  it("Do a search with 0 matches, then assure that the paginator is gone and the page text says no results", () => {
    cy.visitAndCheckAppMount("/companies");
    const inputValueThatWillResultInZeroMatches = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345678987654321";
    cy.get("input[id=search_bar_top]")
      .should("exist")
      .type(inputValueThatWillResultInZeroMatches)
      .type("{enter}")
      .should("have.value", inputValueThatWillResultInZeroMatches);
    cy.get("div.p-paginator").should("not.exist");
    cy.contains("span", "No results");
  });

  it("Search for all companies containing 'a' and verify that results are paginated, only first 100 are shown", () => {
    cy.visitAndCheckAppMount("/companies");
    const inputValue = "a";
    cy.get("input[id=search_bar_top]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .type("{enter}")
      .should("have.value", inputValue);
    cy.get("table.p-datatable-table").should("exist");
    cy.get(".p-paginator-current").should("contain.text", "Showing 1 to 100 of").contains("entries");
    cy.contains("span", "1-100 of");
  });

  it("Search for all companies, go to page 2 of the search results, then run a another query and verify that paginator and the page text are reset", () => {
    cy.visitAndCheckAppMount("/companies");
    cy.get("table.p-datatable-table").should("exist");
    cy.get('button[class="p-paginator-page p-paginator-element p-link"]').eq(0).should("contain.text", "2").click();
    cy.get("table.p-datatable-table").should("exist");
    const inputValue = "a";
    cy.get("input[id=search_bar_top]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .type("{enter}")
      .should("have.value", inputValue);
    cy.get(".p-paginator-current").should("contain.text", "Showing 1 to 100 of").contains("entries");
    cy.contains("span", "1-100 of");
  });
});
