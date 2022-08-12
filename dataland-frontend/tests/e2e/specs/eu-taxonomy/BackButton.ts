import { retrieveCompanyIdsList } from "../../utils/ApiUtils";

describe("As a user, I expect the back button to work properly", () => {
  it("company eu taxonomy page should be present and contain back button", function () {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/companies");
    cy.get("h1").should("contain", "Search EU Taxonomy data");
    cy.retrieveDataIdsList().then((dataIdList: any) => {
      cy.visitAndCheckAppMount("/companies/" + dataIdList[5] + "/frameworks/eutaxonomy");
      cy.get("span.text-primary[title=back_button]")
        .parent(".cursor-pointer.grid.align-items-center")
        .click()
        .url()
        .should("include", "/companies");
    });
  });
});
