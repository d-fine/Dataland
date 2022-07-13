import Chainable = Cypress.Chainable;
import {visitAndCheckAppMount} from "../../../support/commands";

export function checkIfDaxTabIsHighlighted(): Chainable<JQuery> {
  return cy
    .get('li[class="p-tabmenuitem p-highlight"]')
    .children(".p-menuitem-link")
    .children(".p-menuitem-text")
    .should("contain", "DAX");
}

describe("Index Highlighting test suite", () => {
  it("Visit searchtaxonomy page, scroll to the bottom, back to the top, and check if Dax still highlighted", () => {
    cy.restoreLoginSession();
    visitAndCheckAppMount("/searchtaxonomy");

    checkIfDaxTabIsHighlighted();

    cy.scrollTo("bottom", { duration: 500 });
    cy.scrollTo("top", { duration: 500 });

    checkIfDaxTabIsHighlighted();
  });
});
