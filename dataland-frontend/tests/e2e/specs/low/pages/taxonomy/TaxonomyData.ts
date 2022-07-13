import { visitAndCheckAppMount } from "../../../../support/commands";

describe("EU Taxonomy Page", function () {
  it("page should be present", function () {
    cy.restoreLoginSession();
    cy.retrieveDataIdsList().then((dataIdList: any) => {
      visitAndCheckAppMount("/companies/" + dataIdList[2] + "/eutaxonomies");
      cy.get("#app").should("exist");
    });
    cy.get("h2").should("contain", "EU Taxonomy Data");
    const placeholder = "Search company by name or PermID";
    cy.get("input[name=eu_taxonomy_search_bar_standard]")
      .should("not.be.disabled")
      .invoke("attr", "placeholder")
      .should("contain", placeholder);
  });
});
