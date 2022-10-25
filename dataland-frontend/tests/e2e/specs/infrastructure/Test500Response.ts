import { wrapPromiseToCypressPromise } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";

describe("As a developer, I want to ensure that cypress behaves as expected", () => {
  it("Test that 500 request interception can be disabled for individual requests", () => {
    cy.intercept("/api/testing/getDummy500Response", { middleware: true }, (req) => {
      req.headers["DATALAND-ALLOW-5XX"] = "true";
    }).as("Allow 500");

    cy.then(() => getKeycloakToken("data_reader", Cypress.env("KEYCLOAK_READER_PASSWORD"))).then((token) =>
      wrapPromiseToCypressPromise(
        fetch("/api/testing/getDummy500Response", { headers: { Authorization: `Bearer ${token}` } }).then((r) => {
          assert(r.status >= 500, "Expected a 500 response");
        })
      )
    );
  });
});
