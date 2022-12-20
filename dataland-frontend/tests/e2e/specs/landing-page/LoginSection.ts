describe("As a user, I expect to be able to reach the login and signup page from the landing page", () => {
  it("Check if App is present", () => {
    cy.visitAndCheckAppMount("/");
    cy.get("h1").should("contain.text", "THE ALTERNATIVE TO DATA MONOPOLIES");
    cy.get('img[alt="Dataland logo"]').should("be.visible").should("have.attr", "src").should("include", "vision");
    cy.get("button[name=join_dataland_button]").should("be.visible").should("contain.text", "Create a preview account");
    cy.get('i[alt="chevron_right"]').should("be.visible");
  });
  it("Company logos are present", () => {
    cy.visitAndCheckAppMount("/");
    cy.get('img[alt="pwc"]').should("be.visible").should("have.attr", "src");
    cy.get('img[alt="d-fine GmbH"]').should("be.visible").should("have.attr", "src");
  });
});
