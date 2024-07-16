/**
 * Checks the existence and beahviour elements of the email details view
 * @param dataTestParentComponent the parentComponent
 * @param dataTestPatchButton the button that triggers the patch
 */
export function checkEmailFieldsAndCheckBox(dataTestParentComponent: string, dataTestPatchButton: string): void {
  const testEmail = 'test1234@example.com';
  const testMessage = 'test message 1234';
  cy.get(`[data-test="${dataTestParentComponent}"]`)
    .should('exist')
    .should('be.visible')
    .within(() => {
      cy.get('[data-test="acceptConditionsCheckbox"]').should('exist').should('not.be.visible');
      cy.get('[data-test="contactEmail"]').should('exist').type(testEmail);
      cy.get('[data-test="dataRequesterMessage"]').should('exist').type(testMessage);
      cy.get('[data-test="acceptConditionsCheckbox"]').should('exist').should('be.visible');
    });
  cy.get(`[data-test="${dataTestPatchButton}"]`).should('exist').click({ force: true });
  cy.get(`[data-test="${dataTestParentComponent}"]`)
    .should('exist')
    .should('be.visible')
    .contains('You have to declare that the recipient(s) consented in order to add a message');
  cy.get('[data-test="acceptConditionsCheckbox"]').should('exist').should('be.visible').click();
  cy.get(`[data-test="${dataTestPatchButton}"]`).should('exist').click({ force: true });
  cy.get(`[data-test="${dataTestParentComponent}"]`).should('not.exist');
}
