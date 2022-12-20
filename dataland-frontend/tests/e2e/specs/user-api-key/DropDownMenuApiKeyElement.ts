describe("As a user I expect api key link will be visible in the menu", () => {
  it("successfully redirects to the page api-key", () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/companies");
    cy.get("div#profile-picture-dropdown-toggle").click();
    cy.get('[data-test="profileMenu"]').should("be.visible");
    cy.get("div#profile-picture-dropdown-toggle").click();
    cy.get('[data-test="profileMenu"]').should("not.exist");
    cy.get("div#profile-picture-dropdown-toggle").click();
    cy.get('a[id="profile-api-generate-key-button"]').click({ force: true }).url().should("include", "/api-key");
  });
});
