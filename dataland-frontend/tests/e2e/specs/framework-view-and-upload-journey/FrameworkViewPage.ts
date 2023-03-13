import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { DataTypeEnum, EuTaxonomyDataForFinancials, LksgData } from "@clients/backend";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadOneLksgDatasetViaApi } from "@e2e/utils/LksgUpload";
import { getPreparedFixture } from "@e2e/utils/GeneralApiUtils";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadOneEuTaxonomyNonFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { generateEuTaxonomyDataForNonFinancials } from "@e2e/fixtures/eutaxonomy/non-financials/EuTaxonomyDataForNonFinancialsFixtures";
import { humanizeString } from "@/utils/StringHumanizer";
import { uploadOneSfdrDataset } from "@e2e/utils/SfdrUpload";
import { generateSfdrData } from "@e2e/fixtures/sfdr/SfdrDataFixtures";
import { generateLksgData } from "@e2e/fixtures/lksg/LksgDataFixtures";

describe("The shared header of the framework pages should act as expected", { scrollBehavior: false }, () => {
  describeIf(
    "As a user, I expect the framework selection dropdown to work correctly " +
      "to make it possible to switch between framework views",
    {
      executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      dataEnvironments: ["fakeFixtures"],
    },
    function (): void {
      const nameOfCompanyAlpha = "company-alpha-with-four-different-framework-types";
      const expectedFrameworkDropdownItemsForAlpha = new Set<string>([
        humanizeString(DataTypeEnum.EutaxonomyFinancials),
        humanizeString(DataTypeEnum.EutaxonomyNonFinancials),
        humanizeString(DataTypeEnum.Lksg),
        humanizeString(DataTypeEnum.Sfdr),
      ]);
      const expectedReportingPeriodsForEuTaxoFinancialsForAlpha = new Set<string>(["2019", "2016"]);
      const expectedReportingPeriodsForEuTaxoNonFinancialsForAlpha = new Set<string>(["2015"]);
      const expectedReportingPeriodsForLksgForAlpha = new Set<string>(["2023", "2022"]);
      let companyIdOfAlpha: string;

      const nameOfCompanyBeta = "company-beta-with-eutaxo-and-lksg-data";
      const expectedFrameworkDropdownItemsForBeta = new Set<string>([
        humanizeString(DataTypeEnum.EutaxonomyNonFinancials),
        humanizeString(DataTypeEnum.Lksg),
      ]);
      const expectedReportingPeriodsForEuTaxoFinancialsForBeta = new Set<string>(["2014"]);

      const frameworkDropdownSelector = "div#chooseFrameworkDropdown";
      const reportingPeriodDropdownSelector = "div#chooseReportingPeriodDropdown";
      const dropdownItemsSelector = "div.p-dropdown-items-wrapper li";

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
        cy.intercept("/api/companies?searchString=**false").as("getSearchResults");
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
       * @param expectedChosenFramework The expected label of the dropdown
       * @param expectedDropdownOptions
       */
      function validateChosenFramework(expectedChosenFramework: string): void {
        validateViewPage(expectedChosenFramework);
        cy.get("h2:contains('Checking if')").should("not.exist");
        cy.get(frameworkDropdownSelector)
          .find(".p-dropdown-label")
          .should("have.text", humanizeString(expectedChosenFramework));
      }

      function validateChosenReportingPeriod(expectedChosenReportingPeriod: string, skipUrlCheck = false) {
        if (!skipUrlCheck) {
          cy.url().should("contain", `/reportingPeriods/${expectedChosenReportingPeriod}`);
        }
        cy.get("h2:contains('Checking if')").should("not.exist");
        cy.get(reportingPeriodDropdownSelector)
          .find(".p-dropdown-label")
          .should("have.text", expectedChosenReportingPeriod);
      }

      function validateDropdownOptions(dropdownSelector: string, expectedDropdownOptions: Set<string>) {
        cy.get(dropdownSelector).click();
        let optionsCounter = 0;
        cy.get(dropdownItemsSelector)
          .each((item) => {
            expect(expectedDropdownOptions.has(item.text())).to.equal(true);
            optionsCounter++;
          })
          .then(() => {
            expect(expectedDropdownOptions.size).to.equal(optionsCounter);
          });
        cy.get(dropdownSelector).click();
      }

      function validateEligibleActivityValueForFinancialsDataset(expectedEligibleActiviyValue: string) {
        cy.get(`div:contains("Taxonomy-eligible economic activity")`)
          .siblings(`div:contains(${expectedEligibleActiviyValue})`)
          .should("exist");
      }

      function validateOneColumnPerExpectedReportingPeriod(expectedReportingPeriods: Set<string>) {
        expectedReportingPeriods.forEach((singleReportingPeriod) => {
          cy.get(`span.p-column-title:contains(${singleReportingPeriod})`).should("have.length", 1);
        });
      }

      function validateNoErrorMessagesAreShown() {
        getElementAndAssertExistence("noDataForThisFrameworkPresentErrorIndicator", "not.exist");
        getElementAndAssertExistence("noDataForThisDataIdPresentErrorIndicator", "not.exist");
        getElementAndAssertExistence("noDataForThisReportingPeriodPresentErrorIndicator", "not.exist");
      }

      function getElementAndAssertExistence(dataTestSelector: string, shouldTag: string): void {
        cy.get(`[data-test=${dataTestSelector}]`).should(shouldTag);
      }

      /**
       * Opens the dropdown and selects a specific framework.
       *
       * @param frameworkToSelect The framework/item that shall be selected
       */
      function selectFrameworkInDropdown(frameworkToSelect: string): void {
        cy.get(frameworkDropdownSelector).click();
        cy.get(`${dropdownItemsSelector}:contains(${humanizeString(frameworkToSelect)})`).click({ force: true });
      }

      function selectReportingPeriodInDropdown(reportingPeriodToSelect: string): void {
        cy.get(reportingPeriodDropdownSelector).click();
        cy.get(`${dropdownItemsSelector}:contains(${humanizeString(reportingPeriodToSelect)})`).click({ force: true });
      }

      /**
       * Validates if the framework view page is currently displayed.
       *
       * @param framework the framework type as DataTypeEnum
       */
      function validateViewPage(framework: string): void {
        cy.url().should("contain", `/frameworks/${framework}`);
        cy.get('[data-test="frameworkDataTableTitle"]').should("contain", humanizeString(framework));
      }

      function clickBackButton(): void {
        cy.get('[data-test="backButton"]').click();
      }

      /**
       * Uploads a specific company and LkSG dataset from the prepared fixtures, then EU Taxonomy data for financial
       * companies for that company.
       */
      function uploadCompanyAlphaAndData(): void {
        const timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps = 2000;
        getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(nameOfCompanyAlpha))
            .then((storedCompany) => {
              companyIdOfAlpha = storedCompany.companyId;
              return uploadOneLksgDatasetViaApi(
                token,
                companyIdOfAlpha,
                "2023",
                getPreparedFixture("vat-2023-1", lksgPreparedFixtures).t
              );
            })
            .then(() => {
              return cy.wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps).then(() => {
                return uploadOneLksgDatasetViaApi(
                  token,
                  companyIdOfAlpha,
                  "2023",
                  getPreparedFixture("vat-2023-2", lksgPreparedFixtures).t
                );
              });
            })
            .then(() => {
              return cy.wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps).then(() => {
                return uploadOneLksgDatasetViaApi(
                  token,
                  companyIdOfAlpha,
                  "2022",
                  getPreparedFixture("vat-2022", lksgPreparedFixtures).t
                );
              });
            })
            .then(() => {
              return uploadOneSfdrDataset(token, companyIdOfAlpha, "2019", generateSfdrData());
            })
            .then(() => {
              return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                token,
                companyIdOfAlpha,
                "2019",
                getPreparedFixture("eligible-activity-Point-29", euTaxoFinancialPreparedFixtures).t
              );
            })
            .then(() => {
              return cy.wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps).then(() => {
                return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                  token,
                  companyIdOfAlpha,
                  "2019",
                  getPreparedFixture("eligible-activity-Point-292", euTaxoFinancialPreparedFixtures).t
                );
              });
            })
            .then(() => {
              return cy.wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps).then(() => {
                return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                  token,
                  companyIdOfAlpha,
                  "2016",
                  getPreparedFixture("eligible-activity-Point-26", euTaxoFinancialPreparedFixtures).t
                );
              });
            })
            .then(() => {
              return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
                token,
                companyIdOfAlpha,
                "2015",
                generateEuTaxonomyDataForNonFinancials()
              );
            });
        });
      }

      function uploadCompanyBetaAndData(): void {
        let companyId: string;
        getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(nameOfCompanyBeta))
            .then((storedCompany) => {
              companyId = storedCompany.companyId;
              return uploadOneLksgDatasetViaApi(token, companyId, "2015", generateLksgData());
            })
            .then(() => {
              return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
                token,
                companyId,
                "2014",
                generateEuTaxonomyDataForNonFinancials()
              );
            });
        });
      }

      let euTaxoFinancialPreparedFixtures: Array<FixtureData<EuTaxonomyDataForFinancials>>;
      let lksgPreparedFixtures: Array<FixtureData<LksgData>>;

      before(() => {
        cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
          euTaxoFinancialPreparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        });
        cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
          lksgPreparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
        });

        uploadCompanyAlphaAndData();
        uploadCompanyBetaAndData();
      });

      it("Check that the redirect depends correctly on the applied filters and the framework select dropdown works as expected", () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        cy.intercept("/api/companies?searchString=&dataTypes=*").as("firstLoadOfSearchPage");
        cy.visit(`/companies?framework=${DataTypeEnum.EutaxonomyNonFinancials}`);
        cy.wait("@firstLoadOfSearchPage", { timeout: Cypress.env("long_timeout_in_ms") as number });
        createAllInterceptsOnFrameworkViewPage();
        searchCompanyViaLocalSearchBarAndSelectFirstSuggestion(nameOfCompanyAlpha);
        waitForAllInterceptsOnFrameworkViewPage();
        validateViewPage(DataTypeEnum.EutaxonomyNonFinancials);
        validateChosenFramework(DataTypeEnum.EutaxonomyNonFinancials);

        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(
          DataTypeEnum.EutaxonomyNonFinancials,
          nameOfCompanyAlpha
        );
        waitForAllInterceptsOnFrameworkViewPage();
        validateViewPage(DataTypeEnum.EutaxonomyNonFinancials);
        validateChosenFramework(DataTypeEnum.EutaxonomyNonFinancials);

        selectFrameworkInDropdown(DataTypeEnum.Lksg);
        waitForAllInterceptsOnFrameworkViewPage();
        validateViewPage(DataTypeEnum.Lksg);
        validateChosenFramework(DataTypeEnum.Lksg);

        selectFrameworkInDropdown(DataTypeEnum.EutaxonomyFinancials);
        waitForAllInterceptsOnFrameworkViewPage();
        validateViewPage(DataTypeEnum.EutaxonomyFinancials);
      });

      it("Check that from a framework page you can search a company without this framework", () => {
        cy.ensureLoggedIn();
        createAllInterceptsOnFrameworkViewPage();
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(DataTypeEnum.Sfdr, nameOfCompanyAlpha);
        waitForAllInterceptsOnFrameworkViewPage();
        validateViewPage(DataTypeEnum.Sfdr);
        searchCompanyViaLocalSearchBarAndSelectFirstSuggestion(
          nameOfCompanyBeta,
          "input#framework_data_search_bar_standard"
        );
        waitForAllInterceptsOnFrameworkViewPage();
        cy.get('[data-test="companyNameTitle"]').should("contain", nameOfCompanyBeta);

        // TODO check also for different starting scenarios, since this broke for Paul in some scenario
      });

      it("Check that reporting period dropdown works as expected", () => {
        cy.ensureLoggedIn();
        createAllInterceptsOnFrameworkViewPage();
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(
          DataTypeEnum.EutaxonomyFinancials,
          nameOfCompanyAlpha
        );
        validateNoErrorMessagesAreShown();
        waitForAllInterceptsOnFrameworkViewPage();
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForAlpha);
        validateChosenReportingPeriod("2019");
        validateDropdownOptions(reportingPeriodDropdownSelector, expectedReportingPeriodsForEuTaxoFinancialsForAlpha);
        validateEligibleActivityValueForFinancialsDataset("29.2");

        selectReportingPeriodInDropdown("2019");

        validateNoErrorMessagesAreShown();
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2019");
        validateEligibleActivityValueForFinancialsDataset("29.2");

        selectFrameworkInDropdown(DataTypeEnum.EutaxonomyFinancials);

        validateNoErrorMessagesAreShown();
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2019");
        validateEligibleActivityValueForFinancialsDataset("29.2");

        selectReportingPeriodInDropdown("2016");

        validateNoErrorMessagesAreShown();
        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForAlpha);
        validateChosenReportingPeriod("2016");
        validateDropdownOptions(reportingPeriodDropdownSelector, expectedReportingPeriodsForEuTaxoFinancialsForAlpha);
        validateEligibleActivityValueForFinancialsDataset("26");

        selectFrameworkInDropdown(DataTypeEnum.EutaxonomyNonFinancials);

        validateNoErrorMessagesAreShown();
        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        cy.wait("@getMetaDataForCompanyId", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyNonFinancials);
        validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForAlpha);
        validateChosenReportingPeriod("2015");
        validateDropdownOptions(
          reportingPeriodDropdownSelector,
          expectedReportingPeriodsForEuTaxoNonFinancialsForAlpha
        );

        selectFrameworkInDropdown(DataTypeEnum.Lksg);

        validateNoErrorMessagesAreShown();
        waitForAllInterceptsOnFrameworkViewPage();
        validateChosenFramework(DataTypeEnum.Lksg);
        validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForAlpha);
        validateOneColumnPerExpectedReportingPeriod(expectedReportingPeriodsForLksgForAlpha);

        clickBackButton();

        validateNoErrorMessagesAreShown();
        waitForAllInterceptsOnFrameworkViewPage();
        validateChosenFramework(DataTypeEnum.EutaxonomyNonFinancials);
        validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForAlpha);
        validateChosenReportingPeriod("2015");
        validateDropdownOptions(
          reportingPeriodDropdownSelector,
          expectedReportingPeriodsForEuTaxoNonFinancialsForAlpha
        );

        clickBackButton();

        validateNoErrorMessagesAreShown();
        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        cy.wait("@getMetaDataForCompanyId", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2016");
        validateEligibleActivityValueForFinancialsDataset("26");
      });

      it("Check that invalid data IDs or reporting periods in url don't break any user flow", () => {
        const nonExistingDataId = "abcd123123123123123-non-existing";
        const nonExistingReportingPeriod = "999999";
        cy.ensureLoggedIn();
        createAllInterceptsOnFrameworkViewPage();
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(
          DataTypeEnum.EutaxonomyFinancials,
          nameOfCompanyAlpha
        );

        validateNoErrorMessagesAreShown();
        waitForAllInterceptsOnFrameworkViewPage();
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);

        selectReportingPeriodInDropdown("2016");

        validateNoErrorMessagesAreShown();
        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2016");
        validateEligibleActivityValueForFinancialsDataset("26");

        cy.visit(`/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/${nonExistingDataId}`);

        getElementAndAssertExistence("noDataForThisDataIdPresentErrorIndicator", "exist");
        validateChosenReportingPeriod("Select...", true);

        selectReportingPeriodInDropdown("2019");

        validateNoErrorMessagesAreShown();
        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2019");
        validateEligibleActivityValueForFinancialsDataset("29.2");

        clickBackButton();

        getElementAndAssertExistence("noDataForThisDataIdPresentErrorIndicator", "exist");

        clickBackButton();

        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        cy.wait("@getMetaDataForCompanyId", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForAlpha);
        validateChosenReportingPeriod("2016");
        validateDropdownOptions(reportingPeriodDropdownSelector, expectedReportingPeriodsForEuTaxoFinancialsForAlpha);
        validateEligibleActivityValueForFinancialsDataset("26");

        cy.visit(
          `/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/reportingPeriods/${nonExistingReportingPeriod}`
        );

        getElementAndAssertExistence("noDataForThisReportingPeriodPresentErrorIndicator", "exist");
        validateChosenReportingPeriod("Select...", true);

        selectReportingPeriodInDropdown("2015");

        validateNoErrorMessagesAreShown();
        validateChosenReportingPeriod("2015");

        clickBackButton();

        getElementAndAssertExistence("noDataForThisReportingPeriodPresentErrorIndicator", "exist");
        validateChosenReportingPeriod("Select...", true);
      });
    }
  );
});
// TODO test search from specific scenarios
