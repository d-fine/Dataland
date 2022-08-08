describe("Marketing Section", () => {
  it("Checks that the marketing section works properly", () => {
    cy.visitAndCheckAppMount("/");
    cy.get("h2").should("contain.text", "Learn about our vision");
    cy.get('img[alt="Flow Diagramm"]').should("be.visible").should("have.attr", "src");
    cy.get("h3").contains("Bring together who");
    cy.get('img[alt="Data Workflow"]').should("be.visible").should("have.attr", "src");
    cy.get("h3").contains("Maximize data coverage");
  });
});
