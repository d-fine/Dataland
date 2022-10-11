import { getCompanyAndDataIds } from "@e2e/utils/ApiUtils";
import {
  CompanyInformation,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
  DataTypeEnum,
  StoredCompany,
} from "@clients/backend";

let companiesWithData: Array<{
  companyInformation: CompanyInformation;
  euTaxonomyDataForFinancials: EuTaxonomyDataForFinancials;
  euTaxonomyDataForNonFinancials: EuTaxonomyDataForNonFinancials;
}>;

before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (outputFromJson) {
    companiesWithData = outputFromJson;
  });
});

describe("As a user, I expect the search functionality on the /companies page to behave as I expect", function () {
  beforeEach(function () {
    cy.ensureLoggedIn();
  });

  function verifyTaxonomySearchResultTable(): void {
    cy.get("table.p-datatable-table").contains("th", "COMPANY");
    cy.get("table.p-datatable-table").contains("th", "PERM ID");
    cy.get("table.p-datatable-table").contains("th", "SECTOR");
    cy.get("table.p-datatable-table").contains("th", "LOCATION");
  }

  function verifyPaginator(): void {
    cy.get("div[class='p-paginator p-component p-paginator-bottom']").should("exist");
  }

  function executeCompanySearchWithStandardSearchBar(inputValue: string) {
    const inputValueUntilFirstSpace = inputValue.substring(0, inputValue.indexOf(" "));
    cy.get("input[name=search_bar_top]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .should("have.value", inputValue)
      .type("{enter}")
      .should("have.value", inputValue);
    cy.url({ decode: true }).should("include", "/companies?input=" + inputValueUntilFirstSpace);
    verifyTaxonomySearchResultTable();
  }

  it("Check static layout of the search page", function () {
    cy.visitAndCheckAppMount("/companies");
    const placeholder = "Search company by name or PermID";
    const inputValue = "A company name";
    cy.get("input[name=search_bar_top]")
      .should("not.be.disabled")
      .type(inputValue)
      .should("have.value", inputValue)
      .invoke("attr", "placeholder")
      .should("contain", placeholder);
  });

  it("Type b into the search bar, click on ViewAllResults, and check if all results for b are displayed", () => {
    cy.visitAndCheckAppMount("/companies");
    cy.intercept("**/api/companies*").as("searchCompany");
    cy.get("input[name=search_bar_top]").type("b");
    cy.get(".p-autocomplete-item").contains("View all results").click();
    cy.wait("@searchCompany", { timeout: 2 * 1000 }).then(() => {
      verifyTaxonomySearchResultTable();
      cy.url().should("include", "/companies?input=b");
    });
  });

  it("Scroll the page and check if search icon and search bar behave as expected", () => {
    cy.visitAndCheckAppMount("/companies");
    cy.get("input[name=search_bar_top]").type("a").type("{enter}");
    cy.get("button[name=search_bar_collapse]").should("not.be.visible");
    verifyPaginator();

    cy.scrollTo(0, 500, { duration: 300 });
    cy.get("input[name=search_bar_top]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("be.visible");

    cy.scrollTo(0, 0, { duration: 300 });
    cy.get("input[name=search_bar_top]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("not.be.visible");

    cy.scrollTo(0, 500, { duration: 300 });
    cy.get("button[name=search_bar_collapse]").should("exist").click();
    cy.get("input[name=search_bar_top]").should("not.exist");
    cy.get("input[name=search_bar_scrolled]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("not.be.visible");

    cy.scrollTo(0, 480, { duration: 300 });
    cy.get("button[name=search_bar_collapse]").should("be.visible");
    cy.get("input[name=search_bar_top]").should("exist");
    cy.get("input[name=search_bar_scrolled]").should("not.exist");
  });

  it("Scroll the page to type into the search bar in different states and check if the input is always saved", () => {
    const inputValue1 = "ABCDEFG";
    const inputValue2 = "XYZ";
    cy.visitAndCheckAppMount("/companies");
    cy.get("input[name=search_bar_top]").type(inputValue1);
    verifyPaginator();
    cy.scrollTo(0, 500);
    cy.get("button[name=search_bar_collapse]").click();
    cy.get("input[name=search_bar_scrolled]").should("have.value", inputValue1).type(inputValue2);
    cy.scrollTo(0, 0);
    cy.get("input[name=search_bar_top]").should("have.value", inputValue1 + inputValue2);
  });

  it("Check PermId tooltip, execute company search by name, check result table and assure VIEW button works", () => {
    cy.visitAndCheckAppMount("/companies");

    function checkPermIdToolTip(permIdTextInt: string) {
      cy.get('.material-icons[title="Perm ID"]').trigger("mouseenter", "center");
      cy.get(".p-tooltip").should("be.visible").contains(permIdTextInt);
      cy.get('.material-icons[title="Perm ID"]').trigger("mouseleave");
      cy.get(".p-tooltip").should("not.exist");
    }

    function checkViewButtonWorks() {
      cy.get("table.p-datatable-table")
        .contains("td", "VIEW")
        .contains("a", "VIEW")
        .click()
        .url()
        .should("include", "/frameworks");
    }

    cy.visitAndCheckAppMount("/companies");
    const inputValue = companiesWithData[0].companyInformation.companyName;
    const permIdText = "Permanent Identifier (PermID)";
    checkPermIdToolTip(permIdText);
    executeCompanySearchWithStandardSearchBar(inputValue);
    verifyTaxonomySearchResultTable();
    checkViewButtonWorks();
    cy.get("h1").contains(inputValue);
    cy.get("[title=back_button").should("be.visible").click({ force: true });
    checkViewButtonWorks();
    cy.get("h1").contains(inputValue);
  });

  it("Execute a company Search by identifier and assure that the company is found", () => {
    cy.visitAndCheckAppMount("/companies");
    const inputValue = companiesWithData[0].companyInformation.identifiers[0].identifierValue;
    const expectedCompanyName = companiesWithData[0].companyInformation.companyName;
    executeCompanySearchWithStandardSearchBar(inputValue);
    cy.get("td[class='d-bg-white w-3 d-datatable-column-left']").contains(expectedCompanyName);
  });

  it("Visit framework data view page and assure that title is present and a Framework Data Search Bar exists", () => {
    const placeholder = "Search company by name or PermID";
    const inputValue = "A company name";
    cy.getKeycloakToken("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD")).then((token) => {
      getCompanyAndDataIds(token, DataTypeEnum.EutaxonomyNonFinancials).then(
        (storedCompanies: Array<StoredCompany>) => {
          cy.visitAndCheckAppMount(`/companies}/frameworks/${storedCompanies[0].companyId}/eutaxonomy-non-financials`);
          cy.get("input[name=framework_data_search_bar_standard]")
            .should("not.be.disabled")
            .type(inputValue)
            .should("have.value", inputValue)
            .invoke("attr", "placeholder")
            .should("contain", placeholder);
        }
      );
    });
  });

  it("Click on an autocomplete-suggestion and check if forwarded to company framework data page", () => {
    cy.visitAndCheckAppMount("/companies");
    cy.intercept("**/api/companies*").as("searchCompany");
    cy.get("input[name=search_bar_top]").click({ force: true }).type("b");
    cy.wait("@searchCompany", { timeout: 2 * 1000 }).then(() => {
      cy.get(".p-autocomplete-item")
        .eq(0)
        .click({ force: true })
        .url()
        .should("include", "/companies/")
        .url()
        .should("include", "/frameworks/eutaxonomy");
    });
  });

  it("Check if the autocomplete entries are highlighted", () => {
    cy.visitAndCheckAppMount("/companies");
    cy.intercept("**/api/companies*").as("searchCompany");
    cy.get("input[name=search_bar_top]").click({ force: true }).type("-");
    cy.wait("@searchCompany", { timeout: 2 * 1000 }).then(() => {
      cy.get(".p-autocomplete-item").eq(0).get("span[class='font-semibold']").contains("-").should("exist");
    });
  });
});
