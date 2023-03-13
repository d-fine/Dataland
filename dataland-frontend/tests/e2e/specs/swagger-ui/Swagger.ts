import { getBaseUrl } from "@e2e/utils/Cypress";

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
});
