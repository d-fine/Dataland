import { describeIf } from "../../support/TestUtility";

describeIf(
  "As a user, I expect there to be multiple result pages if there are many results to be displayed",
  {
    dataEnvironments: ["fakeFixtures"],
    executionEnvironments: ["development"],
  },
  () => {
    beforeEach(() => {
      cy.ensureLoggedIn();
    });
    it("Assure existence of paginator for Dax search", () => {
      cy.visitAndCheckAppMount("/searchtaxonomy");
      cy.get("h1").should("contain", "Search EU Taxonomy data");
      cy.get("div.p-paginator").should("exist");
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
  }
);
