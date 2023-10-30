describe("As a user, I expect the cookie banner to render and to be functional", () => {
  it("Check that the cookie banner renders", () => {
    cy.visitAndCheckAppMount("/");

    cy.get("#CybotCookiebotDialog").should("exist");
    cy.get("#CybotCookiebotDialogPoweredbyImage").invoke("attr", "src").should("contain", "data:image/png;base64,");
    cy.get("#CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll").should("exist");
    cy.get("#CybotCookiebotDialogBodyLevelButtonLevelOptinAllowallSelection").should("exist");
    cy.get("#CybotCookiebotDialogBodyButtonDecline").should("exist");
    cy.get("#CybotCookiebotDialogBodyFieldsetInnerContainer").should("exist");
    cy.get("#CybotCookiebotDialogBodyContentTitle").should("exist").should("contain", "This website uses cookies");

    cy.get("#CybotCookiebotDialogBodyEdgeMoreDetailsLink")
      .should("exist")
      .click()
      .get("#CybotCookiebotDialogDetailBodyContentCookieContainerTypes")
      .should("exist")
      .get("li.CookieCard")
      .should("have.length", 5)
      .first()
      .get("label")
      .should("contain", "Necessary");

    cy.get("#CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll").click();
    cy.get("#CybotCookiebotDialog").should("not.be.visible");
  });
});
