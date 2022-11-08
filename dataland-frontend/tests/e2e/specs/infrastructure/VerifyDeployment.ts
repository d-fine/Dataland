import { ActuatorApi } from "@clients/backend";
import { getBaseUrl } from "@e2e/utils/Cypress";

interface HealthResponse {
  status: string;
}

interface GitInfoResponse {
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
      .then((response) => JSON.parse(response.body as string) as GitInfoResponse)
      .should("have.a.property", "commit")
      .then((commit_id) =>
        assert(
          Cypress.env("COMMIT_ID") === "ignore" || commit_id === Cypress.env("COMMIT_ID"),
          "checking that commit_id of deployment matches the current one"
        )
      );
  });
});
