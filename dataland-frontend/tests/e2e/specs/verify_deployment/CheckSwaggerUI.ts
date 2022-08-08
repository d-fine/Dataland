describe("Check that swagger ui is present and able to send requests", () => {
  it("should be able to open swagger and send a request", () => {
    cy.visit("/api/swagger-ui/index.html")
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
