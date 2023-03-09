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
import { humanizeString } from "../../../../src/utils/StringHumanizer";
import { uploadOneSfdrDataset } from "../../utils/SfdrUpload";
import { generateSfdrData } from "../../fixtures/sfdr/SfdrDataFixtures";
import { generateLksgData } from "../../fixtures/lksg/LksgDataFixtures"; // TODO write paths with @notation!

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
      const nameOfCompanyBeta = "company-beta-with-eutaxo-and-lksg-data";
      const frameworkDropdownSelector = "div#chooseFrameworkDropdown";
      const reportingPeriodDropdownSelector = "div#chooseReportingPeriodDropdown";
      const dropdownItemsSelector = "div.p-dropdown-items-wrapper li";
      const expectedDropdownItemsForAlpha = new Set<string>([
        humanizeString(DataTypeEnum.EutaxonomyFinancials),
        humanizeString(DataTypeEnum.EutaxonomyNonFinancials),
        humanizeString(DataTypeEnum.Lksg),
        humanizeString(DataTypeEnum.Sfdr),
      ]);

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
      function validateChosenFramework(expectedChosenFramework: string, expectedDropdownOptions: Set<string>): void {
        validateViewPage(expectedChosenFramework);
        cy.get("h2:contains('Checking if')").should("not.exist");
        cy.get(frameworkDropdownSelector)
          .find(".p-dropdown-label")
          .should("have.text", humanizeString(expectedChosenFramework));
        cy.get(frameworkDropdownSelector).click();
        let optionsCounter = 0;
        cy.get(dropdownItemsSelector)
          // .wait(500) // TODO better ideas?  it seems that sometimes the HTML elements are not rendered fast enough to be read
          .each((item) => {
            expect(expectedDropdownOptions.has(item.text())).to.equal(true);
            optionsCounter++;
          })
          .then(() => {
            expect(expectedDropdownOptions.size).to.equal(optionsCounter);
          });
      }

      function validateChosenReportingPeriod(
        expectedChosenReportingPeriod: string,
        expectedDropdownOptions: Set<string>
      ) {
        cy.url().should("contain", `/reportingPeriods/${expectedChosenReportingPeriod}`);
        cy.get("h2:contains('Checking if')").should("not.exist");
        cy.get(reportingPeriodDropdownSelector)
          .find(".p-dropdown-label")
          .should("have.text", expectedChosenReportingPeriod);
        cy.get(reportingPeriodDropdownSelector).click();
        let optionsCounter = 0;
        cy.get(dropdownItemsSelector)
          // .wait(500) // TODO better ideas?  it seems that sometimes the HTML elements are not rendered fast enough to be read
          .each((item) => {
            expect(expectedDropdownOptions.has(item.text())).to.equal(true);
            optionsCounter++;
          })
          .then(() => {
            expect(expectedDropdownOptions.size).to.equal(optionsCounter);
          });
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

      /**
       * Validates if the framework view page is currently displayed.
       *
       * @param framework the framework type as DataTypeEnum
       */
      function validateViewPage(framework: string): void {
        cy.url().should("contain", `/frameworks/${framework}`);
        cy.get('[data-test="frameworkDataTableTitle"]').should("contain", humanizeString(framework)); // TODO
      }

      /**
       * Uploads a specific company and LkSG dataset from the prepared fixtures, then EU Taxonomy data for financial
       * companies for that company.
       */
      function uploadCompanyAlphaAndData(): void {
        let companyId: string;
        const timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps = 2000;
        getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(nameOfCompanyAlpha))
            .then((storedCompany) => {
              companyId = storedCompany.companyId;
              return uploadOneLksgDatasetViaApi(
                token,
                companyId,
                "2023",
                getPreparedFixture("vat-2023-1", lksgPreparedFixtures).t
              );
            })
            .then(() => {
              return cy.wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps).then(() => {
                return uploadOneLksgDatasetViaApi(
                  token,
                  companyId,
                  "2023",
                  getPreparedFixture("vat-2023-2", lksgPreparedFixtures).t
                );
              });
            })
            .then(() => {
              return cy.wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps).then(() => {
                return uploadOneLksgDatasetViaApi(
                  token,
                  companyId,
                  "2022",
                  getPreparedFixture("vat-2022", lksgPreparedFixtures).t
                );
              });
            })
            .then(() => {
              return uploadOneSfdrDataset(token, companyId, "2019", generateSfdrData());
            })
            .then(() => {
              return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                token,
                companyId,
                "2019",
                getPreparedFixture("eligible-activity-Point-29", euTaxoFinancialPreparedFixtures).t
              );
            })
            .then(() => {
              return cy.wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps).then(() => {
                return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                  token,
                  companyId,
                  "2019",
                  getPreparedFixture("eligible-activity-Point-292", euTaxoFinancialPreparedFixtures).t
                );
              });
            })
            .then(() => {
              return cy.wait(timeDelayInMillisecondsBeforeNextUploadToAssureDifferentTimestamps).then(() => {
                return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                  token,
                  companyId,
                  "2016",
                  getPreparedFixture("eligible-activity-Point-26", euTaxoFinancialPreparedFixtures).t
                );
              });
            })
            .then(() => {
              return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
                token,
                companyId,
                "2019",
                generateEuTaxonomyDataForNonFinancials()
              );
            });
        });
      } // TODO optional: you can try to make the waits actually wait where they are written.  currently they are chained at the end

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
        validateChosenFramework(DataTypeEnum.EutaxonomyNonFinancials, expectedDropdownItemsForAlpha);

        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(
          DataTypeEnum.EutaxonomyNonFinancials,
          nameOfCompanyAlpha
        );
        waitForAllInterceptsOnFrameworkViewPage();
        validateViewPage(DataTypeEnum.EutaxonomyNonFinancials);
        validateChosenFramework(DataTypeEnum.EutaxonomyNonFinancials, expectedDropdownItemsForAlpha);

        selectFrameworkInDropdown(DataTypeEnum.Lksg);
        waitForAllInterceptsOnFrameworkViewPage();
        validateViewPage(DataTypeEnum.Lksg);
        validateChosenFramework(DataTypeEnum.Lksg, expectedDropdownItemsForAlpha);

        selectFrameworkInDropdown(DataTypeEnum.EutaxonomyFinancials);
        waitForAllInterceptsOnFrameworkViewPage();
        validateViewPage(DataTypeEnum.EutaxonomyFinancials);
      });

      it("Check that from a framework page you can search a company without this framework", () => {
        // TODO add waits
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
      });

      it("Check that reporting period dropdown works as expected", () => {
        cy.ensureLoggedIn();
        createAllInterceptsOnFrameworkViewPage();
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(
          DataTypeEnum.EutaxonomyFinancials,
          nameOfCompanyAlpha
        );
        waitForAllInterceptsOnFrameworkViewPage();
        validateViewPage(DataTypeEnum.EutaxonomyFinancials);
        validateChosenReportingPeriod("2019", new Set(["2016", "2019"]));
        // validateEligibleActivityValue("29,2")

        // selectReportingPeriodInDropdown("2019")

        // validateReportingPeriodInUrl( "2019")
        // validateReportingPeriodDropdown("2019", ["2016", "2019"])
        // validateEligibleActivityValue("29,2")

        // selectReportingPeriodInDropdown("2016")

        // validateReportingPeriodInUrl( "2016")
        // validateReportingPeriodDropdown("2016", ["2016", "2019"])
        // validateEligibleActivityValue("26")

        // switch to non-taxo

        // validateReportingPeriodInUrl( "2019")
        // validateReportingPeriodDropdown("2019", ["2019"])

        // switch to Lksg

        // validateViewPage(DataTypeEnum.Lksg);

        // back

        // validateReportingPeriodInUrl( "2019")
        // validateReportingPeriodDropdown("2019", ["2019"])

        // back

        // validateReportingPeriodInUrl( "2016")
        // validateReportingPeriodDropdown("2016", ["2016", "2019"])
        // validateEligibleActivityValue("26")

        // back

        // validateReportingPeriodInUrl( "2019")
        // validateReportingPeriodDropdown("2019", ["2016", "2019"])
        // validateEligibleActivityValue("29,2")

        // done

        // TODO

        /*
        test 1: Reporting Period Dropdown
        - assert that you land on view-page for the "latest" dataset
        -  and reporting period dropdown contains all reportingPeriods for current framework
        - switch between reporting periods and assert that data and chosen reporting period and url update correctly
        - do this backwards until you are at the starting point and asserts that everything (URL, chosenReportingPeriod, Data) stays in sync during this
        - Change the reporting period once via dropdown, then change to the other eu-taxo-framework via dropdown, then switch back via dropdown and assert
          that the "latest parsed" reporting period with url and data is in sync
          - Change the reporting period once via dropdown, then change to the other eu-taxo-framework via dropdown, then switch back via BACK BUTTON and assert
            that the "latest parsed" reporting period with url and data is in sync
            - visit the page you are on with an invalid dataId (we already have this test somewhere else => delete it there), assert error message,
                assert "select..." in reporting period dropdown, then choose a reporting Peiod, then click "BACK" and assert that still correct error page
            - switch between eutaxo and lksg (via BACK) and assert that reporting Period is still correct and in sync with data and url
        */
      });

      it("Check that invalid data IDs or reporting periods in url don't break anything", () => {
        cy.ensureLoggedIn();
        createAllInterceptsOnFrameworkViewPage();
        visitSearchPageWithQueryParamsAndClickOnFirstSearchResult(
          DataTypeEnum.EutaxonomyFinancials,
          nameOfCompanyAlpha
        );
        waitForAllInterceptsOnFrameworkViewPage();
        validateViewPage(DataTypeEnum.EutaxonomyFinancials);
        // validateReportingPeriodInUrl( "2019")
        // validateReportingPeriodDropdown("2019", ["2016", "2019"])
        // validateEligibleActivityValue("29,2")

        // visit invalid data Id

        // validate error message for wrong data ID and validate dropdowns
        // TIPP:  cy.contains("h1", "No company with this ID present");

        // choose reporting Period "2016"

        // validateReportingPeriodInUrl( "2016")
        // validateReportingPeriodDropdown("2016", ["2016", "2019"])
        // validateEligibleActivityValue("26")

        // back

        // validate error message for wrong data ID  + url + dropdowns

        // back

        //  validateReportingPeriodInUrl( "2019")
        //  validateReportingPeriodDropdown("2019", ["2016", "2019"])
        //  validateEligibleActivityValue("29,2")

        // visit invalid reporting Period

        // validate error message for invalid reporting Period + dropdowns

        // choose reporting Period "2016"

        // validateReportingPeriodInUrl( "2016")
        // validateReportingPeriodDropdown("2016", ["2016", "2019"])
        // validateEligibleActivityValue("26")

        // back

        // validate error message for wrong reportin Period  + url + dropdowns

        // back

        //  validateReportingPeriodInUrl( "2019")
        //  validateReportingPeriodDropdown("2019", ["2016", "2019"])
        //  validateEligibleActivityValue("29,2")

        // visit invalid data Id

        // switch framework to Lksg

        // validate lksg

        // back

        // validate error message for wrong data ID  + url + dropdowns

        // Done
      });
    }
  );
});
