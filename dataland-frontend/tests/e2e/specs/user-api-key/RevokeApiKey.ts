describe("As a user I expect my api key will be revoke correctly", () => {
  it("successfully generate api key", () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/companies");
    cy.get('img[alt="Open drop down menu icon"]').click();
    cy.get('a[id="profile-api-generate-key-button"]').click({ force: true }).url().should("include", "/api-key");
    cy.get("div#existingApiKeyCard").should("exist");
    cy.get("button.p-button-danger").should("contain.text", "DELETE").click();
    cy.get("div#revokeModal").should("be.visible");
    cy.get("button#confirmRevokeButton").should("contain.text", "CONFIRM").click();
    cy.reload();
    cy.get("div#existingApiKeyCard").should("not.exist");
  });
});
