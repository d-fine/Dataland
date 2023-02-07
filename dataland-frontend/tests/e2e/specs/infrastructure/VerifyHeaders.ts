import { describeIf } from "@e2e/support/TestUtility";
import { getBaseUrl } from "@e2e/utils/Cypress";

describe("As a developer, I want to ensure that security relevant headers are set.", () => {
  /**
   * Verifies the presence of common security headers in the provided response
   *
   * @param response the response to check
   */
  function checkCommonHeaders(response: Cypress.Response<unknown>): void {
    expect(response.headers).to.have.property("referrer-policy", "no-referrer");
    expect(response.headers).to.have.property("strict-transport-security", "max-age=31536000; includeSubDomains");
    expect(response.headers).to.have.property("x-content-type-options", "nosniff");
  }

  /**
   * Verifies that the content-security-policy header of the Dataland webpage matches the provided expected header
   *
   * @param expectedHeader the expected CSP header
   */
  function checkCommonCspHeaders(expectedHeader: string): void {
    const urlsToCheck = [`${getBaseUrl()}/`, `${getBaseUrl()}/keycloak/realms/datalandsecurity`];
    urlsToCheck.forEach((url): void => {
      it(`Check for local CSP headers in ${url}`, (): void => {
        cy.request("GET", url).then((response): void => {
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
          "font-src 'self' data:; img-src 'self' https://*.googleusercontent.com/ https://*.licdn.com/"
      );
    }
  );

  describeIf(
    "Check CSP headers in the CI/CD environment",
    {
      executionEnvironments: ["ci", "developmentCd", "previewCd"],
      dataEnvironments: ["realData", "fakeFixtures"],
    },
    () => {
      checkCommonCspHeaders(
        "default-src 'self' https://www.youtube-nocookie.com; script-src 'self' 'unsafe-eval' " +
          "'sha256-/0dJfWlZ9/P1qMKyXvELqM6+ycG3hol3gmKln32el8o='; style-src 'self' 'unsafe-inline'; " +
          "frame-ancestors 'self'; form-action 'self'; font-src 'self' data:; " +
          "img-src 'self' https://*.googleusercontent.com/ https://*.licdn.com/"
      );
    }
  );

  it("test for frontend response", () => {
    cy.request("GET", `${getBaseUrl()}/`).then((response): void => {
      checkCommonHeaders(response);
      expect(response.headers).to.have.property("x-frame-options", "sameorigin");
    });
  });

  it("test for backend response", () => {
    cy.request("GET", `${getBaseUrl()}/api/actuator/health`).then((response): void => {
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
    cy.request("GET", `${getBaseUrl()}/api/swagger-ui/index.html`).then((response): void => {
      expect(response.headers).to.have.property(
        "content-security-policy",
        "default-src 'self'; script-src 'self' 'sha256-4IiDsMH+GkJlxivIDNfi6qk0O5HPtzyvNwVT3Wt8TIw=';" +
          " style-src 'self'; frame-ancestors 'self'; form-action 'self'; font-src 'self' data:;" +
          " img-src 'self' data:"
      );
      checkCommonHeaders(response);
    });
  });

  it("test for keycloak response", () => {
    cy.request("GET", `${getBaseUrl()}/keycloak/realms/datalandsecurity`).then((response): void => {
      checkCommonHeaders(response);
      assert.equal(response.headers["x-frame-options"].toString().toLowerCase(), "sameorigin");
    });
  });
});
