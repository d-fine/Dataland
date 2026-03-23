/**
 * Visits the quality assurance page and switches to the last table page
 */
export function visitQaOverviewAndGoToLastPage(): void {
  cy.visitAndCheckAppMount('/qualityassurance');
  cy.contains('span', 'REVIEW');
  cy.get('.p-paginator-last', { timeout: Cypress.env('medium_timeout_in_ms') as number }).then((element) => {
    if (element.prop('disabled')) {
      return;
    }
    element.trigger('click');
  });
}
