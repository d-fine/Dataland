import Chainable = Cypress.Chainable;
import { ensureLoggedIn, getKeycloakToken } from "@e2e/utils/Auth";
import { browserThen } from "@e2e/utils/Cypress";

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      visitAndCheckAppMount: typeof visitAndCheckAppMount;
      ensureLoggedIn: typeof ensureLoggedIn;
      getKeycloakToken: typeof getKeycloakToken;
      browserThen: typeof browserThen;
    }
  }
}

/**
 * Visits the provided endpoint and verifies that the Vue #app component exists
 *
 * @param endpoint the endpoint to navigate to
 * @returns the cypress chainable
 */
export function visitAndCheckAppMount(endpoint: string): Chainable<JQuery> {
  return cy.visit(endpoint).get("#app").should("exist");
}

Cypress.Commands.add("visitAndCheckAppMount", visitAndCheckAppMount);
Cypress.Commands.add("ensureLoggedIn", ensureLoggedIn);
Cypress.Commands.add("getKeycloakToken", getKeycloakToken);
Cypress.Commands.add("browserThen", browserThen);
