describe("As a developer, I want to ensure that the deployment is okay", () => {
  it("retrieve health info and check that its up", function () {
    cy.request("GET", "/api/actuator/health").its("body.status").should("equal", "UP");
  });

  it("retrieve info endpoint and check commit", function () {
    cy.request("GET", "/api/actuator/info").its("body.git.commit.id.full").should("equal", Cypress.env("commit_id"));
  });
});
