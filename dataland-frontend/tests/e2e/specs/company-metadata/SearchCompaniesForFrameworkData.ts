import { retrieveDataIdsList } from "../../utils/ApiUtils";
import { checkViewButtonWorks, verifyTaxonomySearchResultTable } from "../../utils/CompanySearch";
import {
  CompanyInformation,
  EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForFinancials,
} from "../../../../build/clients/backend/api";

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
    cy.get("h2").should("contain", "Results");
    cy.get("table.p-datatable-table").should("exist");
  }

  it("Type smth into search bar, wait 1 sec, type enter, and expect to see search results on new page", function () {
    retrieveDataIdsList().then((dataIdList: any) => {
      cy.visitAndCheckAppMount("/companies/" + dataIdList[2] + "/frameworks/eutaxonomy");
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
    cy.get("h2").should("contain", "Results");
    cy.get("table.p-datatable-table").should("exist");
  });

  it("Check static layout of the search page", function () {
    cy.visitAndCheckAppMount("/companies");
    cy.get("h1").should("contain", "Search Data for Companies");
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
    executeCompanySearch(inputValue);
    cy.get("h1").click(); // Collapse the search autocomplete window if it exists
    verifyTaxonomySearchResultTable();
    checkPermIdToolTip(permIdText);
    checkViewButtonWorks();
    cy.get("h1").contains(inputValue);
    cy.get("[title=back_button").should("be.visible").click({ force: true });
    checkViewRowsWorks();
    cy.get("h1").contains(inputValue);
  });

  it("Company Search by Identifier", () => {
    cy.visitAndCheckAppMount("/companies");
    const inputValue = companiesWithData[1].companyInformation.identifiers[0].identifierValue;
    executeCompanySearch(inputValue);
    verifyTaxonomySearchResultTable();
    checkViewButtonWorks();
  });

  it("Search Input field should be always present", () => {
    const placeholder = "Search company by name or PermID";
    const inputValue = "A company name";
    retrieveDataIdsList().then((dataIdList: any) => {
      cy.visitAndCheckAppMount("/companies/" + dataIdList[7] + "/frameworks/eutaxonomy");
      cy.get("input[name=framework_data_search_bar_standard]")
        .should("not.be.disabled")
        .type(inputValue)
        .should("have.value", inputValue)
        .invoke("attr", "placeholder")
        .should("contain", placeholder);
    });
  });

  it("Click on an autocomplete-suggestion and check if forwarded to taxonomy data page", () => {
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

  /*
    it("Upload a company with Eu Taxonomy Data For Financials and check if it only appears in the results if the " +
        "framework filter is set to that framework", () => {

        function uploadCompanyWithEverythingFine(companyName: string) {
            cy.visitAndCheckAppMount("/companies/upload");
            fillCompanyUploadFields(companyName);
            cy.get('button[name="postCompanyData"]').click();
        }

        const companyName = "CompanyWithFinancialData123456XYZXYZ";
        uploadCompanyWithEverythingFine(companyName);
        cy.get("body").should("contain", "success");
        cy.get("span[title=companyId]").then(($companyID) => {
            companyId = $companyID.text();
            cy.visitAndCheckAppMount(`/companies/${companyId}`);
            cy.get("body").should("contain", companyName);
            cy.visit(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`)
            // fill everything
            //upload and check if it worked
            cy.visit(`/companies?input=${companyName}frameworks=EuTaxonomyDataForFinancials`)


            // check if only search result is the company that has just been uploaded
            cy.visit(`/companies?input=${companyName}frameworks=EuTaxonomyDataForNonFinancials`)
            // assure that no results are shown
            cy.visit(`/companies?input=${companyName}`)
            // check if only search result is the company that has just been uploaded

        });
    )


    it("Upload a company with Eu Taxonomy Data For Financials and one with Eu Taxonomy Data For Non-Financials and " +
     "check if they are displayed in the autcomplete dropdown only if the framework filter is set accordingly", () => {

        function uploadCompanyWithEverythingFine(companyName: string) {
            cy.visitAndCheckAppMount("/companies/upload");
            fillCompanyUploadFields(companyName);
            cy.get('button[name="postCompanyData"]').click();
        }

        const companyNameFinancial = "CompanyWithFinancialData987654321";
        const companyNameNonFinancial = "CompanyWithNonFinancialData987654321";
        //for both companies do =>
        uploadCompanyWithEverythingFine(companyName);
        cy.get("body").should("contain", "success");
        cy.get("span[title=companyId]").then(($companyID) => {
            companyId = $companyID.text();
            cy.visitAndCheckAppMount(`/companies/${companyId}`);
            cy.get("body").should("contain", companyName);
            cy.visit(`/companies/${companyId}/frameworks/eutaxonomy-financials/upload`)
            // fill everything
            //upload and check if it worked
            cy.visit(`/companies?input=${companyName}frameworks=EuTaxonomyDataForFinancials`)
        //THEN
            // check if only financial company appears in autocomplete
            cy.visit(`/companies?frameworks=EuTaxonomyDataForFinancials`)
              => input "Data987654321"
              => assert that only companyNameFinancial appears in the dropdown (or multiple instances of it since test may be already run several times)
            cy.visit(`/companies?frameworks=EuTaxonomyDataForNonFinancials`)
              => input "Data987654321"
              => assert that only companyNameNonFinancial appears in the dropdown (or multiple instances of it since test may be already run several times)


        });
    )

     */
});
