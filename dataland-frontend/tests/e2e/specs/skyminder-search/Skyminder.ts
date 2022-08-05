describe("As a developer I want to ensure that the SkyMinder integration works fine", () => {
  beforeEach(() => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/search");
  });

  it("Should display an error when I make a Skyminder search with no input", () => {
    cy.get('button[name="getSkyminderData"]').click();
    cy.get("body").should("contain", "Sorry");
  });

  it("Should display results when I make a valid Skyminder search", () => {
    cy.get("input[name=code]").type("DEU", { force: true });
    cy.get("input[name=name]").type("BMW", { force: true });
    cy.get('button[name="getSkyminderData"]').click();
    cy.get("table", { timeout: 30 * 1000 }).should("exist");
  });
});
