const longTimeoutInMs = Number(Cypress.expose('long_timeout_in_ms') ?? 30000);

/**
 * Verifies the successful creation of the request on the single request page
 * @param company The company searched for
 * @param emailChecked true if the field should be checked, false otherwise
 */
export function verifyOnSingleRequestPage(company: string, emailChecked: boolean): void {
  cy.url({ timeout: longTimeoutInMs }).should('contain', '/requests/');
  cy.get(`div.card__data:contains("${company}")`).scrollIntoView();
  cy.get(`div.card__data:contains("${company}")`).should('be.visible');
  cy.get('[data-test="card_requestIs"]').should('contain.text', 'Request is:Openand Access is:Public since');
  cy.get('[data-test="notifyMeImmediatelyInput"]').should(
    (emailChecked ? '' : 'not.') + 'have.class',
    'p-toggleswitch-checked'
  );
}
