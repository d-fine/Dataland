import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";

const numberOfStockIndices = apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum.length;

export function checkIfDaxTabIsHighlighted(): void {
  cy.get('li[class="p-tabmenuitem p-highlight"]')
    .children(".p-menuitem-link")
    .children(".p-menuitem-text")
    .should("contain", "DAX");
}

describe("Index Panel behavior", function () {
  const indexTabMenu = ".p-tabmenu > .p-tabmenu-nav > .p-tabmenuitem > .p-menuitem-link";
  beforeEach(() => {
    cy.restoreLoginSession();
  });

  it("Index tabmenu should be present", () => {
    cy.visitAndCheckAppMount("/companies");
    cy.get(".p-tabmenuitem").should("have.length", numberOfStockIndices);
    cy.get(indexTabMenu).should("exist").eq(1).parent(".p-tabmenuitem").should("have.css", "color", "rgb(27, 27, 27)");
    cy.verifyTaxonomySearchResultTable();
    cy.checkViewButtonWorks();
    cy.get(indexTabMenu).should("not.exist");
  });

  it("Visit searchtaxonomy page, scroll to the bottom, back to the top, and check if Dax still highlighted", () => {
    cy.visitAndCheckAppMount("/companies");

    checkIfDaxTabIsHighlighted();

    cy.scrollTo("bottom", { duration: 500 });
    cy.scrollTo("top", { duration: 500 });

    checkIfDaxTabIsHighlighted();
  });
});
