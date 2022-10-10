describe("As a developer, I want to ensure that the deployment is okay", (): void => {
  it("retrieve health info and check that its up", function (): void {
    cy.request("GET", "/api/actuator/health").its("body.status").should("equal", "UP");
  });

  it("retrieve info endpoint and check commit", function (): void {
    cy.request("GET", "/api/actuator/info").its("body.git.commit.id.full").should("equal", Cypress.env("commit_id"));
  });
});
