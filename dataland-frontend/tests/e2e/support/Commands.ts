import Chainable = Cypress.Chainable;
import { ensureLoggedIn, getKeycloakToken } from "@e2e/utils/Auth";
import { browserThen } from "@e2e/utils/Cypress";
// import "cypress-file-upload"; TODO: Ask Florian => do we need this?

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      visitAndCheckAppMount: typeof visitAndCheckAppMount;
      deleteDownloadsFolder: typeof deleteDownloadsFolder;
      ensureLoggedIn: typeof ensureLoggedIn;
      getKeycloakToken: typeof getKeycloakToken;
      browserThen: typeof browserThen;
    }
  }
}

export function visitAndCheckAppMount(endpoint: string): Chainable<JQuery> {
  return cy.visit(endpoint).get("#app").should("exist");
}

export function deleteDownloadsFolder(): Chainable<void> {
  return cy.task("deleteFolder", Cypress.config("downloadsFolder"));
}

Cypress.Commands.add("visitAndCheckAppMount", visitAndCheckAppMount);
Cypress.Commands.add("deleteDownloadsFolder", deleteDownloadsFolder);
Cypress.Commands.add("ensureLoggedIn", ensureLoggedIn);
Cypress.Commands.add("getKeycloakToken", getKeycloakToken);
Cypress.Commands.add("browserThen", browserThen);
