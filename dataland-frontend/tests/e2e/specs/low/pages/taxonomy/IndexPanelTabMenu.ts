import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
import {
  checkViewButtonWorks,
  verifyResultTable,
} from "../../../../support/commands";

const numberOfStockIndices =
  apiSpecs.components.schemas.CompanyInformation.properties["indices"].items
    .enum.length;

describe("Index Panel behavior", function () {
  const indexTabMenu =
    ".p-tabmenu > .p-tabmenu-nav > .p-tabmenuitem > .p-menuitem-link";
  beforeEach(() => {
    cy.restoreLoginSession();
  });

  it("Index tabmenu should be present on first visit", () => {
    cy.visit("/searchtaxonomy");
    cy.get(".p-tabmenuitem").should("have.length", numberOfStockIndices);
    cy.get(indexTabMenu)
      .should("exist")
      .eq(1)
      .parent(".p-tabmenuitem")
      .should("have.css", "color", "rgb(27, 27, 27)");
    verifyResultTable();
    checkViewButtonWorks();
    cy.get(indexTabMenu).should("not.exist");
  });

  it("Index tabmenu should be present", () => {
    cy.visit("/searchtaxonomy");
    cy.get(indexTabMenu).should("exist");
    cy.get(".grid").should("not.contain", "Choose by stock market index");
    verifyResultTable();
    checkViewButtonWorks();
  });
});
