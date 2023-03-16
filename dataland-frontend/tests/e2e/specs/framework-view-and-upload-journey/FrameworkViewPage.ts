import { describeIf } from "@e2e/support/TestUtility";
import { getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@sharedUtils/Fixtures";
import { DataTypeEnum, EuTaxonomyDataForFinancials, LksgData } from "@clients/backend";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { uploadOneLksgDatasetViaApi } from "@e2e/utils/LksgUpload";
import { getPreparedFixture } from "@sharedUtils/Fixtures";
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
      let companyIdOfAlpha: string;

      let dataIdOfSupersededLksg2023ForAlpha: string;
      let dataIdOfSupersededFinancial2019ForAlpha: string;

      const nameOfCompanyBeta = "company-beta-with-eutaxo-and-lksg-data";
      const expectedFrameworkDropdownItemsForBeta = new Set<string>([
        humanizeString(DataTypeEnum.EutaxonomyNonFinancials),
        humanizeString(DataTypeEnum.Lksg),
      ]);
      let companyIdOfBeta: string;

      const frameworkDropdownSelector = "div#chooseFrameworkDropdown";
      const reportingPeriodDropdownSelector = "div#chooseReportingPeriodDropdown";
      const dropdownItemsSelector = "div.p-dropdown-items-wrapper li";
      const searchBarSelectorForViewPage = "input#framework_data_search_bar_standard";

      const nonExistingDataId = "abcd123123123123123-non-existing";
      const nonExistingReportingPeriod = "999999-non-existing";
      const nonExistingCompanyId = "ABC-non-existing";

      /**
       * Creates interceptions for all three requests which are sent when you visit the view-page from somewhere else.
       *
       */
      function createAllInterceptsOnFrameworkViewPage(): void {
        cy.intercept("/api/companies/**-**-**").as("getCompanyInformation");
        cy.intercept("/api/metadata**").as("getMetaDataQuery");
        cy.intercept("/api/data/**").as("getFrameworkData");
      }

      /**
       * Waits for all three requests which are sent when you visit the view-page from somewhere else.
       *
       */
      function waitForAllInterceptsOnFrameworkViewPage(): void {
        cy.wait(["@getCompanyInformation", "@getMetaDataQuery", "@getFrameworkData"], {
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
       * Types a search string into the searchbar and clicks on the first autocomplete suggestion.
       *
       * @param searchString The search string to type into the search bar
       * @param searchBarSelector The selector to select the correct search bar from the DOM
       */
      function typeSearchStringIntoSearchBarAndSelectFirstSuggestion(
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
       * Validates that the view-page is currently set to the expected framework by checking the url and the
       * chosen option in the frameworks-dropdown.
       *
       * @param expectedChosenFramework The framework wich is expected to be currently set
       */
      function validateChosenFramework(expectedChosenFramework: string): void {
        cy.url().should("contain", `/frameworks/${expectedChosenFramework}`);
        cy.get('[data-test="frameworkDataTableTitle"]').should("contain", humanizeString(expectedChosenFramework));
        cy.get("h2:contains('Checking if')").should("not.exist");
        cy.get(frameworkDropdownSelector)
          .find(".p-dropdown-label")
          .should("have.text", humanizeString(expectedChosenFramework));
      }

      /**
       * Validates that the view-page is currently set to the expected reporting period by checking the url and
       * the chosen option in the reporting-periods-dropdown.
       *
       * @param expectedChosenReportingPeriod The reporting period wich is expected to be currently set
       * @param skipUrlCheck This flag makes it possible to skip the url-check
       */
      function validateChosenReportingPeriod(expectedChosenReportingPeriod: string, skipUrlCheck = false): void {
        if (!skipUrlCheck) {
          cy.url().should("contain", `/reportingPeriods/${expectedChosenReportingPeriod}`);
        }
        cy.get("h2:contains('Checking if')").should("not.exist");
        cy.get(reportingPeriodDropdownSelector)
          .find(".p-dropdown-label")
          .should("have.text", expectedChosenReportingPeriod);
      }

      /**
       * Validates that a specific dropdown contains some expected options.
       *
       * @param dropdownSelector The selector to be used to identify the dropdown which needs to be validated
       * @param expectedDropdownOptions The expected options for this dropdown
       */
      function validateDropdownOptions(dropdownSelector: string, expectedDropdownOptions: Set<string>): void {
        cy.get(dropdownSelector).click();
        let optionsCounter = 0;
        cy.get(dropdownItemsSelector).should("exist");
        cy.get(`${dropdownItemsSelector}:contains("No available options")`).should("not.exist");
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

      /**
       * Checks if none of the currently three possible error-blocks on the view-page are rendered.
       *
       */
      function validateNoErrorMessagesAreShown(): void {
        getElementAndAssertExistence("noDataForThisFrameworkPresentErrorIndicator", "not.exist");
        getElementAndAssertExistence("noDataForThisDataIdPresentErrorIndicator", "not.exist");
        getElementAndAssertExistence("noDataForThisReportingPeriodPresentErrorIndicator", "not.exist");
        getElementAndAssertExistence("noCompanyWithThisIdErrorIndicator", "not.exist");
        getElementAndAssertExistence("noDataCouldBeLoadedErrorIndicator", "not.exist");
      }

      /**
       * Gets an HTML element by looking for a specific value for the "data-test" HTML attribute and runs a
       * "should"-operation on that HTML element
       *
       * @param dataTestValue The value which the HTML element should have for the attribute "data-test"
       * @param shouldTag The value of the cypress "should" operation, e.g. "not.exist"
       */
      function getElementAndAssertExistence(dataTestValue: string, shouldTag: string): void {
        cy.get(`[data-test=${dataTestValue}]`).should(shouldTag);
      }

      /**
       * Opens the framework dropdown and selects the framework passed as input if it is found
       *
       * @param frameworkToSelect The framework/item that shall be selected
       */
      function selectFrameworkInDropdown(frameworkToSelect: string): void {
        cy.get(frameworkDropdownSelector).click();
        cy.get(`${dropdownItemsSelector}:contains(${humanizeString(frameworkToSelect)})`).click({ force: true });
      }

      /**
       * Opens the reporting periods dropdown and selects the reporting period passed as input if it is found
       *
       * @param reportingPeriodToSelect The reporting period/the item that shall be selected
       */
      function selectReportingPeriodInDropdown(reportingPeriodToSelect: string): void {
        cy.get(reportingPeriodDropdownSelector).click();
        cy.get(`${dropdownItemsSelector}:contains(${reportingPeriodToSelect})`).click({ force: true });
      }

      /**
       * Clicks the back button on the page.
       */
      function clickBackButton(): void {
        cy.get('[data-test="backButton"]').click();
      }

      /**
       * Validates if the container which displays a specific status of the current dataset is present and contains
       * the expected text.
       * It also validates if the corresponding button in that container contains the expected text.
       *
       * @param expectedTextInContainer The expected disclaimer text in the display-status-container
       * @param expectedButtonText The expected text inside the corresponding button of the display-status-container
       * @returns a Cypress Chainable containing the button of the display-status-container
       */
      function validateDisplayStatusContainerAndGetButton(
        expectedTextInContainer: string,
        expectedButtonText: string
      ): Cypress.Chainable {
        return cy
          .get(`[data-test="datasetDisplayStatusContainer"]:contains(${expectedTextInContainer})`)
          .find(`button > span:contains(${expectedButtonText})`);
      }

      /**
       * Validates if all the column headers equal the passed values
       *
       * @param expectedColumnHeaders The expected values in the headers of the LkSG dataset columns
       */
      function validateColumnHeadersOfDisplayedLksgDatasets(expectedColumnHeaders: string[]): void {
        cy.get(".p-column-title").each((element, index, elements) => {
          expect(elements).to.have.length(expectedColumnHeaders.length + 1);
          if (index == 0) {
            expect(element.text()).to.equal("KPIs");
          } else {
            expect(element.text()).to.equal(expectedColumnHeaders[index - 1]);
          }
        });
      }

      /**
       * Validates if all the values in the Vat ID row on the LkSG panel equal the passed values
       *
       * @param expectedVatIdNumbers The expected values in the row of the VAT identification number field
       */
      function validateVatIdNumbersOfDisplayedLksgDatasets(expectedVatIdNumbers: string[]): void {
        cy.get(`tr:contains("VAT Identification Number")`)
          .find("td > span")
          .each((element, index, elements) => {
            expect(elements).to.have.length(expectedVatIdNumbers.length + 1);
            if (index == 0) {
              expect(element.text()).to.equal("VAT Identification Number");
            } else {
              expect(element.text()).to.equal(expectedVatIdNumbers[index - 1]);
            }
          });
      }

      /**
       * Validates that the EU taxonomy financials table is there and has the expected taxonomy eligible economic activity value in percent
       *
       * @param expectedTaxonomyEligibleEconomicActivityValueInPercent  the expected taxonomy eligible economic activity value in percent
       */
      function validateEUTaxonomyFinancialsTable(expectedTaxonomyEligibleEconomicActivityValueInPercent: string): void {
        cy.get("[data-test='taxocard']:contains('Taxonomy-eligible economic activity')")
          .find("[data-test='value']")
          .should("have.text", expectedTaxonomyEligibleEconomicActivityValueInPercent);
      }

      /**
       * Uploads the test company "Alpha" with its datasets.
       *
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
              ).then((dataMetaInformation) => {
                dataIdOfSupersededLksg2023ForAlpha = dataMetaInformation.dataId;
              });
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
                getPreparedFixture("eligible-activity-Point-0.29", euTaxoFinancialPreparedFixtures).t
              ).then((dataMetaInformation) => {
                dataIdOfSupersededFinancial2019ForAlpha = dataMetaInformation.dataId;
              });
            })
            .then(() => {
              return cy.wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps).then(() => {
                return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                  token,
                  companyIdOfAlpha,
                  "2019",
                  getPreparedFixture("eligible-activity-Point-0.292", euTaxoFinancialPreparedFixtures).t
                );
              });
            })
            .then(() => {
              return cy.wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps).then(() => {
                return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                  token,
                  companyIdOfAlpha,
                  "2016",
                  getPreparedFixture("eligible-activity-Point-0.26", euTaxoFinancialPreparedFixtures).t
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

      /**
       * Uploads the test company "Beta" with its datasets.
       *
       */
      function uploadCompanyBetaAndData(): void {
        getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(nameOfCompanyBeta))
            .then((storedCompany) => {
              companyIdOfBeta = storedCompany.companyId;
              return uploadOneLksgDatasetViaApi(token, companyIdOfBeta, "2015", generateLksgData());
            })
            .then(() => {
              return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
                token,
                companyIdOfBeta,
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
        typeSearchStringIntoSearchBarAndSelectFirstSuggestion(nameOfCompanyAlpha);
        waitForAllInterceptsOnFrameworkViewPage();
        validateChosenFramework(DataTypeEnum.EutaxonomyNonFinancials);

        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(
          DataTypeEnum.EutaxonomyNonFinancials,
          nameOfCompanyAlpha
        );
        waitForAllInterceptsOnFrameworkViewPage();
        validateChosenFramework(DataTypeEnum.EutaxonomyNonFinancials);

        selectFrameworkInDropdown(DataTypeEnum.Lksg);
        waitForAllInterceptsOnFrameworkViewPage();
        validateChosenFramework(DataTypeEnum.Lksg);

        selectFrameworkInDropdown(DataTypeEnum.EutaxonomyFinancials);
        waitForAllInterceptsOnFrameworkViewPage();
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
      });

      it(
        "Check that from the view-page, even in error mode, you can search a company, even if it" +
          "does not have a dataset for the framework chosen on the search page",
        () => {
          cy.ensureLoggedIn();
          createAllInterceptsOnFrameworkViewPage();
          cy.visit(`/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.Sfdr}`);

          waitForAllInterceptsOnFrameworkViewPage();
          validateChosenFramework(DataTypeEnum.Sfdr);

          typeSearchStringIntoSearchBarAndSelectFirstSuggestion(nameOfCompanyBeta, searchBarSelectorForViewPage);

          waitForAllInterceptsOnFrameworkViewPage();
          cy.get('[data-test="companyNameTitle"]').should("contain", nameOfCompanyBeta);
          validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForBeta);

          cy.visit(
            `/companies/${companyIdOfBeta}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/${nonExistingDataId}`
          );
          typeSearchStringIntoSearchBarAndSelectFirstSuggestion(nameOfCompanyAlpha, searchBarSelectorForViewPage);

          waitForAllInterceptsOnFrameworkViewPage();
          cy.get('[data-test="companyNameTitle"]').should("contain", nameOfCompanyAlpha);
          validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForAlpha);

          cy.visit(
            `/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/reportingPeriods/${nonExistingReportingPeriod}`
          );
          typeSearchStringIntoSearchBarAndSelectFirstSuggestion(nameOfCompanyBeta, searchBarSelectorForViewPage);

          waitForAllInterceptsOnFrameworkViewPage();
          cy.get('[data-test="companyNameTitle"]').should("contain", nameOfCompanyBeta);
        }
      );

      it("Check that using back-button and dropdowns on the view-page work as expected", () => {
        cy.ensureLoggedIn();
        createAllInterceptsOnFrameworkViewPage();
        cy.visit(`/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`);
        validateNoErrorMessagesAreShown();
        waitForAllInterceptsOnFrameworkViewPage();
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForAlpha);
        validateChosenReportingPeriod("2019");
        validateDropdownOptions(reportingPeriodDropdownSelector, expectedReportingPeriodsForEuTaxoFinancialsForAlpha);
        validateEUTaxonomyFinancialsTable("29.2");

        selectReportingPeriodInDropdown("2019");

        validateNoErrorMessagesAreShown();
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2019");
        validateEUTaxonomyFinancialsTable("29.2");

        selectFrameworkInDropdown(DataTypeEnum.EutaxonomyFinancials);

        validateNoErrorMessagesAreShown();
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2019");
        validateEUTaxonomyFinancialsTable("29.2");

        selectReportingPeriodInDropdown("2016");

        validateNoErrorMessagesAreShown();
        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForAlpha);
        validateChosenReportingPeriod("2016");
        validateDropdownOptions(reportingPeriodDropdownSelector, expectedReportingPeriodsForEuTaxoFinancialsForAlpha);
        validateEUTaxonomyFinancialsTable("26");

        selectFrameworkInDropdown(DataTypeEnum.EutaxonomyNonFinancials);

        validateNoErrorMessagesAreShown();
        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        cy.wait("@getMetaDataQuery", { timeout: Cypress.env("long_timeout_in_ms") as number });
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
        validateColumnHeadersOfDisplayedLksgDatasets(["2023", "2022"]);

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
        cy.wait("@getMetaDataQuery", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2016");
        validateEUTaxonomyFinancialsTable("26");
      });

      it("Check that invalid data ID, reporting period or company ID in URL don't break any user flow on the view-page", () => {
        cy.ensureLoggedIn();
        createAllInterceptsOnFrameworkViewPage();
        cy.visit(`/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`);

        validateNoErrorMessagesAreShown();
        waitForAllInterceptsOnFrameworkViewPage();
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);

        selectReportingPeriodInDropdown("2016");

        validateNoErrorMessagesAreShown();
        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2016");
        validateEUTaxonomyFinancialsTable("26");

        cy.visit(`/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/${nonExistingDataId}`);

        getElementAndAssertExistence("noDataForThisDataIdPresentErrorIndicator", "exist");
        validateChosenReportingPeriod("Select...", true);

        selectReportingPeriodInDropdown("2019");

        validateNoErrorMessagesAreShown();
        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2019");
        validateEUTaxonomyFinancialsTable("29.2");

        clickBackButton();

        getElementAndAssertExistence("noDataForThisDataIdPresentErrorIndicator", "exist");

        clickBackButton();

        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        cy.wait("@getMetaDataQuery", { timeout: Cypress.env("long_timeout_in_ms") as number });
        validateChosenFramework(DataTypeEnum.EutaxonomyFinancials);
        validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForAlpha);
        validateChosenReportingPeriod("2016");
        validateDropdownOptions(reportingPeriodDropdownSelector, expectedReportingPeriodsForEuTaxoFinancialsForAlpha);
        validateEUTaxonomyFinancialsTable("26");

        cy.visit(
          `/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/reportingPeriods/${nonExistingReportingPeriod}`
        );
        cy.wait(["@getCompanyInformation", "@getMetaDataQuery"], {
          timeout: Cypress.env("long_timeout_in_ms") as number,
        });

        getElementAndAssertExistence("noDataForThisReportingPeriodPresentErrorIndicator", "exist");
        validateChosenReportingPeriod("Select...", true);

        selectReportingPeriodInDropdown("2015");

        validateNoErrorMessagesAreShown();
        validateChosenReportingPeriod("2015");

        clickBackButton();

        getElementAndAssertExistence("noDataForThisReportingPeriodPresentErrorIndicator", "exist");
        validateChosenReportingPeriod("Select...", true);

        cy.visit(
          `/companies/${nonExistingCompanyId}/frameworks/${DataTypeEnum.Lksg}/${dataIdOfSupersededLksg2023ForAlpha}`
        );

        getElementAndAssertExistence("noCompanyWithThisIdErrorIndicator", "not.exist");
        getElementAndAssertExistence("noDataCouldBeLoadedErrorIndicator", "not.exist");

        typeSearchStringIntoSearchBarAndSelectFirstSuggestion(nameOfCompanyBeta, searchBarSelectorForViewPage);

        waitForAllInterceptsOnFrameworkViewPage();
        validateDropdownOptions(frameworkDropdownSelector, expectedFrameworkDropdownItemsForBeta);
        validateNoErrorMessagesAreShown();

        clickBackButton();

        getElementAndAssertExistence("noCompanyWithThisIdErrorIndicator", "exist");
        getElementAndAssertExistence("noDataCouldBeLoadedErrorIndicator", "exist");
      });

      it("Check if the version change bar works as expected on several framework view pages", () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        cy.intercept("/api/metadata/**").as("getMetaDataForSpecificDataId");
        createAllInterceptsOnFrameworkViewPage();
        cy.visit(
          `/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.Lksg}/${dataIdOfSupersededLksg2023ForAlpha}`
        );

        waitForAllInterceptsOnFrameworkViewPage();
        validateColumnHeadersOfDisplayedLksgDatasets(["2023"]);
        validateVatIdNumbersOfDisplayedLksgDatasets(["2023-1"]);
        validateDisplayStatusContainerAndGetButton("This dataset is superseded", "View Active").click();

        cy.wait(["@getMetaDataForSpecificDataId", "@getFrameworkData"], {
          timeout: Cypress.env("long_timeout_in_ms") as number,
        });
        cy.url().should(
          "eq",
          `${getBaseUrl()}/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.Lksg}/reportingPeriods/2023`
        );
        validateColumnHeadersOfDisplayedLksgDatasets(["2023"]);
        validateVatIdNumbersOfDisplayedLksgDatasets(["2023-2"]);
        validateDisplayStatusContainerAndGetButton(
          "You are only viewing a single available dataset",
          "View All"
        ).click();

        cy.wait("@getFrameworkData", {
          timeout: Cypress.env("long_timeout_in_ms") as number,
        });
        cy.url().should("eq", `${getBaseUrl()}/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.Lksg}`);
        validateColumnHeadersOfDisplayedLksgDatasets(["2023", "2022"]);
        validateVatIdNumbersOfDisplayedLksgDatasets(["2023-2", "2022"]);
        cy.contains("This dataset is superseded").should("not.exist");
        getElementAndAssertExistence("datasetDisplayStatusContainer", "not.exist");
        clickBackButton();

        cy.wait(["@getMetaDataForSpecificDataId", "@getFrameworkData"], {
          timeout: Cypress.env("long_timeout_in_ms") as number,
        });
        cy.url().should(
          "eq",
          `${getBaseUrl()}/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.Lksg}/reportingPeriods/2023`
        );
        validateColumnHeadersOfDisplayedLksgDatasets(["2023"]);
        validateVatIdNumbersOfDisplayedLksgDatasets(["2023-2"]);
        validateDisplayStatusContainerAndGetButton("You are only viewing a single available dataset", "View All");
        clickBackButton();

        cy.wait(["@getMetaDataForSpecificDataId", "@getFrameworkData"], {
          timeout: Cypress.env("long_timeout_in_ms") as number,
        });
        cy.url().should(
          "eq",
          `${getBaseUrl()}/companies/${companyIdOfAlpha}/frameworks/${
            DataTypeEnum.Lksg
          }/${dataIdOfSupersededLksg2023ForAlpha}`
        );
        validateColumnHeadersOfDisplayedLksgDatasets(["2023"]);
        validateVatIdNumbersOfDisplayedLksgDatasets(["2023-1"]);
        validateDisplayStatusContainerAndGetButton("This dataset is superseded", "View Active");
        cy.visit(
          `/companies/${companyIdOfAlpha}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/${dataIdOfSupersededFinancial2019ForAlpha}`
        );

        waitForAllInterceptsOnFrameworkViewPage();
        validateEUTaxonomyFinancialsTable("29");
        validateDisplayStatusContainerAndGetButton("This dataset is superseded", "View Active").click();

        cy.wait("@getFrameworkData", { timeout: Cypress.env("long_timeout_in_ms") as number });
        cy.url().should(
          "eq",
          `${getBaseUrl()}/companies/${companyIdOfAlpha}/frameworks/${
            DataTypeEnum.EutaxonomyFinancials
          }/reportingPeriods/2019`
        );
        validateEUTaxonomyFinancialsTable("29.2");
        getElementAndAssertExistence("datasetDisplayStatusContainer", "not.exist");
        clickBackButton();

        cy.wait(["@getMetaDataForSpecificDataId", "@getFrameworkData"], {
          timeout: Cypress.env("long_timeout_in_ms") as number,
        });
        cy.url().should(
          "eq",
          `${getBaseUrl()}/companies/${companyIdOfAlpha}/frameworks/${
            DataTypeEnum.EutaxonomyFinancials
          }/${dataIdOfSupersededFinancial2019ForAlpha}`
        );
        validateEUTaxonomyFinancialsTable("29");
      });
    }
  );
});
