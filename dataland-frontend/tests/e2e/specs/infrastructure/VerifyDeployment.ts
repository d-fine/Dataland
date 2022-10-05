import { ActuatorApi } from "@clients/backend";

describe("As a developer, I want to ensure that the deployment is okay", () => {
  it("retrieve health info and check that its up", () => {
    cy.browserThen(new ActuatorApi().health()).then((healthResponse) => {
      const data = healthResponse.data as any;
      expect(data.status).to.equal("UP");
    });
  });

  it("retrieve info endpoint and check commit", () => {
    cy.browserThen(new ActuatorApi().info()).then((healthResponse) => {
      const data = healthResponse.data as any;
      expect(data.git.commit.id.full).to.equal(Cypress.env("commit_id"));
    });
  });
});
