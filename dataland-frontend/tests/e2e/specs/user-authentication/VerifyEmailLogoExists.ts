describe("As a user I expect to find the dataland logo for emails", () => {
  it(`Test availability of dataland logo`, () => {
    cy.request(`${Cypress.config("baseUrl")!} + "/images/logos/logo_dataland_long.png`).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.length(2033);
    });
  });
});
