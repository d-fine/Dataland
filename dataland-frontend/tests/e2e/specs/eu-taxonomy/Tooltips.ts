import { retrieveCompanyIdsList } from "../../utils/ApiUtils";

describe("As a user, I expect informative tooltips to be shown on the EuTaxonomy result page", () => {
  it("tooltips are present and contain text as expected", function () {
    const NFRDText = "Non financial disclosure directive";
    const AssuranceText = "Level of Assurance specifies the confidence level";
    cy.intercept("**/api/companies/*").as("retrieveCompany");
    cy.ensureLoggedIn();
    cy.retrieveCompanyIdsList().then((companyIdList: any) => {
      cy.visitAndCheckAppMount("/companies/" + companyIdList[0] + "/frameworks/eutaxonomy");
      cy.wait("@retrieveCompany", { timeout: 2 * 1000 }).then(() => {
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
