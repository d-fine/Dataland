import { CompanyInformationIndicesEnum } from "../../../../build/clients/backend/org/dataland/datalandfrontend/openApiClient/model";
import { checkViewButtonWorks, verifyTaxonomySearchResultTable } from "../../utils/CompanySearch";

const numberOfStockIndices = Object.keys(CompanyInformationIndicesEnum).length;

describe("As a user, I expect the index selection tabs to work on /searchtaxonomy", function () {
  const indexTabMenu = ".p-tabmenu > .p-tabmenu-nav > .p-tabmenuitem > .p-menuitem-link";
  beforeEach(() => {
    cy.ensureLoggedIn();
  });

  it("Index tabmenu should be present", () => {
    cy.visitAndCheckAppMount("/companies");
    cy.get(".p-tabmenuitem").should("have.length", numberOfStockIndices);
    cy.get(indexTabMenu).should("exist").eq(1).parent(".p-tabmenuitem").should("have.css", "color", "rgb(27, 27, 27)");
    verifyTaxonomySearchResultTable();
    checkViewButtonWorks();
    cy.get(indexTabMenu).should("not.exist");
  });

  it("Visit companies search page, scroll to the bottom, back to the top, and check if Dax still highlighted", () => {
    cy.visitAndCheckAppMount("/companies");
    function checkIfDaxTabIsHighlighted(): void {
      cy.get('li[class="p-tabmenuitem p-highlight"]')
        .children(".p-menuitem-link")
        .children(".p-menuitem-text")
        .should("contain", "DAX");
    }

    checkIfDaxTabIsHighlighted();

    cy.scrollTo("bottom", { duration: 500 });
    cy.scrollTo("top", { duration: 500 });

    checkIfDaxTabIsHighlighted();
  });
});
