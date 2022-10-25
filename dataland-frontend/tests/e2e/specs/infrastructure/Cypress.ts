import { wrapPromiseToCypressPromise } from "../../utils/Cypress";

describe("As a developer, I want to ensure that cypress behaves as expected", () => {
  it("Test that 500 request interception can be disabled for individual requests", () => {
    cy.intercept("/api/force500Response", { middleware: true }, (req) => {
      console.log("ALLOW500");
      req.headers["DATALAND-ALLOW-5XX"] = "true";
    }).as("Allow 500");

    cy.then(() =>
      wrapPromiseToCypressPromise(
        fetch("/api/force500Response").then((r) => {
          assert(r.status >= 500, "Expected a 500 response");
        })
      )
    );
  });
});
