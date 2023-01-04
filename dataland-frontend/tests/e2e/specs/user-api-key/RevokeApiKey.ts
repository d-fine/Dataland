describe("As a user I expect my api key will be revoke correctly", () => {
  it("successfully revoke api key", () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/api-key");
    cy.intercept("GET", "**/api-keys/getApiKeyMetaInfoForUser*", { fixture: "ApiKeyInfoMockWithKey.json" }).as(
      "apiKeyInfo"
    );
    cy.get("div#existingApiKeyCard").should("exist");
    cy.get("button").contains("DELETE").click();
    cy.get("div#revokeModal").should("be.visible");
    cy.get("button").contains("CANCEL").click();
    cy.get("div#revokeModal").should("not.exist");
    cy.get("button").contains("DELETE").click();
    cy.get("button#confirmRevokeButton").should("contain.text", "CONFIRM").click();
    cy.get("div#existingApiKeyCard").should("not.exist");
    cy.get("[data-test='noApiKeyWelcomeComponent']").find("button").should("contain.text", "CREATE NEW API KEY");
  });
});
