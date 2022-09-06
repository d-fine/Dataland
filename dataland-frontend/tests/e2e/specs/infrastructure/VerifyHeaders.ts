import { describeIf } from "../../support/TestUtility";

describe("As a developer, I want to ensure that security relevant headers are set.", () => {
  function checkCommonHeaders(response: Cypress.Response<any>) {
    console.log(response.headers);
    expect(response.headers).to.have.property("referrer-policy", "no-referrer");
    expect(response.headers).to.have.property("strict-transport-security", "max-age=31536000; includeSubDomains");
    expect(response.headers).to.have.property("x-content-type-options", "nosniff");
  }

  function checkCommonCspHeaders(expectedHeader: string) {
    const urlsToCheck = [Cypress.config("baseUrl") + "/", Cypress.config("baseUrl") + "/keycloak/robots.txt"];
    urlsToCheck.forEach((url) => {
      it(`Check for local CSP headers in ${url}`, () => {
        cy.request("GET", url).then((response) => {
          expect(response.headers).to.have.property("content-security-policy", expectedHeader);
        });
      });
    });
  }

  describeIf(
    "Check CSP headers in the local development environment",
    {
      executionEnvironments: ["developmentLocal"],
      dataEnvironments: ["realData", "fakeFixtures"],
    },
    () => {
      checkCommonCspHeaders(
        "default-src 'self' https://www.youtube-nocookie.com; script-src 'self' 'unsafe-eval' " +
          "'unsafe-inline'; style-src 'self' 'unsafe-inline'; frame-ancestors 'self'; form-action 'self'; " +
          "font-src 'self' data:; img-src 'self' https://*.googleusercontent.com/"
      );
    }
  );

  describeIf(
    "Check CSP headers in the CI/CD environment",
    {
      executionEnvironments: ["development", "preview"],
      dataEnvironments: ["realData", "fakeFixtures"],
    },
    () => {
      checkCommonCspHeaders(
        "default-src 'self' https://www.youtube-nocookie.com; script-src 'self' 'unsafe-eval' " +
          "'sha256-/0dJfWlZ9/P1qMKyXvELqM6+ycG3hol3gmKln32el8o='; style-src 'self' 'unsafe-inline'; " +
          "frame-ancestors 'self'; form-action 'self'; font-src 'self' data:; " +
          "img-src 'self' https://*.googleusercontent.com/"
      );
    }
  );

  it("test for frontend response", () => {
    cy.request("GET", Cypress.config("baseUrl") + "/").then((response) => {
      checkCommonHeaders(response);
      expect(response.headers).to.have.property("x-frame-options", "sameorigin");
    });
  });

  it("test for backend response", () => {
    cy.request("GET", Cypress.config("baseUrl") + "/api/actuator/health").then((response) => {
      expect(response.headers).to.have.property("cache-control", "no-cache, no-store, max-age=0, must-revalidate");
      expect(response.headers).to.have.property(
        "content-security-policy",
        "frame-ancestors 'none'; default-src 'none'"
      );
      checkCommonHeaders(response);
      expect(response.headers).to.have.property("x-frame-options", "DENY");
    });
  });

  it("test for swagger ui response", () => {
    cy.request("GET", Cypress.config("baseUrl") + "/api/swagger-ui/index.html").then((response) => {
      expect(response.headers).to.have.property(
        "content-security-policy",
        "default-src 'self'; script-src 'self'; style-src 'self'; frame-ancestors 'self';" +
          " form-action 'self'; font-src 'self' data:; img-src 'self' data:"
      );
      checkCommonHeaders(response);
    });
  });

  it("test for keycloak response", () => {
    cy.request("GET", Cypress.config("baseUrl") + "/keycloak/robots.txt").then((response) => {
      checkCommonHeaders(response);
      expect(response.headers).to.have.property("x-frame-options", "sameorigin");
    });
  });
});
