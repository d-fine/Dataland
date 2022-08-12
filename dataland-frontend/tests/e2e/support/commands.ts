import Chainable = Cypress.Chainable;
import { ensureLoggedIn, getKeycloakToken } from "../utils/Auth";

declare global {
  namespace Cypress {
    interface Chainable {
      visitAndCheckAppMount: typeof visitAndCheckAppMount;
      ensureLoggedIn: typeof ensureLoggedIn;
      getKeycloakToken: typeof getKeycloakToken;
    }
  }
}

export function visitAndCheckAppMount(endpoint: string): Chainable<JQuery> {
  return cy.visit(endpoint).get("#app").should("exist");
}

Cypress.Commands.add("visitAndCheckAppMount", visitAndCheckAppMount);
Cypress.Commands.add("ensureLoggedIn", ensureLoggedIn);
Cypress.Commands.add("getKeycloakToken", getKeycloakToken);
