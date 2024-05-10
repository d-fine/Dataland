import { describeIf } from "@e2e/support/TestUtility";
import { getFirstEuTaxonomyFinancialsFixtureDataFromFixtures } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { DataTypeEnum, type EuTaxonomyDataForFinancials, type SfdrData } from "@clients/backend";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodeConverter";
import { admin_name, admin_pw, getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { verifySearchResultTableExists } from "@sharedUtils/ElementChecks";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { convertStringToQueryParamFormat } from "@e2e/utils/Converters";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { uploadCompanyAndFrameworkData, uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";
let companiesWithEuTaxonomyDataForFinancials: Array<FixtureData<EuTaxonomyDataForFinancials>>;
let companiesWithSfdrData: Array<FixtureData<SfdrData>>;
before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (jsonContent) {
    companiesWithEuTaxonomyDataForFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
  });
  cy.fixture("CompanyInformationWithSfdrData").then(function (jsonContent) {
    companiesWithSfdrData = jsonContent as Array<FixtureData<SfdrData>>;
  });
});

/**
 * Function which escapes parenthesis for regex expression
 * @param inputString string for which parenthesis should be escaped
 * @returns inputString the string without parenthesis
 */
function escapeParenthesisInRegExp(inputString: string): string {
  return inputString.replace(/[()]/g, "\\$&");
}
describe("As a user, I expect the search functionality on the /companies page to adjust to the selected dropdown filters", () => {
  const failureMessageOnAvailableDatasetsPage = "We're sorry, but your search did not return any results.";
  it("The framework filter synchronise between the search bar and the URL", { scrollBehavior: false }, () => {
    cy.ensureLoggedIn();
    cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
    cy.visit("/companies").wait("@companies-meta-information");
    verifySearchResultTableExists();
    cy.url().should("eq", getBaseUrl() + "/companies");
    cy.get("#framework-filter")
      .click()
      .get("div.p-multiselect-panel")
      .find(`li.p-multiselect-item:contains(${humanizeStringOrNumber(DataTypeEnum.EutaxonomyFinancials)})`)
      .click();
    verifySearchResultTableExists();
    cy.url()
      .should("eq", getBaseUrl() + "/companies?" + `framework=${DataTypeEnum.EutaxonomyFinancials}`)
      .get("div.p-multiselect-panel")
      .find(`li.p-multiselect-item:contains(${humanizeStringOrNumber(DataTypeEnum.P2p)})`)
      .click();
    verifySearchResultTableExists();
    cy.get(".p-multiselect-items-wrapper").scrollTo("bottom");
    cy.get("div.p-multiselect-panel")
      .find(`li.p-multiselect-item:contains(${humanizeStringOrNumber(DataTypeEnum.Sfdr)})`)
      .click();
    verifySearchResultTableExists();
    cy.url()
      .should(
        "eq",
        getBaseUrl() +
          "/companies?" +
          `framework=${DataTypeEnum.EutaxonomyFinancials}` +
          `&framework=${DataTypeEnum.P2p}` +
          `&framework=${DataTypeEnum.Sfdr}`,
      )
      .get("div.p-multiselect-panel")
      .find(`li.p-highlight:contains(${humanizeStringOrNumber(DataTypeEnum.P2p)})`)
      .click();
    cy.url().should(
      "eq",
      getBaseUrl() +
        "/companies?" +
        `framework=${DataTypeEnum.EutaxonomyFinancials}` +
        `&framework=${DataTypeEnum.Sfdr}`,
    );
  });

  describeIf(
    "",
    {
      executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      onlyExecuteOnDatabaseReset: true,
    },
    () => {
      it(
        "Checks that the country-code filter synchronises between the search bar and the drop down and works",
        { scrollBehavior: false },
        () => {
          const demoCompanyToTestFor = companiesWithEuTaxonomyDataForFinancials[0].companyInformation;
          const demoCompanyWithDifferentCountryCode = companiesWithEuTaxonomyDataForFinancials.find(
            (it) => it.companyInformation.countryCode !== demoCompanyToTestFor.countryCode,
          )!.companyInformation;

          const demoCompanyToTestForCountryName = assertDefined(
            getCountryNameFromCountryCode(demoCompanyToTestFor.countryCode),
          );
          cy.ensureLoggedIn();
          cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
          cy.visit(
            `/companies?input=${demoCompanyToTestFor.companyName}&countryCode=${demoCompanyWithDifferentCountryCode.countryCode}`,
          )
            .wait("@companies-meta-information")
            .get("div[class='col-12 text-left']")
            .should("contain.text", failureMessageOnAvailableDatasetsPage)
            .get("#country-filter")
            .click()
            .get('input[placeholder="Search countries"]')
            .type(`${demoCompanyToTestForCountryName}`)
            .get("li")
            .contains(RegExp(`^${escapeParenthesisInRegExp(demoCompanyToTestForCountryName)}$`))
            .click()
            .get("td[class='d-bg-white w-3 d-datatable-column-left']")
            .contains(demoCompanyToTestFor.companyName)
            .should("exist")
            .url()
            .should("contain", `countryCode=${convertStringToQueryParamFormat(demoCompanyToTestFor.countryCode)}`);
        },
      );

      it(
        "Checks that the sector filter synchronises between the search bar and the drop down and works",
        { scrollBehavior: false },
        () => {
          const demoCompanyToTestFor = assertDefined(
            companiesWithEuTaxonomyDataForFinancials.find((it) => it.companyInformation?.sector)?.companyInformation,
          );
          expect(demoCompanyToTestFor?.sector).to.not.be.undefined;

          const demoCompanyWithDifferentSector = assertDefined(
            companiesWithEuTaxonomyDataForFinancials.find(
              (it) => it.companyInformation?.sector !== demoCompanyToTestFor.sector && it.companyInformation?.sector,
            )?.companyInformation,
          );
          expect(demoCompanyWithDifferentSector?.sector).to.not.be.undefined;

          cy.ensureLoggedIn();
          cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
          cy.visit(
            `/companies?input=${demoCompanyToTestFor.companyName}&sector=${demoCompanyWithDifferentSector.sector!}`,
          )
            .wait("@companies-meta-information")
            .get("div[class='col-12 text-left']")
            .should("contain.text", failureMessageOnAvailableDatasetsPage)
            .get("#sector-filter")
            .click()
            .get('input[placeholder="Search sectors"]')
            .type(`${demoCompanyToTestFor.sector!}`)
            .get("li")
            .contains(RegExp(`^${demoCompanyToTestFor.sector!}$`))
            .click()
            .get("td[class='d-bg-white w-3 d-datatable-column-left']")
            .contains(demoCompanyToTestFor.companyName)
            .should("exist");
          cy.url().should("contain", `sector=${convertStringToQueryParamFormat(demoCompanyToTestFor.sector!)}`);
        },
      );
    },
  );
  it("Checks that the reset button works as expected", { scrollBehavior: false }, () => {
    cy.ensureLoggedIn();
    cy.visit(`/companies?sector=dummy&countryCode=dummy&framework=${DataTypeEnum.EutaxonomyFinancials}`);
    cy.get("span:contains('RESET')")
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
      verifySearchResultTableExists();
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
      verifySearchResultTableExists();
      cy.get("div.p-multiselect-panel").should("not.exist");
    },
  );

  describeIf(
    "As a user, I expect the search results to adjust according to the framework filter",
    {
      executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    },
    function () {
      beforeEach(function () {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
      });

      const companyNameMarker = "Data987654321";
      it(
        "Upload a company without uploading framework data for it, assure that its sector appears as filter " +
          "option, and check that the company appears in the autocomplete suggestions and in the " +
          "search results, if no framework filter is set.",
        () => {
          const preFix = "ThisCompanyHasNoDataSet";
          const companyName = preFix + companyNameMarker;
          const sector = "SectorWithNoDataSet";
          getKeycloakToken(uploader_name, uploader_pw).then((token) => {
            return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName, sector));
          });
          cy.intercept({ url: "**/api/companies*", times: 1 }).as("searchCompanyInitial");
          cy.visit(`/companies`).wait("@searchCompanyInitial");
          verifySearchResultTableExists();
          cy.intercept({ url: `**/api/companies/names?searchString=${companyNameMarker}*`, times: 1 }).as(
            "searchCompanyInput",
          );
          cy.get("input[id=search_bar_top]")
            .click({ scrollBehavior: false })
            .type(companyNameMarker, { scrollBehavior: false });
          cy.wait("@searchCompanyInput", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
            cy.get(".p-autocomplete-item").eq(0).get("span[class='font-normal']").contains(preFix).should("exist");
          });
        },
      );
      it(
        "Upload a company without uploading framework data for it, assure that its sector does not appear as filter " +
          "option, and check if the company neither appears in the autocomplete suggestions nor in the " +
          "search results, if at least one framework filter is set.",
        () => {
          const companyName = "ThisCompanyShouldNeverBeFound12349876";
          const sector = "ThisSectorShouldNeverAppearInDropdown";
          getKeycloakToken(uploader_name, uploader_pw).then((token) => {
            return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName, sector));
          });
          cy.visit(`/companies`);
          cy.intercept("**/api/companies/meta-information").as("getFilterOptions");
          cy.get("#framework-filter")
            .click()
            .get("div.p-multiselect-panel")
            .find(`li.p-multiselect-item:contains(${humanizeStringOrNumber(DataTypeEnum.Lksg)})`)
            .click();
          verifySearchResultTableExists();
          cy.wait("@getFilterOptions", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(() => {
            verifySearchResultTableExists();
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
            const timeInMillisecondsToAllowPotentialDropdownToAppearIfThereAreMatches = 1000;
            cy.wait(timeInMillisecondsToAllowPotentialDropdownToAppearIfThereAreMatches);
            cy.get(".p-autocomplete-item").should("not.exist");
          });
          cy.visit(`/companies?input=${companyName}`);
          cy.get("#framework-filter")
            .click()
            .get("div.p-multiselect-panel")
            .find(`li.p-multiselect-item:contains(${humanizeStringOrNumber(DataTypeEnum.Lksg)})`)
            .click();
          cy.get("div[class='col-12 text-left']").should("contain.text", failureMessageOnAvailableDatasetsPage);
        },
      );

      it(
        "Upload a company with Eu Taxonomy Data For Financials and check if it only appears in the results if the " +
          "framework filter is set to that framework, or to several frameworks including that framework",
        () => {
          const companyName = "CompanyWithFinancial" + companyNameMarker;
          getKeycloakToken(admin_name, admin_pw).then((token) => {
            getFirstEuTaxonomyFinancialsFixtureDataFromFixtures().then((fixtureData) => {
              return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((storedCompany) => {
                return uploadFrameworkData(
                  DataTypeEnum.EutaxonomyFinancials,
                  token,
                  storedCompany.companyId,
                  fixtureData.reportingPeriod,
                  fixtureData.t,
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
          cy.visit(`/companies?input=${companyName}&framework=${DataTypeEnum.Sfdr}`)
            .get("div[class='col-12 text-left']")
            .should("contain.text", failureMessageOnAvailableDatasetsPage);
          cy.visit(
            `/companies?input=${companyName}&framework=${DataTypeEnum.Sfdr}&framework=${DataTypeEnum.EutaxonomyFinancials}`,
          )
            .get("td[class='d-bg-white w-3 d-datatable-column-left']")
            .contains(companyName)
            .should("exist");
        },
      );

      /**
       * Visits the company search page, filters by the specified framework,
       * enters companyNamePrefix into the search bar and ensures that a matching company appears as the first result
       * @param companyNamePrefix the search term to enter
       * @param frameworkToFilterFor the framework to filter by
       */
      function checkFirstAutoCompleteSuggestion(companyNamePrefix: string, frameworkToFilterFor: string): void {
        cy.intercept({ url: "**/api/companies*", times: 1 }).as("searchCompanyInitial");
        cy.visit(`/companies?framework=${frameworkToFilterFor}`).wait("@searchCompanyInitial");
        verifySearchResultTableExists();
        cy.intercept({ url: `**searchString=${companyNameMarker}*`, times: 1 }).as(
          `searchCompanyInput_${frameworkToFilterFor}`,
        );
        cy.get("input[id=search_bar_top]")
          .click({ scrollBehavior: false })
          .type(companyNameMarker, { scrollBehavior: false });
        cy.wait(`@searchCompanyInput_${frameworkToFilterFor}`).then(() => {
          cy.get(".p-autocomplete-item")
            .eq(0)
            .get("span[class='font-normal']")
            .contains(companyNamePrefix)
            .should("exist");
        });
      }

      it(
        "Upload a company with Eu Taxonomy Data For Financials and one with SFDR and " +
          "check if they are displayed in the autocomplete dropdown only if the framework filter is set accordingly",
        () => {
          const companyNameFinancialPrefix = "CompanyWithFinancial";
          const companyNameFinancial = companyNameFinancialPrefix + companyNameMarker;

          getKeycloakToken(admin_name, admin_pw).then((token) => {
            getFirstEuTaxonomyFinancialsFixtureDataFromFixtures().then((fixtureData) => {
              return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyNameFinancial)).then(
                (storedCompany) => {
                  return uploadFrameworkData(
                    DataTypeEnum.EutaxonomyFinancials,
                    token,
                    storedCompany.companyId,
                    fixtureData.reportingPeriod,
                    fixtureData.t,
                  );
                },
              );
            });
          });
          checkFirstAutoCompleteSuggestion(companyNameFinancialPrefix, DataTypeEnum.EutaxonomyFinancials);

          const companyNameSfdrPrefix = "CompanyWithSfdr";
          const companyNameSfdr = companyNameSfdrPrefix + companyNameMarker;

          getKeycloakToken(admin_name, admin_pw).then((token) => {
            const sfdrFixture = companiesWithSfdrData[0];
            void uploadCompanyAndFrameworkData(
              DataTypeEnum.Sfdr,
              token,
              generateDummyCompanyInformation(companyNameSfdr),
              sfdrFixture.t,
              sfdrFixture.reportingPeriod,
            );
          });
          checkFirstAutoCompleteSuggestion(companyNameSfdrPrefix, DataTypeEnum.Sme);
        },
      );
    },
  );
});
