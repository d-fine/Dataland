import { createCompanyAndGetId } from "../../utils/CompanyUpload";

describe("As a user, I want to ensure that security relevant headers are set.", () => {
  function checkCommonHeaders(response: Cypress.Response<any>) {
    expect(response.headers).to.have.property("feature-policy", "none");
    expect(response.headers).to.have.property("referrer-policy", "no-referrer");
    expect(response.headers).to.have.property("strict-transport-security", "max-age=31536000 ; includeSubDomains");
    expect(response.headers).to.have.property("x-content-type-options", "nosniff");
  }

  it("test for frontend response", () => {
    cy.request("GET", Cypress.config("baseUrl") + "/").then((response) => {
      expect(response.headers).to.have.property("cache-control", "no-store");
      expect(response.headers).to.have.property("content-security-policy", "frame-ancestors 'self'");
      checkCommonHeaders(response);
      expect(response.headers).to.have.property("x-frame-options", "sameorigin");
    });
  });

  it("test for backend response", () => {
    cy.ensureLoggedIn("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"));
    createCompanyAndGetId("Dummy Company");
    cy.getKeycloakToken("data_reader", Cypress.env("KEYCLOAK_READER_PASSWORD")).then((token) => {
      cy.request({
        url: "/api/companies",
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + token,
        },
      }).then((response) => {
        console.log("headers:");
        console.log(response.headers);
        expect(response.headers).to.have.property("cache-control", "no-cache, no-store, max-age=0, must-revalidate");
        expect(response.headers).to.have.property(
          "content-security-policy",
          "frame-ancestors 'none'; default-src 'none'"
        );
        checkCommonHeaders(response);
        expect(response.headers).to.have.property("x-frame-options", "DENY");
      });
    });
  });
});
