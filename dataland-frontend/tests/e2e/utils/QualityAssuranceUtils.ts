/**
 * Switches to the last table page on the qa Overiew Page
 */
export function qaOverviewNavigateToLastPage(): void {
  cy.get('[data-test="qa-review-section"]').should('be.visible');
  cy.get('.p-paginator-last', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then((element) => {
    if (element.prop('disabled')) {
      return;
    }
    element.trigger('click');
  });
}
