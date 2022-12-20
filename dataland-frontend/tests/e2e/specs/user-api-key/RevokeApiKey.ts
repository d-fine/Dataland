describe("As a user I expect my api key will be revoke correctly", () => {
  it("successfully revoke api key", () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/api-key");
    cy.intercept("GET", "**/api-keys/getApiKeyMetaInfoForUser*", { fixture: "ApiKeyInfoMockWithKey.json" }).as(
      "apiKeyInfo"
    );
    cy.get("div#existingApiKeyCard").should("exist");
    cy.get("button.p-button-danger").should("contain.text", "DELETE").click();
    cy.get("div#revokeModal").should("be.visible");
    cy.get("button#confirmRevokeButton").should("contain.text", "CONFIRM").click();
  });
});
