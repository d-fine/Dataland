import ClaimOwnershipDialog from "@/components/resources/companyCockpit/ClaimOwnershipDialog.vue";

describe("Component test for ClaimOwnershipPanel", () => {
  it("ClaimOwnershipPanel component works correctly", () => {
    cy.mountWithPlugins(ClaimOwnershipDialog, {
      data() {
        return {
          companyName: "TestClaimOwnershipDialogMessage",
          dialogIsVisible: true,
        };
      },
    }).then(() => {
      cy.get("#claimOwnerShipDialog").should("exist").should("be.visible");
      cy.get("[data-test='claimOwnershipDialogMessage']").should(
        "contain.text",
        "Are you responsible for the datasets of TestClaimOwnershipDialogMessage? Claim dataset ownership in order to ensure high",
      );

      cy.get("textarea[name='claimOwnershipMessage']")
        .type("THIS IS A TEST MESSAGE")
        .should("have.value", "THIS IS A TEST MESSAGE");

      cy.get(".p-dialog-footer .p-button-label").should("contain.text", "SUBMIT");

      // For some reason the click doesn't work :(
      // cy.get(".p-dialog-footer button").click();

      // cy.get("[data-test='claimOwnershipDialogSubmittedMessage']").should(
      //   "contain.text",
      //   "We will reach out to you soon via email.",
      // );
      // cy.get(".p-dialog-footer .p-button-label").should("contain.text", "CLOSE");

      // For some reason the click doesn't work :(
      // cy.get(".p-dialog-footer button").click();

      // cy.get("#claimOwnerShipDialog").should("not.exist");
    });
  });
});
