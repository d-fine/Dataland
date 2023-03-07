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
import { getRandomReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";
import { humanizeString } from "../../../../src/utils/StringHumanizer"; // TODO write path with @notation!

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
      const dropdownSelector = "div#chooseFrameworkDropdown";
      const dropdownItemsSelector = "div.p-dropdown-items-wrapper li";
      const financialsDropdownItem = "EU Taxonomy for financial companies"; // TODO could depend on humanize String and DataType class?
      const lksgDropdownItem = "LkSG"; // TODO could depend on humanize String and DataType class?

      function createAllInterceptsOnFrameworkViewPage() {
        cy.intercept("/api/companies/**-**-**").as("getCompanyInformation");
        cy.intercept("/api/metadata**").as("getMetaDataForCompanyId");
        cy.intercept("/api/data/**").as("getFrameworkData");
      }

      function waitForAllInterceptsOnFrameworkViewPage() {
        cy.wait(["@getCompanyInformation", "@getMetaDataForCompanyId", "@getFrameworkData"], {
          timeout: Cypress.env("long_timeout_in_ms") as number,
        });
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
        cy.intercept("/api/companies**").as("getSearchResults");
        cy.visit(`/companies?input=${searchStringQueryParam}&framework=${frameworkQueryParam}`);
        cy.wait("@getSearchResults", { timeout: Cypress.env("long_timeout_in_ms") as number });
        const companySelector = "span:contains(VIEW)";
        cy.get(companySelector).first().scrollIntoView();
        cy.get(companySelector).first().click({ force: true });
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
        cy.intercept("/api/companies?searchString=**true").as("autocompleteSuggestions");
        cy.get(searchBarSelector).click();
        cy.get(searchBarSelector).type(searchString, { force: true });
        cy.wait("@autocompleteSuggestions", { timeout: Cypress.env("long_timeout_in_ms") as number });
        const companySelector = ".p-autocomplete-item";
        cy.get(companySelector).first().scrollIntoView();
        cy.get(companySelector).first().click({ force: true });
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
      }

      /**
       * Opens the dropdown and selects a specific framework.
       *
       * @param frameworkToSelect The framework/item that shall be selected
       */
      function selectFrameworkInDropdown(frameworkToSelect: string): void {
        cy.get(dropdownSelector).click();
        cy.get(`${dropdownItemsSelector}:contains(${frameworkToSelect})`).click({ force: true });
      }

      /**
       * Validates if the framework view page is currently displayed.
       *
       * @param framework the framework type as DataTypeEnum
       */
      function validateFrameworkPage(framework: DataTypeEnum): void {
        cy.url().should("contain", `/frameworks/${framework}`);
        cy.get('[data-test="frameworkDataTableTitle"]').should("contain", humanizeString(framework)); // TODO
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
                singleLksgPreparedFixture.t,
                singleLksgPreparedFixture.reportingPeriod
              ).then((uploadIds) => {
                return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                  token,
                  uploadIds.companyId,
                  getRandomReportingPeriod(),
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
            return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
              token,
              storedCompany.companyId,
              getRandomReportingPeriod(),
              generateEuTaxonomyDataForNonFinancials()
            );
          });
        });
      }

      before(() => {
        uploadCompanyAndLksgDataAndEuTaxonomyFinancialsDataViaApi();
        uploadCompanyAndEuTaxonomyNonFinancialsDatasetViaApi();
      });

      it("Check that the redirect depends correctly on the applied filters and the framework select dropdown works as expected", () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        cy.intercept("/api/companies?searchString=&dataTypes=*").as("firstLoadOfSearchPage");
        cy.visit(`/companies?framework=${DataTypeEnum.EutaxonomyFinancials}`);
        cy.wait("@firstLoadOfSearchPage", { timeout: Cypress.env("long_timeout_in_ms") as number });
        createAllInterceptsOnFrameworkViewPage();
        searchCompanyViaLocalSearchBarAndSelectFirstSuggestion(lksgAndFinancialCompanyName);

        waitForAllInterceptsOnFrameworkViewPage();
        validateFrameworkPage(DataTypeEnum.EutaxonomyFinancials);
        validateDropdown(financialsDropdownItem);
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(
          DataTypeEnum.EutaxonomyFinancials,
          lksgAndFinancialCompanyName
        );

        waitForAllInterceptsOnFrameworkViewPage();
        validateFrameworkPage(DataTypeEnum.EutaxonomyFinancials);
        validateDropdown(financialsDropdownItem);
        selectFrameworkInDropdown(lksgDropdownItem);

        waitForAllInterceptsOnFrameworkViewPage();
        validateFrameworkPage(DataTypeEnum.Lksg);
        validateDropdown(lksgDropdownItem);
        selectFrameworkInDropdown(financialsDropdownItem);

        waitForAllInterceptsOnFrameworkViewPage();
        validateFrameworkPage(DataTypeEnum.EutaxonomyFinancials);
        cy.visit(`/companies?framework=${DataTypeEnum.Lksg}`);
        searchCompanyViaLocalSearchBarAndSelectFirstSuggestion(lksgAndFinancialCompanyName);

        waitForAllInterceptsOnFrameworkViewPage();
        validateFrameworkPage(DataTypeEnum.Lksg);
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(DataTypeEnum.Lksg, lksgAndFinancialCompanyName);

        waitForAllInterceptsOnFrameworkViewPage();
        validateFrameworkPage(DataTypeEnum.Lksg);
        validateDropdown(lksgDropdownItem);
      });

      it("Check that from a framework page you can search a company without this framework", () => {
        // TODO add waits
        cy.ensureLoggedIn();
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(DataTypeEnum.Lksg, lksgAndFinancialCompanyName);
        validateFrameworkPage(DataTypeEnum.Lksg);
        searchCompanyViaLocalSearchBarAndSelectFirstSuggestion(
          nonFinancialCompanyName,
          "input#framework_data_search_bar_standard"
        );
        validateFrameworkPage(DataTypeEnum.EutaxonomyNonFinancials);
      });

      it("Check if invalid company ID or invalid data ID lead to displayed error message on framework view page", () => {
        // TODO add waits
        cy.ensureLoggedIn();
        const someInvalidCompanyId = "12345-some-invalid-companyId";
        const someInvalidDataId = "789-some-invalid-dataId-987";
        cy.visit(`/companies/${someInvalidCompanyId}/frameworks/${DataTypeEnum.Lksg}`);
        cy.contains("h1", "No company with this ID present");
        getKeycloakToken().then((token: string) => {
          return getStoredCompaniesForDataType(token, DataTypeEnum.EutaxonomyFinancials).then(
            (listOfStoredCompaniesWithAtLeastOneEuTaxoFinancialsDataset) => {
              const companyIdOfSomeStoredCompany =
                listOfStoredCompaniesWithAtLeastOneEuTaxoFinancialsDataset[0].companyId;
              cy.visit(
                `/companies/${companyIdOfSomeStoredCompany}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/${someInvalidDataId}`
              );
              cy.contains(
                "h2",
                "No EU Taxonomy for financial companies data could be found for the data ID passed in the URL for this company."
              );
            }
          );
        });
      });
    }
  );
});
