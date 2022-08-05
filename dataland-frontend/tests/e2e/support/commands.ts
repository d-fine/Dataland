import Chainable = Cypress.Chainable;

declare global {
  namespace Cypress {
    interface Chainable {
      visitAndCheckAppMount: typeof visitAndCheckAppMount;
    }
  }
}

export function visitAndCheckAppMount(endpoint: string): Chainable<JQuery> {
  return cy.visit(endpoint).get("#app").should("exist");
}

Cypress.Commands.add("visitAndCheckAppMount", visitAndCheckAppMount);
