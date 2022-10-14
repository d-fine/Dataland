describe("As a user, I want to be able to use the swagger UI to send requests to the backend", () => {
  it("Checks that one is able to open swagger, authorize and send a get company request", () => {
    cy.ensureLoggedIn();
    cy.visit(`/api/swagger-ui/index.html`)
      .get('button[class="btn authorize unlocked"]')
      .click()
      .get('button[class="btn modal-btn auth authorize button"]')
      .eq(1)
      .click()
      .get('button[class="close-modal"]')
      .click()
      .get("#operations-company-data-controller-getCompanies button.opblock-summary-control")
      .should("exist")
      .click()
      .get("#operations-company-data-controller-getCompanies button.try-out__btn")
      .should("exist")
      .click()
      .get("#operations-company-data-controller-getCompanies button.execute")
      .should("exist")
      .click()
      .get("#operations-company-data-controller-getCompanies .live-responses-table td.response-col_status")
      .contains("200");
  });
});
