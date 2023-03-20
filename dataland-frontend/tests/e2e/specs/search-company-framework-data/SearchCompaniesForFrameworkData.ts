import { getStoredCompaniesForDataType } from "@e2e//utils/GeneralApiUtils";
import { EuTaxonomyDataForNonFinancials, DataTypeEnum, StoredCompany } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { verifySearchResultTable } from "@e2e/utils/VerifyingElements";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { FixtureData } from "@sharedUtils/Fixtures";
import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import {
  getFirstEuTaxonomyNonFinancialsFixtureDataFromFixtures,
  uploadOneEuTaxonomyNonFinancialsDatasetViaApi,
} from "@e2e/utils/EuTaxonomyNonFinancialsUpload";

let companiesWithEuTaxonomyDataForNonFinancials: Array<FixtureData<EuTaxonomyDataForNonFinancials>>;

before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (jsonContent) {
    companiesWithEuTaxonomyDataForNonFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForNonFinancials>>;
  });
});

describe("As a user, I expect the search functionality on the /companies page to show me the desired results", function () {
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
    verifySearchResultTable();
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
      verifySearchResultTable();
      cy.get("input[id=search_bar_top]").type(inputValue1);
      cy.scrollTo(0, 500);
      cy.get("button[name=search_bar_collapse]").click();
      cy.get("input[id=search_bar_scrolled]").should("have.value", inputValue1).type(inputValue2);
      cy.scrollTo(0, 0);
      cy.get("input[id=search_bar_top]").should("have.value", inputValue1 + inputValue2);
    }
  );

  /**
   * Enters the given text in the search bar and hits enter verifying that the search result table matches the expected
   * format and the url includes the search term
   *
   * @param inputValue the text to enter into the search bar
   */
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
    verifySearchResultTable();
  }

  it(
    "Check PermId tooltip, execute company search by name, check result table and assure VIEW button works",
    { scrollBehavior: false },
    () => {
      /**
       * Verifies that the tooltip of the Perm ID in the search table header contains the expected text
       *
       * @param permIdTextInt the text expected in the tooltip
       */
      function checkPermIdToolTip(permIdTextInt: string): void {
        cy.get('.material-icons[title="Perm ID"]').trigger("mouseenter", "center");
        cy.get(".p-tooltip").should("be.visible").contains(permIdTextInt);
        cy.get('.material-icons[title="Perm ID"]').trigger("mouseleave");
        cy.get(".p-tooltip").should("not.exist");
      }

      /**
       * Verifies that the view button redirects to the view framework data page
       */
      function checkViewButtonWorks(): void {
        cy.get("table.p-datatable-table").contains("td", "VIEW").click().url().should("include", "/frameworks");
      }

      cy.visitAndCheckAppMount("/companies");
      verifySearchResultTable();
      const inputValue = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation.companyName;
      const permIdText = "Permanent Identifier (PermID)";
      checkPermIdToolTip(permIdText);
      executeCompanySearchWithStandardSearchBar(inputValue);
      verifySearchResultTable();
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
          cy.visitAndCheckAppMount(
            `/companies/${storedCompanies[0].companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
          );
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

  it("Search with autocompletion for companies with b in it, click and use arrow keys, find searched company in recommendation", () => {
    getKeycloakToken(uploader_name, uploader_pw).then((token) => {
      cy.browserThen(getStoredCompaniesForDataType(token, DataTypeEnum.EutaxonomyNonFinancials)).then(
        (storedCompanies: Array<StoredCompany>) => {
          const primevueHighlightedSuggestionClass = "p-focus";
          const searchString = storedCompanies[0].companyInformation.companyName;
          const searchStringResultingInAtLeastTwoAutocompleteSuggestions = "a";
          cy.visitAndCheckAppMount("/companies");
          cy.intercept("**/api/companies*").as("searchCompany");
          cy.get("input[id=search_bar_top]").type("b");
          cy.get(".p-autocomplete-item").eq(0).get("span[class='font-semibold']").contains("b").should("exist");
          cy.get(".p-autocomplete-item").contains("View all results").click();
          cy.wait("@searchCompany", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
            verifySearchResultTable();
            cy.url().should("include", "/companies?input=b");
          });
          cy.get("input[id=search_bar_top]")
            .click({ force: true })
            .type("{backspace}")
            .type(searchStringResultingInAtLeastTwoAutocompleteSuggestions);
          cy.wait("@searchCompany", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
            cy.get("ul[class=p-autocomplete-items]").should("exist");
            cy.get("input[id=search_bar_top]").type("{downArrow}");
            cy.get(".p-autocomplete-item").eq(0).should("have.class", primevueHighlightedSuggestionClass);
            cy.get(".p-autocomplete-item").eq(1).should("not.have.class", primevueHighlightedSuggestionClass);
            cy.get("input[id=search_bar_top]").type("{downArrow}");
            cy.get(".p-autocomplete-item").eq(0).should("not.have.class", primevueHighlightedSuggestionClass);
            cy.get(".p-autocomplete-item").eq(1).should("have.class", primevueHighlightedSuggestionClass);
            cy.get("input[id=search_bar_top]").type("{upArrow}");
            cy.get(".p-autocomplete-item").eq(0).should("have.class", primevueHighlightedSuggestionClass);
            cy.get(".p-autocomplete-item").eq(1).should("not.have.class", primevueHighlightedSuggestionClass);
          });
          cy.get("input[id=search_bar_top]").click({ force: true }).type("{backspace}").type(searchString);
          cy.wait("@searchCompany", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
            cy.get(".p-autocomplete-item")
              .eq(0)
              .should("contain.text", searchString)
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

  /**
   * Returns the first company from the fake fixture that has at least one alternative name
   *
   * @returns the matching company from the fake fixtures
   */
  function getCompanyWithAlternativeName(): FixtureData<EuTaxonomyDataForNonFinancials> {
    return companiesWithEuTaxonomyDataForNonFinancials.filter((it) => {
      return (
        it.companyInformation.companyAlternativeNames !== undefined &&
        it.companyInformation.companyAlternativeNames.length > 0
      );
    })[0];
  }

  it("Search for company by its alternative name", () => {
    const testCompany = getCompanyWithAlternativeName();
    const searchValue = testCompany.companyInformation.companyAlternativeNames![0];
    cy.visitAndCheckAppMount("/companies");
    executeCompanySearchWithStandardSearchBar(searchValue);
  });

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
          getFirstEuTaxonomyNonFinancialsFixtureDataFromFixtures().then((fixtureData) => {
            return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((storedCompany) => {
              return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
                token,
                storedCompany.companyId,
                fixtureData.reportingPeriod,
                fixtureData.t
              );
            });
          });
        });
        cy.visitAndCheckAppMount("/companies");
        cy.intercept("**/api/companies*").as("searchCompany");
        cy.get("input[id=search_bar_top]").click({ force: true }).type(highlightedSubString);
        cy.wait("@searchCompany", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
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
