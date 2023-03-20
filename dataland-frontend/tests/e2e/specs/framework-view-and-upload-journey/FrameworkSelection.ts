import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { DataTypeEnum, LksgData } from "@clients/backend";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { generateEuTaxonomyDataForFinancials } from "@e2e/fixtures/eutaxonomy/financials/EuTaxonomyDataForFinancialsFixtures";
import { uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgUpload";
import { getPreparedFixture, getStoredCompaniesForDataType } from "@e2e/utils/GeneralApiUtils";
import { uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { generateCompanyInformation } from "@e2e/fixtures/CompanyFixtures";
import { uploadOneEuTaxonomyNonFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { generateEuTaxonomyDataForNonFinancials } from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsFixtures";

describe("The shared header of the framework pages should act as expected", { scrollBehavior: false }, () => {
  describeIf(
    "As a user, I expect the framework selection dropdown to work correctly " +
      "to make it possible to switch between framework views",
    {
      executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      dataEnvironments: ["fakeFixtures"],
    },
    function (): void {
      const lksgAndFinancialCompanyName = "two-different-data-set-types";
      const nonFinancialCompanyName = "some-non-financial-only-company";
      const dropdownSelector = "div#frameworkDataDropdown";
      const dropdownItemsSelector = "div.p-dropdown-items-wrapper li";
      const financialsDropdownItem = "EU Taxonomy for financial companies";
      const lksgDropdownItem = "LkSG";

      /**
       * Visits the search page with framework and company name query params set, and clicks on the first VIEW selector
       * in the search results table.
       *
       * @param frameworkQueryParam The query param set as framework filter
       * @param searchStringQueryParam The query param set as search string
       */
      function visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(
        frameworkQueryParam: string,
        searchStringQueryParam: string
      ): void {
        cy.intercept(`/companies?input=${searchStringQueryParam}&framework=${frameworkQueryParam}`).as("companyLoad");
        cy.visit(`/companies?input=${searchStringQueryParam}&framework=${frameworkQueryParam}`);
        cy.wait("@companyLoad", { timeout: Cypress.env("long_timeout_in_ms") as number });
        cy.intercept("**/api/companies/*").as("searchCompany");
        const companySelector = "span:contains(VIEW)";
        cy.get(companySelector).first().scrollIntoView();
        cy.get(companySelector).first().click({ force: true });
        cy.wait("@searchCompany", { timeout: Cypress.env("long_timeout_in_ms") as number });
      }

      /**
       * Visits the search page with the framework query param set, type a search string into the search bar and click
       * the first suggestion.
       *
       * @param frameworkQueryParam The query param set as framework filter
       * @param searchStringForSearchBar The search string to type into the search bar
       */
      function selectCompanyViaAutocompleteOnCompaniesPage(
        frameworkQueryParam: string,
        searchStringForSearchBar: string
      ): void {
        cy.intercept("**/api/companies/**").as("getDataRequest");
        cy.visit(`/companies?framework=${frameworkQueryParam}`);
        cy.wait("@getDataRequest", { timeout: Cypress.env("short_timeout_in_ms") as number });
        searchCompanyViaLocalSearchBarAndSelectFirstSuggestion(searchStringForSearchBar);
      }

      /**
       * Visits the search page with the framework query param set, type a search string into the search bar and click
       * the first suggestion.
       *
       * @param searchString The search string to type into the search bar
       * @param searchBarSelector The selector to select the correct search bar from the DOM
       */
      function searchCompanyViaLocalSearchBarAndSelectFirstSuggestion(
        searchString: string,
        searchBarSelector = "input#search_bar_top"
      ): void {
        cy.get(searchBarSelector).click();
        cy.get(searchBarSelector).type(searchString, { force: true });
        cy.intercept("**/api/companies/*").as("searchCompany");
        const companySelector = ".p-autocomplete-item";
        cy.get(companySelector).first().scrollIntoView();
        cy.get(companySelector).first().click({ force: true });
        cy.wait("@searchCompany", { timeout: Cypress.env("long_timeout_in_ms") as number });
      }

      /**
       * Validates if the dropdown label and the items in the dropdown equal the expected values.
       *
       * @param expectedDropdownLabel The expected label of the dropdown
       */
      function validateDropdown(expectedDropdownLabel: string): void {
        cy.get("h2:contains('Checking if')").should("not.exist");
        cy.get(dropdownSelector).find(".p-dropdown-label").should("have.text", expectedDropdownLabel);
        cy.get(dropdownSelector).click();
        const expectedDropdownItems = new Set<string>([financialsDropdownItem, lksgDropdownItem]);
        cy.get(dropdownItemsSelector)
          .each((item) => {
            expect(expectedDropdownItems.has(item.text())).to.equal(true);
            expectedDropdownItems.delete(item.text());
          })
          .then(() => {
            expect(expectedDropdownItems.size).to.equal(0);
          });
        cy.get(dropdownSelector).click();
      }

      /**
       * Opens the dropdown and selects a specific framework.
       *
       * @param frameworkToSelect The framework/item that shall be selected
       */
      function selectFrameworkInDropdown(frameworkToSelect: string): void {
        cy.intercept("**/api/companies/*").as("selectFramework");
        cy.get(dropdownSelector).click();
        cy.get(`${dropdownItemsSelector}:contains(${frameworkToSelect})`).click({ force: true });
        cy.wait("@selectFramework", { timeout: Cypress.env("long_timeout_in_ms") as number });
      }

      /**
       * Validates if the framework view page is currently displayed.
       *
       * @param framework the framework type as DataTypeEnum
       * @param header the h2 header to check for
       */
      function validateFrameworkPage(framework: DataTypeEnum, header: string): void {
        cy.url().should("contain", `/frameworks/${framework}`);
        cy.get("h2").should("contain", header);
      }

      /**
       * Uploads a specific company and LkSG dataset from the prepared fixtures, then EU Taxonomy data for financial
       * companies for that company.
       */
      function uploadCompanyAndLksgDataAndEuTaxonomyFinancialsDataViaApi(): void {
        let lksgPreparedFixtures: Array<FixtureData<LksgData>>;
        cy.fixture("CompanyInformationWithLksgPreparedFixtures")
          .then(function (jsonContent) {
            lksgPreparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
          })
          .then(() => {
            const singleLksgPreparedFixture = getPreparedFixture(lksgAndFinancialCompanyName, lksgPreparedFixtures);
            return getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
              return uploadCompanyAndLksgDataViaApi(
                token,
                singleLksgPreparedFixture.companyInformation,
                singleLksgPreparedFixture.t
              ).then((uploadIds) => {
                return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                  token,
                  uploadIds.companyId,
                  generateEuTaxonomyDataForFinancials()
                );
              });
            });
          });
      }

      /**
       * Uploads a company with a specific name and an EU Taxonomy dataset for financial companies for that company.
       */
      function uploadCompanyAndEuTaxonomyNonFinancialsDatasetViaApi(): void {
        getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
          const companyInformation = generateCompanyInformation();
          companyInformation.companyName = nonFinancialCompanyName;
          return uploadCompanyViaApi(token, companyInformation).then((storedCompany) => {
            const nonFinancialDataSet = generateEuTaxonomyDataForNonFinancials();
            return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(token, storedCompany.companyId, nonFinancialDataSet);
          });
        });
      }

      before(() => {
        uploadCompanyAndLksgDataAndEuTaxonomyFinancialsDataViaApi();
        uploadCompanyAndEuTaxonomyNonFinancialsDatasetViaApi();
      });

      it("Check that the redirect depends correctly on the applied filters and the framework select dropdown works as expected", () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        selectCompanyViaAutocompleteOnCompaniesPage(DataTypeEnum.EutaxonomyFinancials, lksgAndFinancialCompanyName);
        validateFrameworkPage(DataTypeEnum.EutaxonomyFinancials, "EU Taxonomy Data");
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(
          DataTypeEnum.EutaxonomyFinancials,
          lksgAndFinancialCompanyName
        );
        validateFrameworkPage(DataTypeEnum.EutaxonomyFinancials, "EU Taxonomy Data");
        validateDropdown(financialsDropdownItem);
        selectFrameworkInDropdown(lksgDropdownItem);
        validateFrameworkPage(DataTypeEnum.Lksg, "LkSG Data");
        validateDropdown(lksgDropdownItem);
        selectFrameworkInDropdown(financialsDropdownItem);
        validateFrameworkPage(DataTypeEnum.EutaxonomyFinancials, "EU Taxonomy Data");

        selectCompanyViaAutocompleteOnCompaniesPage(DataTypeEnum.Lksg, lksgAndFinancialCompanyName);
        validateFrameworkPage(DataTypeEnum.Lksg, "LkSG Data");
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(DataTypeEnum.Lksg, lksgAndFinancialCompanyName);
        validateFrameworkPage(DataTypeEnum.Lksg, "LkSG Data");
        validateDropdown(lksgDropdownItem);
      });

      it("Check that from a framework page you can search a company without this framework", () => {
        cy.ensureLoggedIn();
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(DataTypeEnum.Lksg, lksgAndFinancialCompanyName);
        validateFrameworkPage(DataTypeEnum.Lksg, "LkSG Data");
        searchCompanyViaLocalSearchBarAndSelectFirstSuggestion(
          nonFinancialCompanyName,
          "input#framework_data_search_bar_standard"
        );
        validateFrameworkPage(DataTypeEnum.EutaxonomyNonFinancials, "EU Taxonomy Data");
      });

      it("Check if invalid company ID or invalid data ID lead to displayed error message on framework view page", () => {
        cy.ensureLoggedIn();
        const someInvalidCompanyId = "12345-some-invalid-companyId";
        const someInvalidDataId = "789-some-invalid-dataId-987";
        cy.intercept(`**/api/companies/${someInvalidCompanyId}/frameworks/${DataTypeEnum.Lksg}`).as("searchCompany");
        cy.visit(`/companies/${someInvalidCompanyId}/frameworks/${DataTypeEnum.Lksg}`);
        cy.wait("@searchCompany", { timeout: Cypress.env("medium_timeout_in_ms") as number });
        cy.contains("h1", "No company with this ID present");
        getKeycloakToken().then((token: string) => {
          return getStoredCompaniesForDataType(token, DataTypeEnum.EutaxonomyFinancials).then(
            (listOfStoredCompaniesWithAtLeastOneEuTaxoFinancialsDataset) => {
              const companyIdOfSomeStoredCompany =
                listOfStoredCompaniesWithAtLeastOneEuTaxoFinancialsDataset[0].companyId;
              cy.visit(
                `/companies/${companyIdOfSomeStoredCompany}/frameworks/${DataTypeEnum.EutaxonomyFinancials}?dataId=${someInvalidDataId}`
              );
              cy.contains(
                "h2",
                "There is no EU-Taxonomy data for financial companies available for the data ID you provided in the URL."
              );
            }
          );
        });
      });
    }
  );
});
