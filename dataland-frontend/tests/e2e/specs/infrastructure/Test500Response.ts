import { wrapPromiseToCypressPromise, reader_name, reader_pw } from "@e2e/utils/Cypress";
import { describeIf } from "@e2e/support/TestUtility";

describe("As a developer, I want to ensure that cypress behaves as expected", () => {
  describeIf(
    "In the CI pipeline and in the local development environment, testing endpoint should be exposed",
    {
      executionEnvironments: ["developmentLocal", "ci"],
      dataEnvironments: ["fakeFixtures", "realData"],
    },
    () => {
      it("Test that 500 request interception can be disabled for individual requests", () => {
        cy.intercept("/api/testing/getDummy500Response", { middleware: true }, (req) => {
          req.headers["DATALAND-ALLOW-5XX"] = "true";
        }).as("Allow 500");

        cy.getKeycloakToken(reader_name, reader_pw).then((token) =>
          wrapPromiseToCypressPromise(
            fetch("/api/testing/getDummy500Response", { headers: { Authorization: `Bearer ${token}` } }).then((r) => {
              assert(r.status >= 500, "Expected a 500 response");
            })
          )
        );
      });
    }
  );
  describeIf(
    "In the CD pipeline, testing endpoint should NOT be exposed",
    {
      executionEnvironments: ["developmentCd", "previewCd"],
      dataEnvironments: ["fakeFixtures", "realData"],
    },
    () => {
      it("Test that the testing endpoints are not available", () => {
        cy.getKeycloakToken(reader_name, reader_pw).then((token) =>
          wrapPromiseToCypressPromise(
            fetch("/api/testing/getDummy500Response", { headers: { Authorization: `Bearer ${token}` } }).then((r) => {
              assert(r.status == 404, "Expected a 404 response");
            })
          )
        );
      });
    }
  );
});
