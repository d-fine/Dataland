/**
 * Checks if the dataland footer element is present
 */
export function checkFooter(): void {
  cy.get('[data-test="dataland footer"]').should('exist');
}

/**
 * Verifies the header row of the company table
 */
export function verifySearchResultTableExists(): void {
  cy.get('table.p-datatable-table').contains('th', 'COMPANY');
  cy.get('table.p-datatable-table').contains('th', 'LEI');
  cy.get('table.p-datatable-table').contains('th', 'SECTOR');
  cy.get('table.p-datatable-table').contains('th', 'LOCATION');
}

/**
 * Verifies that the company cockpit for a specific company is currently displayed
 * @param expectedCompanyName of the company
 * @param expectedCompanyId of the company
 */
export function validateCompanyCockpitPage(expectedCompanyName: string, expectedCompanyId: string): void {
  cy.url().should('contain', `/companies/${expectedCompanyId}`);
  cy.get('h1[data-test="companyNameTitle"]', { timeout: Cypress.env('long_timeout_in_ms') as number }).should(
    'have.text',
    expectedCompanyName
  );
}

/**
 * Runs a function block within the prime-vue modal dialog window.
 * This can be used to ensure that cypress-assertions are actually run on elements inside the modal.
 * @param functionBlock to run within the modal
 */
export function runFunctionBlockWithinPrimeVueModal(functionBlock: () => void): void {
  const selectorForPrimeVueModal = '.p-dialog-mask';
  cy.get(selectorForPrimeVueModal).within(functionBlock);
}
