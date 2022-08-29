describe("As a user, I want to ensure that security relevant headers are set.", () => {
  function checkCommonHeaders(response: Cypress.Response<any>) {
    expect(response.headers).to.have.property("cross-origin-embedder-policy", "require-corp");
    expect(response.headers).to.have.property("cross-origin-resource-policy", "same-site");
    expect(response.headers).to.have.property("feature-policy", "none");
    expect(response.headers).to.have.property("referrer-policy", "no-referrer");
    expect(response.headers).to.have.property("strict-transport-security", "max-age=31536000; includeSubDomains");
    expect(response.headers).to.have.property("x-content-type-options", "nosniff");
  }

  it("test for frontend response", () => {
    cy.request("GET", Cypress.config("baseUrl") + "/").then((response) => {
      expect(response.headers).to.have.property(
        "content-security-policy",
        "frame-ancestors 'self'; form-action 'self'"
      );
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

  it("test for keycloak response", () => {
    cy.request("GET", Cypress.config("baseUrl") + "/keycloak/robots.txt").then((response) => {
      expect(response.headers).to.have.property(
        "content-security-policy",
        "frame-ancestors 'self'; form-action 'self'"
      );
      checkCommonHeaders(response);
      expect(response.headers).to.have.property("x-frame-options", "sameorigin");
    });
  });
});
