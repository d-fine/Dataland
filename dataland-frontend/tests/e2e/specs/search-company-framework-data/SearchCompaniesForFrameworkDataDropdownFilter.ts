import { describeIf } from "@e2e/support/TestUtility";
import {
  getFirstEuTaxonomyFinancialsDatasetFromFixtures,
  uploadOneEuTaxonomyFinancialsDatasetViaApi,
} from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadOneEuTaxonomyNonFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { DataTypeEnum, EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodeConverter";
import { getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { verifyTaxonomySearchResultTable } from "@e2e/utils/VerifyingElements";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { convertStringToQueryParamFormat } from "@e2e/utils/Converters";

let companiesWithEuTaxonomyDataForNonFinancials: Array<FixtureData<EuTaxonomyDataForNonFinancials>>;

before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (jsonContent) {
    companiesWithEuTaxonomyDataForNonFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForNonFinancials>>;
  });
});

describe("As a user, I expect the search functionality on the /companies page to adjust to the selected dropdown filters", () => {
  it(
    "The framework filter should contain SFDR even though it is not yet implemented, and synchronise " +
      "between the search bar and the URL",
    { scrollBehavior: false },
    () => {
      cy.ensureLoggedIn();
      cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
      cy.visit("/companies").wait("@companies-meta-information");
      verifyTaxonomySearchResultTable();
      cy.get("#framework-filter")
        .click()
        .get("div.p-multiselect-panel")
        .find("li.p-disabled:contains('SFDR')")
        .should("exist")
        .get("div.p-multiselect-panel")
        .find("li.p-highlight:contains('EU Taxonomy for financial companies')")
        .click();
      verifyTaxonomySearchResultTable();
      cy.url()
        .should(
          "eq",
          getBaseUrl() + `/companies?framework=${DataTypeEnum.EutaxonomyNonFinancials}&framework=${DataTypeEnum.Lksg}`
        )
        .get("div.p-multiselect-panel")
        .find("li.p-multiselect-item:contains('EU Taxonomy for financial companies')")
        .click();
      verifyTaxonomySearchResultTable();
      cy.url()
        .should("eq", getBaseUrl() + "/companies")
        .get("div.p-multiselect-panel")
        .find("li.p-highlight:contains('EU Taxonomy for non-financial companies')")
        .click();
      verifyTaxonomySearchResultTable();
      cy.url().should(
        "eq",
        getBaseUrl() + `/companies?framework=${DataTypeEnum.EutaxonomyFinancials}&framework=${DataTypeEnum.Lksg}`
      );
    }
  );
  it(
    "Checks that the country-code filter synchronises between the search bar and the drop down and works",
    { scrollBehavior: false },
    () => {
      const demoCompanyToTestFor = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation;
      const demoCompanyWithDifferentCountryCode = companiesWithEuTaxonomyDataForNonFinancials.find(
        (it) => it.companyInformation.countryCode !== demoCompanyToTestFor.countryCode
      )!.companyInformation;

      const demoCompanyToTestForCountryName = getCountryNameFromCountryCode(demoCompanyToTestFor.countryCode);

      cy.ensureLoggedIn();
      cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
      cy.visit(
        `/companies?input=${demoCompanyToTestFor.companyName}&countryCode=${demoCompanyWithDifferentCountryCode.countryCode}`
      )
        .wait("@companies-meta-information")
        .get("div[class='col-12 text-left']")
        .should("contain.text", "Sorry! Your search didn't return any results.")
        .get("#country-filter")
        .click()
        .get('input[placeholder="Search countries"]')
        .type(`${demoCompanyToTestForCountryName}`)
        .get("li")
        .contains(RegExp(`^${demoCompanyToTestForCountryName}$`))
        .click()
        .get("td[class='d-bg-white w-3 d-datatable-column-left']")
        .contains(demoCompanyToTestFor.companyName)
        .should("exist")
        .url()
        .should("contain", `countryCode=${convertStringToQueryParamFormat(demoCompanyToTestFor.countryCode)}`);
    }
  );
  it(
    "Checks that the sector filter synchronises between the search bar and the drop down and works",
    { scrollBehavior: false },
    () => {
      const demoCompanyToTestFor = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation;
      const demoCompanyWithDifferentSector = companiesWithEuTaxonomyDataForNonFinancials.find(
        (it) => it.companyInformation.sector !== demoCompanyToTestFor.sector
      )!.companyInformation;

      cy.ensureLoggedIn();
      cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
      cy.visit(`/companies?input=${demoCompanyToTestFor.companyName}&sector=${demoCompanyWithDifferentSector.sector}`)
        .wait("@companies-meta-information")
        .get("div[class='col-12 text-left']")
        .should("contain.text", "Sorry! Your search didn't return any results.")
        .get("#sector-filter")
        .click()
        .get('input[placeholder="Search sectors"]')
        .type(`${demoCompanyToTestFor.sector}`)
        .get("li")
        .contains(RegExp(`^${demoCompanyToTestFor.sector}$`))
        .click()
        .get("td[class='d-bg-white w-3 d-datatable-column-left']")
        .contains(demoCompanyToTestFor.companyName)
        .should("exist")
        .url()
        .should("contain", `sector=${convertStringToQueryParamFormat(demoCompanyToTestFor.sector)}`);
    }
  );
  it("Checks that the reset button works as expected", { scrollBehavior: false }, () => {
    const demoCompanyToTestFor = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation;
    cy.ensureLoggedIn();
    cy.visit(
      `/companies?sector=${demoCompanyToTestFor.sector}&countryCode=${demoCompanyToTestFor.countryCode}&framework=${DataTypeEnum.EutaxonomyNonFinancials}`
    )
      .get("span:contains('RESET')")
      .eq(0)
      .click()
      .url()
      .should("eq", getBaseUrl() + "/companies");
  });
  it(
    "Check that the filter dropdowns close when you scroll, especially on the resulting query when you check a box while you are not at the top of the page",
    { scrollBehavior: false },
    () => {
      cy.ensureLoggedIn();
      cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
      cy.visit("/companies").wait("@companies-meta-information");
      verifyTaxonomySearchResultTable();
      cy.get("#framework-filter").click().get("div.p-multiselect-panel").should("exist");

      cy.scrollTo(0, 500, { duration: 300 }).get("div.p-multiselect-panel").should("not.exist");
      cy.get("#framework-filter").click().get("div.p-multiselect-panel").should("exist");
      cy.scrollTo(0, 600, { duration: 300 }).get("div.p-multiselect-panel").should("not.exist");
      cy.get("#framework-filter").click().get("div.p-multiselect-panel").should("exist");
      cy.scrollTo(0, 500, { duration: 300 })
        .get("div.p-multiselect-panel")
        .should("not.exist")
        .get("#framework-filter")
        .click()
        .get("div.p-multiselect-panel")
        .find("li.p-multiselect-item")
        .first()
        .click();
      verifyTaxonomySearchResultTable();
      cy.get("div.p-multiselect-panel").should("not.exist");
    }
  );

  describeIf(
    "As a user, I expect the search results to adjust according to the framework filter",
    {
      executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      dataEnvironments: ["fakeFixtures"],
    },
    function () {
      beforeEach(function () {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
      });

      it(
        "Upload a company without uploading framework data for it, assure that its sector does not appear as filter " +
          "option, and check if the company neither appears in the autocomplete suggestions nor in the " +
          "search results, even though no framework filter is actively set.",
        () => {
          const companyName = "ThisCompanyShouldNeverBeFound12349876";
          const sector = "ThisSectorShouldNeverAppearInDropdown";
          getKeycloakToken(uploader_name, uploader_pw).then((token) => {
            return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName, sector));
          });
          cy.visit(`/companies`);
          cy.intercept("**/api/companies/meta-information").as("getFilterOptions");
          verifyTaxonomySearchResultTable();
          cy.wait("@getFilterOptions", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
            verifyTaxonomySearchResultTable();
            cy.get("#sector-filter")
              .click({ scrollBehavior: false })
              .get('input[placeholder="Search sectors"]')
              .type(sector, { scrollBehavior: false })
              .get("div.p-multiselect-panel")
              .find("li:contains('No results found')")
              .should("exist");
          });
          cy.intercept("**/api/companies*").as("searchCompany");
          cy.get("input[id=search_bar_top]")
            .click({ scrollBehavior: false })
            .type(companyName, { scrollBehavior: false });
          cy.wait("@searchCompany", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
            cy.wait(1000);
            cy.get(".p-autocomplete-item").should("not.exist");
          });
          cy.visit(`/companies?input=${companyName}`)
            .get("div[class='col-12 text-left']")
            .should("contain.text", "Sorry! Your search didn't return any results.");
        }
      );

      const companyNameMarker = "Data987654321";

      it(
        "Upload a company with Eu Taxonomy Data For Financials and check if it only appears in the results if the " +
          "framework filter is set to that framework, or to several frameworks including that framework",
        () => {
          const companyName = "CompanyWithFinancial" + companyNameMarker;
          getKeycloakToken(uploader_name, uploader_pw).then((token) => {
            getFirstEuTaxonomyFinancialsDatasetFromFixtures().then((euTaxonomyFinancialsDataset) => {
              return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((storedCompany) => {
                return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                  token,
                  storedCompany.companyId,
                  euTaxonomyFinancialsDataset
                );
              });
            });
          });
          cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
          cy.visit(`/companies?input=${companyName}`)
            .wait("@companies-meta-information")
            .get("td[class='d-bg-white w-3 d-datatable-column-left']")
            .contains(companyName)
            .should("exist");
          cy.visit(`/companies?input=${companyName}&framework=${DataTypeEnum.EutaxonomyFinancials}`)
            .get("td[class='d-bg-white w-3 d-datatable-column-left']")
            .contains(companyName)
            .should("exist");
          cy.visit(`/companies?input=${companyName}&framework=${DataTypeEnum.EutaxonomyNonFinancials}`)
            .get("div[class='col-12 text-left']")
            .should("contain.text", "Sorry! Your search didn't return any results.");
          cy.visit(
            `/companies?input=${companyName}&framework=${DataTypeEnum.EutaxonomyNonFinancials}&framework=${DataTypeEnum.EutaxonomyFinancials}`
          )
            .get("td[class='d-bg-white w-3 d-datatable-column-left']")
            .contains(companyName)
            .should("exist");
        }
      );

      /**
       * Visits the company search page, filters by the specified framework,
       * enters companyNamePrefix into the search bar and ensures that a matching company appears as the first result
       *
       * @param companyNamePrefix the search term to enter
       * @param frameworkToFilterFor the framework to filter by
       */
      function checkFirstAutoCompleteSuggestion(companyNamePrefix: string, frameworkToFilterFor: string): void {
        cy.visit(`/companies?framework=${frameworkToFilterFor}`);
        cy.intercept("**/api/companies*").as("searchCompany");
        verifyTaxonomySearchResultTable();
        cy.get("input[id=search_bar_top]")
          .click({ scrollBehavior: false })
          .type(companyNameMarker, { scrollBehavior: false });
        cy.wait("@searchCompany", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
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

          getKeycloakToken(uploader_name, uploader_pw).then((token) => {
            getFirstEuTaxonomyFinancialsDatasetFromFixtures().then((euTaxonomyFinancialsDataset) => {
              return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyNameFinancial)).then(
                (storedCompany) => {
                  return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                    token,
                    storedCompany.companyId,
                    euTaxonomyFinancialsDataset
                  );
                }
              );
            });
          });
          checkFirstAutoCompleteSuggestion(companyNameFinancialPrefix, DataTypeEnum.EutaxonomyFinancials);

          const companyNameNonFinancialPrefix = "CompanyWithNonFinancial";
          const companyNameNonFinancial = companyNameNonFinancialPrefix + companyNameMarker;

          getKeycloakToken(uploader_name, uploader_pw).then((token) => {
            return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyNameNonFinancial)).then(
              (storedCompany) => {
                return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
                  token,
                  storedCompany.companyId,
                  companiesWithEuTaxonomyDataForNonFinancials[0].t
                );
              }
            );
          });

          checkFirstAutoCompleteSuggestion(companyNameNonFinancialPrefix, DataTypeEnum.EutaxonomyNonFinancials);
        }
      );
    }
  );
});
