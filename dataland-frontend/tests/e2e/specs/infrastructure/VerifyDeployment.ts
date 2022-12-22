import { ActuatorApi } from "@clients/backend";
import { getBaseUrl } from "@e2e/utils/Cypress";

interface HealthResponse {
  status: string;
}

describe("As a developer, I want to ensure that the deployment is okay", () => {
  it("retrieve health info and check that its up", () => {
    cy.browserThen(new ActuatorApi().health()).then((healthResponse) => {
      const data = healthResponse.data as HealthResponse;
      expect(data.status).to.equal("UP");
    });
  });

  it("retrieve info endpoint and check commit", () => {
    cy.request(`${getBaseUrl()}/gitinfo`)
      .should("have.a.property", "body")
      .should("have.a.property", "commit")
      .should("eq", Cypress.env("commit_id") as string);
  });
});
