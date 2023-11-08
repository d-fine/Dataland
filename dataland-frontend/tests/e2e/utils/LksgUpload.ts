/**
 * Scrolls to the top and bottom of the LKSG input form and checks if the sidebar correctly switches from sticky to non-sticky while doing so.
 */
export function checkStickynessOfSubmitSideBar(): void {
  cy.scrollTo("bottom");
  cy.get("[data-test='submitSideBar']").should("have.css", "position", "fixed").and("have.css", "top", "60px");
  cy.scrollTo("top");
  cy.get("[data-test='submitSideBar']").should("have.css", "position", "relative").and("have.css", "top", "0px");
}

// TODO delete this at the end probably??? Only one function left
