import ClaimOwnershipPanel from "@/components/resources/companyCockpit/ClaimOwnershipPanel.vue";

describe("Component test for ClaimOwnershipPanel", () => {
  it("ClaimOwnershipPanel component works correctly", () => {
    cy.mountWithPlugins(ClaimOwnershipPanel, {
      data() {
        return {
          companyName: "TestClaimOwnershipPanelCompany",
        };
      },
    }).then(() => {
      cy.get("[data-test='claimOwnershipPanelHeading']").should(
        "have.text",
        " Responsible for TestClaimOwnershipPanelCompany? ",
      );
      cy.get("[data-test='claimOwnershipPanelLink']")
        .should("have.text", " Claim company dataset ownership. ")
        .click()
        .get("#claimOwnerShipDialog")
        .should("exist")
        .should("be.visible");
    });
  });
});
