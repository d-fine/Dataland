describe("As a user, I want to be able to use the swagger UI to send requests to the backend", () => {
  it("Checks that one is able to open swagger and send a request", () => {
    cy.visit(`${Cypress.env("API")}/swagger-ui/index.html`)
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
});
