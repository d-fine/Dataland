describe("User interactive tests for Data Search", () => {
  beforeEach(() => {
    cy.restoreLoginSession();
    cy.visit("/search");
  });

  it("Skyminder Data Search with no input", () => {
    cy.get('button[name="getSkyminderData"]').click();
    cy.get("body").should("contain", "Sorry");
  });

  it("Skyminder Data Search when everything is fine", () => {
    cy.get("input[name=code]").type("DEU", { force: true });
    cy.get("input[name=name]").type("BMW", { force: true });
    cy.get('button[name="getSkyminderData"]').click();
    cy.get("table", { timeout: 30 * 1000 }).should("exist");
  });
});
