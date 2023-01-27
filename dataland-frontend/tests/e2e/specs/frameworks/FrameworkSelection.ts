import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { LksgData } from "@clients/backend";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { generateEuTaxonomyDataForFinancials } from "@e2e/fixtures/eutaxonomy/financials/EuTaxonomyDataForFinancialsFixtures";
import { uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgUpload";
import { getPreparedFixture } from "@e2e/utils/GeneralApiUtils";
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
       * Wraps an interception with a wait-statement around a function that sends a request to the metadata-endpoint
       * of the backend.
       *
       * @param trigger The function which sends a request to the metadata-endpoint of the backend
       */
      function interceptAndWaitForMetaDataRequest(trigger: () => void): void {
        const metaDataAlias = "retrieveMetaData";
        cy.intercept("**/api/metadata**").as(metaDataAlias);
        trigger();
        cy.wait(`@${metaDataAlias}`, { timeout: Cypress.env("medium_timeout_in_ms") as number });
        cy.wait(3000);
      }

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
        cy.visit(`/companies?input=${searchStringQueryParam}&framework=${frameworkQueryParam}`);
        interceptAndWaitForMetaDataRequest(() => {
          const companySelector = "a span:contains( VIEW)";
          cy.get(companySelector).first().scrollIntoView();
          cy.get(companySelector).first().click({ force: true });
        });
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
        cy.visit(`/companies?framework=${frameworkQueryParam}`);
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
        interceptAndWaitForMetaDataRequest(() => {
          const companySelector = ".p-autocomplete-item";
          cy.get(companySelector).first().scrollIntoView();
          cy.get(companySelector).first().click({ force: true });
        });
      }

      /**
       * Validates if the dropdown label and the items in the dropdown equal the expected values.
       *
       * @param expectedDropdownLabel The expected label of the dropdown
       */
      function validateDropdown(expectedDropdownLabel: string): void {
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
      }

      /**
       * Opens the dropdown and selects a specific framework.
       *
       * @param frameworkToSelect The framework/item that shall be selected
       */
      function selectFrameworkInDropdown(frameworkToSelect: string): void {
        cy.get(dropdownSelector).click();
        interceptAndWaitForMetaDataRequest(() => {
          cy.get(`${dropdownItemsSelector}:contains(${frameworkToSelect})`).click({ force: true });
        });
      }

      /**
       * Validates if the framework view page for EU Taxonomy data for financial companies is currently displayed.
       */
      function validateFinancialsPage(): void {
        cy.url().should("contain", `/frameworks/eutaxonomy-financials`);
        cy.get("h2").should("contain", "EU Taxonomy Data");
      }

      /**
       * Validates if the framework view page for EU Taxonomy data for non-financial companies is currently displayed.
       */
      function validateNonFinancialsPage(): void {
        cy.url().should("contain", `/frameworks/eutaxonomy-non-financials`);
        cy.get("h2").should("contain", "EU Taxonomy Data");
      }

      /**
       * Validates if the framework view page for LkSG data is currently displayed.
       */
      function validateLksgPage(): void {
        cy.url().should("contain", `/frameworks/lksg`);
        cy.get("h2").should("contain", "LkSG data");
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
        selectCompanyViaAutocompleteOnCompaniesPage("eutaxonomy-financials", lksgAndFinancialCompanyName);
        validateFinancialsPage();
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult("eutaxonomy-financials", lksgAndFinancialCompanyName);
        validateFinancialsPage();
        validateDropdown(financialsDropdownItem);
        selectFrameworkInDropdown(lksgDropdownItem);
        validateLksgPage();
        validateDropdown(lksgDropdownItem);
        selectFrameworkInDropdown(financialsDropdownItem);
        validateFinancialsPage();

        selectCompanyViaAutocompleteOnCompaniesPage("lksg", lksgAndFinancialCompanyName);
        validateLksgPage();
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult("lksg", lksgAndFinancialCompanyName);
        validateLksgPage();
        validateDropdown(lksgDropdownItem);
      });

      it("Check that from a framework page you can search a company without this framework", () => {
        cy.ensureLoggedIn();
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult("lksg", lksgAndFinancialCompanyName);
        validateLksgPage();
        searchCompanyViaLocalSearchBarAndSelectFirstSuggestion(
          nonFinancialCompanyName,
          "input#framework_data_search_bar_standard"
        );
        validateNonFinancialsPage();
      });
    }
  );
});
