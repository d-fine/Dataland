import { wrapPromiseToCypressPromise } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { reader_name, reader_pw } from "@e2e/utils/Cypress";
describe("As a developer, I want to ensure that cypress behaves as expected", () => {
  it("Test that 500 request interception can be disabled for individual requests", () => {
    cy.intercept("/api/testing/getDummy500Response", { middleware: true }, (req) => {
      req.headers["DATALAND-ALLOW-5XX"] = "true";
    }).as("Allow 500");

    cy.then(() => getKeycloakToken(reader_name, reader_pw)).then((token) =>
      wrapPromiseToCypressPromise(
        fetch("/api/testing/getDummy500Response", { headers: { Authorization: `Bearer ${token}` } }).then((r) => {
          assert(r.status >= 500, "Expected a 500 response");
        })
      )
    );
  });
});
