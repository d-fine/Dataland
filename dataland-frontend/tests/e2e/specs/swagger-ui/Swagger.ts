import {admin_name, admin_pw, getBaseUrl} from "@e2e/utils/Cypress";
import Chainable = Cypress.Chainable;
import {generateDummyCompanyInformation, uploadCompanyViaApi} from "@e2e/utils/CompanyUpload";
import {getKeycloakToken} from "@e2e/utils/Auth";

describe("As a user, I want to be able to use the swagger UI to send requests to the backend", () => {
  it("Checks that one is able to open swagger and send a request", () => {
    cy.visit(`/api/swagger-ui/index.html`)
      .get("#operations-Actuator-health button.opblock-summary-control")
      .should("exist")
      .click()
      .get("#operations-Actuator-health button.try-out__btn")
      .should("exist")
      .click()
      .get("#operations-Actuator-health button.execute")
      .should("exist")
      .click()
      .get("#operations-Actuator-health .live-responses-table td.response-col_status")
      .contains("200");
  });

  it("Checks that requests to internal api endpoints are redirected to nocontent page", () => {
    cy.visit(`/api/internal/swagger-ui/index.html`)
      .url()
      .should("eq", getBaseUrl() + "/nocontent");
  });

  it.only("Checks that POST requests of the individual frameworks work with the default values", async () => {
    getKeycloakToken(admin_name, admin_pw).then((token) => {
      // console.log("XXXX")
      // cy.log("XXXX")
      return uploadCompanyViaApi(token, generateDummyCompanyInformation(`Swagger Corp. ${new Date().getTime()}`))
    })
  //     .then((company) => {
  //     console.log(company.companyId)
  //     cy.log("BBBB")
  //     cy.log(company.companyId)
  //     cy.ensureLoggedIn(admin_name, admin_pw);
  //     cy.visit("/api/swagger-ui/index.html");
  //
  //     cy.get("button.authorize").click();
  //     cy.intercept("**/keycloak/realms/datalandsecurity/protocol/openid-connect/token*").as("authorize")
  //     cy.get(".dialog-ux .auth-container button.authorize").last().click()
  //     cy.wait("@authorize")
  //     cy.get(".dialog-ux button.close-modal").click()
  //     cy.log("ASDF")
  //   });
  //
  //   // const postCompanyRequestId = "#operations-company-data-controller-postCompany";
  //   // toggleRequestPanel(postCompanyRequestId)
  //   // tryItOut(postCompanyRequestId)
  //   // // TODO randomize lei
  //   // executeRequest(postCompanyRequestId)
  //   // validateSuccess(postCompanyRequestId)
  //   // // TODO get company id
  //   //
  //   // // TODO for each framework upload default
  });

  function toggleRequestPanel(requestId: string): void {
    cy.get(requestId).find(".opblock-summary-control").click();
  }

  function tryItOut(requestId: string): void {
    cy.get(requestId).find("button.try-out__btn").click();
  }

  function executeRequest(requestId: string): void {
    cy.get(requestId).find("button.execute").click();
  }

  function validateSuccess(requestId: string): void {
    cy.get(requestId).find(".live-responses-table tbody td").first().should("contain.text", "200");
  }
});
