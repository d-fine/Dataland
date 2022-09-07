import { retrieveFirstCompanyIdWithFrameworkData } from "../../utils/ApiUtils";
import { checkViewButtonWorks, verifyTaxonomySearchResultTable } from "../../utils/CompanySearch";
import {
  CompanyInformation,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
} from "../../../../build/clients/backend";
import { createCompanyAndGetId } from "../../utils/CompanyUpload";
import { uploadEuTaxonomyDataForNonFinancials } from "../../utils/EuTaxonomyNonFinancialsUpload";
import { describeIf } from "../../support/TestUtility";
import { uploadDummyEuTaxonomyDataForFinancials } from "../../utils/EuTaxonomyFinancialsUpload";

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

  function executeCompanySearch(inputValue: string) {
    cy.get("input[name=search_bar_top]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .type("{enter}")
      .should("have.value", inputValue);
    cy.get("table.p-datatable-table").should("exist");
  }

  it("Type smth into search bar, wait 1 sec, type enter, and expect to see search results on new page", function () {
    retrieveFirstCompanyIdWithFrameworkData("eutaxonomy-non-financials").then((companyId: string) => {
      cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-non-financials`);
    });
    cy.get("h2").should("contain", "EU Taxonomy Data");
    const inputValue = "A";
    cy.get("input[name=framework_data_search_bar_standard]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .should("have.value", inputValue)
      .wait(1000)
      .type("{enter}");
    cy.url().should("include", "/companies?input=" + inputValue);
    cy.get("table.p-datatable-table").should("exist");
  });

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

  it("Company Search by Name", () => {
    cy.visitAndCheckAppMount("/companies");

    function checkViewRowsWorks(): void {
      cy.get("table.p-datatable-table");
      cy.contains("td", "VIEW").siblings().contains("€").click().url().should("include", "/companies/");
    }

    function checkPermIdToolTip(permIdTextInt: string) {
      cy.get('.material-icons[title="Perm ID"]').trigger("mouseenter", "center");
      cy.get(".p-tooltip").should("be.visible").contains(permIdTextInt);
      cy.get('.material-icons[title="Perm ID"]').trigger("mouseleave");
      cy.get(".p-tooltip").should("not.exist");
    }

    cy.visitAndCheckAppMount("/companies");
    const inputValue = companiesWithData[0].companyInformation.companyName;
    const permIdText = "Permanent Identifier (PermID)";
    checkPermIdToolTip(permIdText);
    executeCompanySearch(inputValue);
    verifyTaxonomySearchResultTable();
    checkViewButtonWorks();
    cy.get("h1").contains(inputValue);
    cy.get("[title=back_button").should("be.visible").click({ force: true });
    checkViewRowsWorks();
    cy.get("h1").contains(inputValue);
  });

  it("Company Search by Identifier", () => {
    cy.visitAndCheckAppMount("/companies");
    const inputValue = companiesWithData[1].companyInformation.identifiers[0].identifierValue;
    // TODO Test ist nicht streng genug. Sollte assuren dass die tatsächliche Firma gefunden wird.
    executeCompanySearch(inputValue);
    verifyTaxonomySearchResultTable();
    checkViewButtonWorks();
  });

  it("Search Input field should always be present", () => {
    const placeholder = "Search company by name or PermID";
    const inputValue = "A company name";
    retrieveFirstCompanyIdWithFrameworkData("eutaxonomy-non-financials").then((companyId: any) => {
      cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/eutaxonomy-non-financials`);
      cy.get("input[name=framework_data_search_bar_standard]")
        .should("not.be.disabled")
        .type(inputValue)
        .should("have.value", inputValue)
        .invoke("attr", "placeholder")
        .should("contain", placeholder);
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
    cy.get("button[name=search_bar_collapse]").should("not.exist");

    cy.scrollTo(0, 500, { duration: 300 });
    cy.get("input[name=search_bar_top]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("exist");

    cy.scrollTo(0, 0, { duration: 300 });
    cy.get("input[name=search_bar_top]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("not.exist");

    cy.scrollTo(0, 500, { duration: 300 });
    cy.get("button[name=search_bar_collapse]").should("exist").click();
    cy.get("input[name=search_bar_top]").should("not.exist");
    cy.get("input[name=search_bar_scrolled]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("not.exist");

    cy.scrollTo(0, 480, { duration: 300 });
    cy.get("button[name=search_bar_collapse]").should("exist");
    cy.get("input[name=search_bar_top]").should("exist");
    cy.get("input[name=search_bar_scrolled]").should("not.exist");
  });

  it("Scroll the page to type into the search bar in different states and check if the input is always saved", () => {
    const inputValue1 = "ABCDEFG";
    const inputValue2 = "XYZ";
    cy.visitAndCheckAppMount("/companies");
    cy.get("input[name=search_bar_top]").type(inputValue1);
    cy.scrollTo(0, 500);
    cy.get("button[name=search_bar_collapse]").click();
    cy.get("input[name=search_bar_scrolled]").should("have.value", inputValue1).type(inputValue2);
    cy.scrollTo(0, 0);
    cy.get("input[name=search_bar_top]").should("have.value", inputValue1 + inputValue2);
  });
});

describeIf(
  "As a user, I expect the search functionality on the /companies page to adjust to the framework filters",
  {
    executionEnvironments: ["development"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    beforeEach(function () {
      cy.ensureLoggedIn("data_uploader", Cypress.env("KEYCLOAK_UPLOADER_PASSWORD"));
    });

    const companyNameMarker = "Data987654321";

    it(
      "Upload a company without uploading framework data for it and check if it does not appear in the results " +
        "even though no framework filter is set. Afterwards upload framework data and assure that it appears now.",
      () => {
        const companyName = "SomeCompany19944991" + companyNameMarker;
        createCompanyAndGetId(companyName).then((companyId) => {
          cy.visit(`/companies?input=${companyName}`)
            .get("div[class='col-12 text-left']")
            .should("contain.text", "Sorry! The company you searched for was not found in our database");
          uploadDummyEuTaxonomyDataForFinancials(companyId);
          cy.visit(`/companies?input=${companyName}`)
            .get("td[class='d-bg-white w-3 d-datatable-column-left']")
            .contains(companyName)
            .should("exist");
        });
      }
    );

    it(
      "Upload a company with Eu Taxonomy Data For Financials and check if it only appears in the results if the " +
        "framework filter is set to that framework",
      () => {
        const companyName = "CompanyWithFinancial" + companyNameMarker;
        createCompanyAndGetId(companyName).then((companyId) => uploadDummyEuTaxonomyDataForFinancials(companyId));
        cy.visit(`/companies?input=${companyName}`)
          .get("td[class='d-bg-white w-3 d-datatable-column-left']")
          .contains(companyName)
          .should("exist");
        cy.visit(`/companies?input=${companyName}&frameworks=eutaxonomy-financials`)
          .get("td[class='d-bg-white w-3 d-datatable-column-left']")
          .contains(companyName)
          .should("exist");
        cy.visit(`/companies?input=${companyName}&frameworks=eutaxonomy-non-financials`)
          .get("div[class='col-12 text-left']")
          .should("contain.text", "Sorry! The company you searched for was not found in our database");
      }
    );

    function checkFirstAutoCompleteSuggestion(companyNamePrefix: string, frameworkToFilterFor: string): void {
      cy.visit(`/companies?frameworks=${frameworkToFilterFor}`);
      cy.intercept("**/api/companies*").as("searchCompany");
      cy.get("input[name=search_bar_top]").click({ force: true }).type(companyNameMarker);
      cy.wait("@searchCompany", { timeout: 2 * 1000 }).then(() => {
        cy.get(".p-autocomplete-item")
          .eq(0)
          .get("span[class='font-normal']")
          .contains(companyNamePrefix)
          .should("exist");
      });
    }

    it(
      "Upload a company with Eu Taxonomy Data For Financials and one with Eu Taxonomy Data For Non-Financials and " +
        "check if they are displayed in the autocomplete dropdown only if the framework filter is set accordingly",
      () => {
        const companyNameFinancialPrefix = "CompanyWithFinancial";
        const companyNameFinancial = companyNameFinancialPrefix + companyNameMarker;
        createCompanyAndGetId(companyNameFinancial).then((companyId) =>
          uploadDummyEuTaxonomyDataForFinancials(companyId)
        );
        checkFirstAutoCompleteSuggestion(companyNameFinancialPrefix, "eutaxonomy-financials");

        const companyNameNonFinancialPrefix = "CompanyWithNonFinancial";
        const companyNameNonFinancial = companyNameNonFinancialPrefix + companyNameMarker;
        createCompanyAndGetId(companyNameNonFinancial).then((companyId) =>
          uploadEuTaxonomyDataForNonFinancials(companyId)
        );
        checkFirstAutoCompleteSuggestion(companyNameNonFinancialPrefix, "eutaxonomy-non-financials");
      }
    );
  }
);
