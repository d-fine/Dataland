import { describeIf } from "../../support/TestUtility";

describe("As a user, I expect there to be multiple result pages if there are many results to be displayed", () => {
  beforeEach(() => {
    cy.ensureLoggedIn();
  });

  it("Do a search with 0 matches, then assure that the paginator is gone", () => {
    cy.visitAndCheckAppMount("/searchtaxonomy");
    const inputValueThatWillResultInZeroMatches = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345678987654321";
    cy.get("input[name=eu_taxonomy_search_bar_top]")
      .should("exist")
      .type(inputValueThatWillResultInZeroMatches)
      .type("{enter}")
      .should("have.value", inputValueThatWillResultInZeroMatches);
    cy.get("div.p-paginator").should("not.exist");
  });

  it("Search for all companies containing 'a' and verify that results are paginated, only first 100 are shown", () => {
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
