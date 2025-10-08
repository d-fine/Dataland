describe('As a user I expect api key link will be visible in the menu', () => {
  it('successfully redirects to the page api-key', () => {
    cy.ensureLoggedIn();

    cy.visitAndCheckAppMount('/requests');

    cy.get('div.user-menu-container').click();
    cy.get('[data-test="profileMenu"]').should('be.visible');
    cy.get('div.user-menu-container').click();
    cy.get('[data-test="profileMenu"]').should('not.exist');
    cy.get('div.user-menu-container').click();
    cy.get('.p-menu-item-label').contains('API KEY').click();
    cy.url().should('include', '/api-key');
  });
});
