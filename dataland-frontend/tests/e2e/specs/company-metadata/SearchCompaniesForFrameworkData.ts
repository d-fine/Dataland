import { getStoredCompaniesForDataType } from "@e2e//utils/GeneralApiUtils";
import { EuTaxonomyDataForNonFinancials, DataTypeEnum, StoredCompany } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { verifyTaxonomySearchResultTable } from "@e2e/utils/VerifyingElements";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import {
  getFirstEuTaxonomyNonFinancialsDatasetFromFixtures,
  uploadOneEuTaxonomyNonFinancialsDatasetViaApi,
} from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { SHORT_TIMEOUT_IN_MS } from "../../../../src/utils/Constants";

let companiesWithEuTaxonomyDataForNonFinancials: Array<FixtureData<EuTaxonomyDataForNonFinancials>>;

before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (jsonContent) {
    companiesWithEuTaxonomyDataForNonFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForNonFinancials>>;
  });
});

describe("As a user, I expect the search functionality on the /companies page to behave as I expect", function () {
  beforeEach(function () {
    cy.ensureLoggedIn();
  });

  it("Check static layout of the search page", function () {
    cy.visitAndCheckAppMount("/companies");
    const placeholder = "Search company by name or PermID";
    const inputValue = "A company name";
    cy.get("input[id=search_bar_top]")
      .should("not.be.disabled")
      .type(inputValue)
      .should("have.value", inputValue)
      .invoke("attr", "placeholder")
      .should("contain", placeholder);
  });

  it("Scroll the page and check if search icon and search bar behave as expected", { scrollBehavior: false }, () => {
    cy.visitAndCheckAppMount("/companies");
    verifyTaxonomySearchResultTable();
    cy.get("button[name=search_bar_collapse]").should("not.be.visible");

    cy.scrollTo(0, 500, { duration: 300 });
    cy.get("input[id=search_bar_top]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("be.visible");

    cy.scrollTo(0, 0, { duration: 300 });
    cy.get("input[id=search_bar_top]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("not.be.visible");

    cy.scrollTo(0, 500, { duration: 300 });
    cy.get("button[name=search_bar_collapse]").should("exist").click();
    cy.get("input[id=search_bar_top]").should("not.exist");
    cy.get("input[id=search_bar_scrolled]").should("exist");
    cy.get("button[name=search_bar_collapse]").should("not.be.visible");

    cy.scrollTo(0, 480, { duration: 300 });
    cy.get("button[name=search_bar_collapse]").should("be.visible");
    cy.get("input[id=search_bar_top]").should("exist");
    cy.get("input[id=search_bar_scrolled]").should("not.exist");
  });

  it(
    "Scroll the page to type into the search bar in different states and check if the input is always saved",
    { scrollBehavior: false },
    () => {
      const inputValue1 = "ABCDEFG";
      const inputValue2 = "XYZ";
      cy.visitAndCheckAppMount("/companies");
      verifyTaxonomySearchResultTable();
      cy.get("input[id=search_bar_top]").type(inputValue1);
      cy.scrollTo(0, 500);
      cy.get("button[name=search_bar_collapse]").click();
      cy.get("input[id=search_bar_scrolled]").should("have.value", inputValue1).type(inputValue2);
      cy.scrollTo(0, 0);
      cy.get("input[id=search_bar_top]").should("have.value", inputValue1 + inputValue2);
    }
  );

  it(
    "Type b into the search bar, click on ViewAllResults, and check if all results for b are displayed",
    { scrollBehavior: false },
    () => {
      cy.visitAndCheckAppMount("/companies");
      cy.intercept("**/api/companies*").as("searchCompany");
      cy.get("input[id=search_bar_top]").type("b");
      cy.get(".p-autocomplete-item").contains("View all results").click();
      cy.wait("@searchCompany", { timeout: SHORT_TIMEOUT_IN_MS }).then(() => {
        verifyTaxonomySearchResultTable();
        cy.url().should("include", "/companies?input=b");
      });
    }
  );

  function executeCompanySearchWithStandardSearchBar(inputValue: string): void {
    const inputValueUntilFirstSpace = inputValue.substring(0, inputValue.indexOf(" "));
    cy.get("input[id=search_bar_top]")
      .should("not.be.disabled")
      .click({ force: true })
      .type(inputValue)
      .should("have.value", inputValue)
      .type("{enter}")
      .should("have.value", inputValue);
    cy.url({ decode: true }).should("include", "/companies?input=" + inputValueUntilFirstSpace);
    verifyTaxonomySearchResultTable();
  }

  it(
    "Check PermId tooltip, execute company search by name, check result table and assure VIEW button works",
    { scrollBehavior: false },
    () => {
      function checkPermIdToolTip(permIdTextInt: string): void {
        cy.get('.material-icons[title="Perm ID"]').trigger("mouseenter", "center");
        cy.get(".p-tooltip").should("be.visible").contains(permIdTextInt);
        cy.get('.material-icons[title="Perm ID"]').trigger("mouseleave");
        cy.get(".p-tooltip").should("not.exist");
      }

      function checkViewButtonWorks(): void {
        cy.get("table.p-datatable-table")
          .contains("td", "VIEW")
          .contains("a", "VIEW")
          .click()
          .url()
          .should("include", "/frameworks");
      }

      cy.visitAndCheckAppMount("/companies");
      verifyTaxonomySearchResultTable();
      const inputValue = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation.companyName;
      const permIdText = "Permanent Identifier (PermID)";
      checkPermIdToolTip(permIdText);
      executeCompanySearchWithStandardSearchBar(inputValue);
      verifyTaxonomySearchResultTable();
      checkViewButtonWorks();
      cy.get("h1").contains(inputValue);
      cy.get("[title=back_button").should("be.visible").click({ force: true });
      cy.get("input[id=search_bar_top]").should("contain.value", inputValue);
      checkViewButtonWorks();
      cy.get("h1").contains(inputValue);
    }
  );

  it("Execute a company Search by identifier and assure that the company is found", () => {
    cy.visitAndCheckAppMount("/companies");
    const inputValue = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation.identifiers[0].identifierValue;
    const expectedCompanyName = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation.companyName;
    executeCompanySearchWithStandardSearchBar(inputValue);
    cy.get("td[class='d-bg-white w-3 d-datatable-column-left']").contains(expectedCompanyName);
  });

  it("Visit framework data view page and assure that title is present and a Framework Data Search Bar exists", () => {
    const placeholder = "Search company by name or PermID";
    const inputValue = "A company name";

    getKeycloakToken(uploader_name, uploader_pw).then((token) => {
      cy.browserThen(getStoredCompaniesForDataType(token, DataTypeEnum.EutaxonomyNonFinancials)).then(
        (storedCompanies: Array<StoredCompany>) => {
          cy.visitAndCheckAppMount(`/companies/${storedCompanies[0].companyId}/frameworks/eutaxonomy-non-financials`);
          cy.get("input[id=framework_data_search_bar_standard]")
            .should("not.be.disabled")
            .type(inputValue)
            .should("have.value", inputValue)
            .invoke("attr", "placeholder")
            .should("contain", placeholder);
        }
      );
    });
  });

  it("Click on an autocomplete-suggestion and check if forwarded to company framework data view page", () => {
    getKeycloakToken(uploader_name, uploader_pw).then((token) => {
      cy.browserThen(getStoredCompaniesForDataType(token, DataTypeEnum.EutaxonomyNonFinancials)).then(
        (storedCompanies: Array<StoredCompany>) => {
          const searchString = storedCompanies[0].companyInformation.companyName.substring(0, 4);
          cy.visitAndCheckAppMount("/companies");
          cy.intercept("**/api/companies*").as("searchCompany");
          cy.get("input[id=search_bar_top]").click({ force: true }).type(searchString);
          cy.wait("@searchCompany", { timeout: SHORT_TIMEOUT_IN_MS }).then(() => {
            cy.get(".p-autocomplete-item")
              .eq(0)
              .click({ force: true })
              .url()
              .should("include", "/companies/")
              .url()
              .should("include", "/frameworks/eutaxonomy");
          });
        }
      );
    });
  });

  it(
    "Navigate with arrow keys, press enter on an autocomplete-suggestion and check if forwarded to company framework data view page",
    { scrollBehavior: false },
    () => {
      const primevueHighlightedSuggestionClass = "p-focus";
      const searchStringResultingInAtLeastTwoAutocompleteSuggestions = "a";
      cy.visitAndCheckAppMount("/companies");
      verifyTaxonomySearchResultTable();
      cy.intercept("**/api/companies*").as("searchCompany");
      cy.get("input[id=search_bar_top]")
        .click({ force: true })
        .type(searchStringResultingInAtLeastTwoAutocompleteSuggestions);
      cy.wait("@searchCompany", { timeout: SHORT_TIMEOUT_IN_MS }).then(() => {
        cy.get("ul[class=p-autocomplete-items]");
        cy.get("input[id=search_bar_top]").type("{downArrow}");
        cy.get(".p-autocomplete-item").eq(0).should("have.class", primevueHighlightedSuggestionClass);
        cy.get(".p-autocomplete-item").eq(1).should("not.have.class", primevueHighlightedSuggestionClass);
        cy.get("input[id=search_bar_top]").type("{downArrow}");
        cy.get(".p-autocomplete-item").eq(0).should("not.have.class", primevueHighlightedSuggestionClass);
        cy.get(".p-autocomplete-item").eq(1).should("have.class", primevueHighlightedSuggestionClass);
        cy.get("input[id=search_bar_top]").type("{upArrow}");
        cy.get(".p-autocomplete-item").eq(0).should("have.class", primevueHighlightedSuggestionClass);
        cy.get(".p-autocomplete-item").eq(1).should("not.have.class", primevueHighlightedSuggestionClass);
        cy.get("input[id=search_bar_top]")
          .type("{enter}")
          .url()
          .should("include", "/companies/")
          .url()
          .should("include", "/frameworks/");
      });
    }
  );

  describeIf(
    "As a user, I expect substrings of the autocomplete suggestions to be highlighted if they match my search string",
    {
      executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      dataEnvironments: ["fakeFixtures"],
    },
    () => {
      it("Check if substrings of autocomplete entries are highlighted", { scrollBehavior: false }, () => {
        cy.ensureLoggedIn();
        const highlightedSubString = "this_is_highlighted";
        const companyName = "ABCDEFG" + highlightedSubString + "HIJKLMNOP";
        getKeycloakToken(uploader_name, uploader_pw).then((token) => {
          getFirstEuTaxonomyNonFinancialsDatasetFromFixtures().then((data) => {
            return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((storedCompany) => {
              return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(token, storedCompany.companyId, data);
            });
          });
        });
        cy.visitAndCheckAppMount("/companies");
        cy.intercept("**/api/companies*").as("searchCompany");
        cy.get("input[id=search_bar_top]").click({ force: true }).type(highlightedSubString);
        cy.wait("@searchCompany", { timeout: SHORT_TIMEOUT_IN_MS }).then(() => {
          cy.get(".p-autocomplete-item")
            .eq(0)
            .get("span[class='font-semibold']")
            .contains(highlightedSubString)
            .should("exist");
        });
      });
    }
  );
});
