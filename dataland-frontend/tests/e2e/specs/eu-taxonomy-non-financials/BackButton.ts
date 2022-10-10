import { retrieveCompanyIdsList } from "@e2e/utils/ApiUtils";

describe("As a user, I expect the back button to work properly", (): void => {
  it("company eu taxonomy page should be present and contain back button", function (): void {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/companies");
    retrieveCompanyIdsList().then((dataIdList: any): void => {
      cy.visitAndCheckAppMount("/companies/" + dataIdList[5] + "/frameworks/eutaxonomy-non-financials");
      cy.get("span.text-primary[title=back_button]")
        .parent(".cursor-pointer.grid.align-items-center")
        .click()
        .url()
        .should("include", "/companies");
    });
  });
});
