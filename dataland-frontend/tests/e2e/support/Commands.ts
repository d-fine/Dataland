import Chainable = Cypress.Chainable;
import { ensureLoggedIn, getKeycloakToken } from "@e2e/utils/Auth";
import { browserThen } from "@e2e/utils/Cypress";
import {rmdir} from "fs";

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

export function deleteDownloadsFolder(): Promise<void | null> {
  return new Promise((resolve, reject) => {
    rmdir(Cypress.config("downloadsFolder"), {recursive: true}, (err) => {
      if (err) {
        console.error(err)
        return reject(err)
      }
      resolve(null)
    })
  })
}

Cypress.Commands.add("visitAndCheckAppMount", visitAndCheckAppMount);
Cypress.Commands.add("deleteDownloadsFolder", deleteDownloadsFolder);
Cypress.Commands.add("ensureLoggedIn", ensureLoggedIn);
Cypress.Commands.add("getKeycloakToken", getKeycloakToken);
Cypress.Commands.add("browserThen", browserThen);
