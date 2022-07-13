import { visitAndCheckAppMount } from "../../../support/commands";

describe("Tooltips test suite", () => {
  it("tooltips are present and contain text as expected", function () {
    const NFRDText = "Non financial disclosure directive";
    const AssuranceText = "Level of Assurance specifies the confidence level";
    cy.intercept("**/api/companies/*").as("retrieveCompany");
    cy.restoreLoginSession();
    cy.retrieveCompanyIdsList().then((companyIdList: any) => {
      visitAndCheckAppMount("/companies/" + companyIdList[0] + "/eutaxonomies");
      cy.wait("@retrieveCompany", { timeout: 2 * 1000 }).then(() => {
        cy.get("#app", { timeout: 2 * 1000 }).should("exist");
        cy.get(".p-card-content .text-left strong").contains("NFRD required");
        cy.get('.material-icons[title="NFRD required"]').trigger("mouseenter", "center");
        cy.get(".p-tooltip").should("be.visible").contains(NFRDText);
        cy.get('.material-icons[title="NFRD required"]').trigger("mouseleave");
        cy.get(".p-tooltip").should("not.exist");
        cy.get(".p-card-content .text-left strong").contains("Level of Assurance");
        cy.get('.material-icons[title="Level of Assurance"]').trigger("mouseenter", "center");
        cy.get(".p-tooltip").should("be.visible").contains(AssuranceText);
      });
    });
  });
});
