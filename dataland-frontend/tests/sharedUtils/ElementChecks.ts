/**
 * Checks if the dataland footer element is present
 */
export function checkFooter(): void {
  cy.get('[data-test="dataland footer"]').should("exist");
}
