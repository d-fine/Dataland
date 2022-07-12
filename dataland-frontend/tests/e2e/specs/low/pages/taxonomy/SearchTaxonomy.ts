import { checkViewButtonWorks, verifyTaxonomySearchResultTable } from "../../../../support/commands";

let companiesWithData: any;

before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyData").then(function (companies) {
    companiesWithData = companies;
  });
});

function executeCompanySearch(inputValue: string) {
  cy.get("input[name=eu_taxonomy_search_bar_standard]")
    .should("not.be.disabled")
    .click({ force: true })
    .type(inputValue)
    .type("{enter}")
    .should("have.value", inputValue);
  cy.get("h2").should("contain", "Results");
  cy.get("table.p-datatable-table").should("exist");
}

function checkPermIdToolTip(permIdText: string) {
  cy.get('.material-icons[title="Perm ID"]').trigger("mouseenter", "center");
  cy.get(".p-tooltip").should("be.visible").contains(permIdText);
  cy.get('.material-icons[title="Perm ID"]').trigger("mouseleave");
  cy.get(".p-tooltip").should("not.exist");
}

describe("Search Taxonomy", function () {
  beforeEach(function () {
    cy.restoreLoginSession();
  });

  it("Check static layout of the search page", function () {
    cy.visit("/searchtaxonomy");
    cy.get("#app").should("exist");
    cy.get("h1").should("contain", "Search EU Taxonomy data");
    const placeholder = "Search company by name or PermID";
    const inputValue = "A company name";
    cy.get("input[name=eu_taxonomy_search_bar_standard]")
      .should("not.be.disabled")
      .type(inputValue)
      .should("have.value", inputValue)
      .invoke("attr", "placeholder")
      .should("contain", placeholder);
  });

  it("Company Search by Name", () => {
    cy.visit("/searchtaxonomy");
    const inputValue = companiesWithData[0].companyInformation.companyName;
    const permIdText = "Permanent Identifier (PermID)";
    executeCompanySearch(inputValue);
    verifyTaxonomySearchResultTable();
    checkPermIdToolTip(permIdText);
    checkViewButtonWorks();
    cy.get("h1").contains(inputValue);
  });

  it("Company Search by Identifier", () => {
    cy.visit("/searchtaxonomy");
    const inputValue = companiesWithData[1].companyInformation.identifiers[0].identifierValue;
    executeCompanySearch(inputValue);
    verifyTaxonomySearchResultTable();
    checkViewButtonWorks();
  });

  it("Search Input field should be always present", () => {
    const placeholder = "Search company by name or PermID";
    const inputValue = "A company name";
    cy.retrieveDataIdsList().then((dataIdList: any) => {
      cy.visit("/companies/" + dataIdList[7] + "/eutaxonomies");
      cy.get("input[name=eu_taxonomy_search_bar_standard]")
        .should("not.be.disabled")
        .type(inputValue)
        .should("have.value", inputValue)
        .invoke("attr", "placeholder")
        .should("contain", placeholder);
    });
  });

  it("Autocomplete functionality", () => {
    cy.visit("/searchtaxonomy");
    cy.intercept("**/api/companies*").as("searchCompany");
    cy.get("input[name=eu_taxonomy_search_bar_standard]").click({ force: true }).type("b");
    cy.wait("@searchCompany", { timeout: 2 * 1000 }).then(() => {
      cy.get(".p-autocomplete-item")
        .eq(0)
        .click({ force: true })
        .url()
        .should("include", "/companies/")
        .url()
        .should("include", "/eutaxonomies");
    });
  });

  it("Scroll functionality", () => {
    cy.visit("/searchtaxonomy");
    cy.get("button[name=search_bar_collapse]").should("not.exist");
    cy.get("input[name=eu_taxonomy_search_bar_standard]").click({ force: true }).type("a").type("{enter}");
    cy.scrollTo(0, 500);
    cy.get("button[name=search_bar_collapse]").should("exist");

    cy.scrollTo(0, 0);
    cy.get("button[name=search_bar_collapse]").should("not.exist");

    cy.scrollTo(0, 500);
    cy.get("button[name=search_bar_collapse]").should("exist").click();
    cy.get("input[name=eu_taxonomy_search_bar_scrolled]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("not.exist");
  });

  it("Communication between standard search bar and search bar in scrolled mode", () => {
    const inputValue1 = "ABCDEFG";
    const inputValue2 = "XYZ";
    cy.visit("/searchtaxonomy");
    cy.get("input[name=eu_taxonomy_search_bar_standard]").type(inputValue1);
    cy.scrollTo(0, 500);
    cy.get("button[name=search_bar_collapse]").click();
    cy.get("input[name=eu_taxonomy_search_bar_scrolled]").should("have.value", inputValue1).type(inputValue2);
    cy.scrollTo("top");
    cy.get("input[name=eu_taxonomy_search_bar_standard]").should("have.value", inputValue1 + inputValue2);
  });
});
