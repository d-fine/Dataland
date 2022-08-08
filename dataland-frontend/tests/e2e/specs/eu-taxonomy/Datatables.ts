describe("Datables test suite", () => {
  it("Search for all companies containing 'a' and verify that results are paginated, only first 100 are shown", () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/searchtaxonomy");
    const inputValue = "a";
    cy.get("input[name=eu_taxonomy_search_bar_top]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .type("{enter}")
      .should("have.value", inputValue);
    cy.get("h2").should("contain", "Results");
    cy.get("table.p-datatable-table").should("exist");
    cy.get(".p-paginator-current").should("contain.text", "Showing 1 to 100 of").contains("entries");
  });
});
