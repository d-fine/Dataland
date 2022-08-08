import { retrieveDataIdsList } from "../../utils/ApiUtils";
import { checkViewButtonWorks, verifyTaxonomySearchResultTable } from "../../utils/CompanySearch";

let companiesWithData: any;

before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyData").then(function (companies) {
    companiesWithData = companies;
  });
});

describe("As a user, I expect the search functionality on the /searchtaxonomy page to behave as I expect", function () {
  beforeEach(function () {
    cy.ensureLoggedIn();
  });

  function executeCompanySearch(inputValue: string) {
    cy.get("input[name=eu_taxonomy_search_bar_top]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .type("{enter}")
      .should("have.value", inputValue);
    cy.get("h2").should("contain", "Results");
    cy.get("table.p-datatable-table").should("exist");
  }

  it("Type smth into search bar, wait 1 sec, type enter, and expect to see search results on new page", function () {
    retrieveDataIdsList().then((dataIdList: any) => {
      cy.visitAndCheckAppMount("/companies/" + dataIdList[2] + "/eutaxonomies");
    });
    cy.get("h2").should("contain", "EU Taxonomy Data");
    const inputValue = "A";
    cy.get("input[name=eu_taxonomy_search_bar_standard]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .should("have.value", inputValue)
      .wait(1000)
      .type("{enter}");
    cy.url().should("include", "/searchtaxonomy?input=" + inputValue);
    cy.get("h2").should("contain", "Results");
    cy.get("table.p-datatable-table").should("exist");
  });

  it("Check static layout of the search page", function () {
    cy.visitAndCheckAppMount("/searchtaxonomy");
    cy.get("h1").should("contain", "Search EU Taxonomy data");
    const placeholder = "Search company by name or PermID";
    const inputValue = "A company name";
    cy.get("input[name=eu_taxonomy_search_bar_top]")
      .should("not.be.disabled")
      .type(inputValue)
      .should("have.value", inputValue)
      .invoke("attr", "placeholder")
      .should("contain", placeholder);
  });

  it("Company Search by Name", () => {
    function checkViewRowsWorks(): void {
      cy.get("table.p-datatable-table");
      cy.contains("td", "VIEW")
        .siblings()
        .contains("€")
        .click()
        .url()
        .should("include", "/companies/")
        .url()
        .should("include", "/eutaxonomies");
    }

    function checkPermIdToolTip(permIdTextInt: string) {
      cy.get('.material-icons[title="Perm ID"]').trigger("mouseenter", "center");
      cy.get(".p-tooltip").should("be.visible").contains(permIdTextInt);
      cy.get('.material-icons[title="Perm ID"]').trigger("mouseleave");
      cy.get(".p-tooltip").should("not.exist");
    }

    cy.visitAndCheckAppMount("/searchtaxonomy");
    const inputValue = companiesWithData[0].companyInformation.companyName;
    const permIdText = "Permanent Identifier (PermID)";
    executeCompanySearch(inputValue);
    verifyTaxonomySearchResultTable();
    checkPermIdToolTip(permIdText);
    checkViewButtonWorks();
    cy.get("h1").contains(inputValue);
    cy.get("[title=back_button").should("be.visible").click({ force: true });
    checkViewRowsWorks();
    cy.get("h1").contains(inputValue);
  });

  it("Company Search by Identifier", () => {
    cy.visitAndCheckAppMount("/searchtaxonomy");
    const inputValue = companiesWithData[1].companyInformation.identifiers[0].identifierValue;
    executeCompanySearch(inputValue);
    verifyTaxonomySearchResultTable();
    checkViewButtonWorks();
  });

  it("Search Input field should be always present", () => {
    const placeholder = "Search company by name or PermID";
    const inputValue = "A company name";
    retrieveDataIdsList().then((dataIdList: any) => {
      cy.visitAndCheckAppMount("/companies/" + dataIdList[7] + "/eutaxonomies");
      cy.get("input[name=eu_taxonomy_search_bar_standard]")
        .should("not.be.disabled")
        .type(inputValue)
        .should("have.value", inputValue)
        .invoke("attr", "placeholder")
        .should("contain", placeholder);
    });
  });

  it("Click on an autocomplete-suggestion and check if forwarded to taxonomy data page", () => {
    cy.visitAndCheckAppMount("/searchtaxonomy");
    cy.intercept("**/api/companies*").as("searchCompany");
    cy.get("input[name=eu_taxonomy_search_bar_top]").click({ force: true }).type("b");
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

  it("Check if the autocomplete entries are highlighted", () => {
    cy.visitAndCheckAppMount("/searchtaxonomy");
    cy.intercept("**/api/companies*").as("searchCompany");
    cy.get("input[name=eu_taxonomy_search_bar_top]").click({ force: true }).type("-");
    cy.wait("@searchCompany", { timeout: 2 * 1000 }).then(() => {
      cy.get(".p-autocomplete-item").eq(0).get("span[class='font-semibold']").contains("-").should("exist");
    });
  });

  it("Type b into the search bar, click on ViewAllResults, and check if all results for b are displayed", () => {
    cy.visitAndCheckAppMount("/searchtaxonomy");
    cy.intercept("**/api/companies*").as("searchCompany");
    cy.get("input[name=eu_taxonomy_search_bar_top]").type("b");
    cy.get(".p-autocomplete-item").contains("View all results").click();
    cy.wait("@searchCompany", { timeout: 2 * 1000 }).then(() => {
      verifyTaxonomySearchResultTable();
      cy.url().should("include", "/searchtaxonomy?input=b");
    });
  });

  it("Scroll the page and check if search icon and search bar behave as expected", () => {
    cy.visitAndCheckAppMount("/searchtaxonomy");
    cy.get("input[name=eu_taxonomy_search_bar_top]").type("a").type("{enter}");
    cy.get("button[name=search_bar_collapse]").should("not.exist");

    cy.scrollTo(0, 500, { duration: 300 });
    cy.get("input[name=eu_taxonomy_search_bar_top]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("exist");

    cy.scrollTo(0, 0, { duration: 300 });
    cy.get("input[name=eu_taxonomy_search_bar_top]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("not.exist");

    cy.scrollTo(0, 500, { duration: 300 });
    cy.get("button[name=search_bar_collapse]").should("exist").click();
    cy.get("input[name=eu_taxonomy_search_bar_top]").should("not.exist");
    cy.get("input[name=eu_taxonomy_search_bar_scrolled]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("not.exist");

    cy.scrollTo(0, 480, { duration: 300 });
    cy.get("button[name=search_bar_collapse]").should("exist");
    cy.get("input[name=eu_taxonomy_search_bar_top]").should("exist");
    cy.get("input[name=eu_taxonomy_search_bar_scrolled]").should("not.exist");
  });

  it("Scroll the page to type into the search bar in different states and check if the input is always saved", () => {
    const inputValue1 = "ABCDEFG";
    const inputValue2 = "XYZ";
    cy.visitAndCheckAppMount("/searchtaxonomy");
    cy.get("input[name=eu_taxonomy_search_bar_top]").type(inputValue1);
    cy.scrollTo(0, 500);
    cy.get("button[name=search_bar_collapse]").click();
    cy.get("input[name=eu_taxonomy_search_bar_scrolled]").should("have.value", inputValue1).type(inputValue2);
    cy.scrollTo(0, 0);
    cy.get("input[name=eu_taxonomy_search_bar_top]").should("have.value", inputValue1 + inputValue2);
  });
});
