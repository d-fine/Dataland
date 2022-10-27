import { describeIf } from "@e2e/support/TestUtility";
import { uploadDummyEuTaxonomyDataForFinancials } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { createCompanyAndGetId } from "@e2e/utils/CompanyUpload";
import { uploadDummyEuTaxonomyDataForNonFinancials } from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { EuTaxonomyDataForNonFinancials } from "@clients/backend";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodes";
import { getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";

let companiesWithEuTaxonomyDataForNonFinancials: Array<FixtureData<EuTaxonomyDataForNonFinancials>>;

before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancials").then(function (jsonContent) {
    companiesWithEuTaxonomyDataForNonFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForNonFinancials>>;
  });
});

describe("As a user, I expect the search functionality on the /companies page to adjust to the selected dropdown filters", () => {
  it("LKSG and SFDR should be displayed in the framework dropdown even though they are not yet implemented", () => {
    cy.ensureLoggedIn();
    cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
    cy.visit("/companies")
      .wait("@companies-meta-information")
      .get("#framework-filter")
      .click()
      .get("div.p-multiselect-panel")
      .find("li.p-disabled:contains('SFDR')")
      .should("exist")
      .get("div.p-multiselect-panel")
      .find("li.p-disabled:contains('LkSG')")
      .should("exist");
  });
  it(
    "Check that the framework filter synchronises between the search bar and the URL",
    { scrollBehavior: false },
    () => {
      cy.ensureLoggedIn();
      cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
      cy.visit(`/companies?framework=eutaxonomy-financials`)
        .wait("@companies-meta-information")
        .get("#framework-filter")
        .click()
        .get("div.p-multiselect-panel")
        .find("li.p-highlight:contains('EU Taxonomy for financial companies')")
        .click()
        .get("div.p-multiselect-panel")
        .find("li:contains('EU Taxonomy for non-financial companies')")
        .click()
        .url()
        .should("include", "/companies?framework=eutaxonomy-non-financials");
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
        .should("contain", `${demoCompanyToTestForCountryName}`)
        .click()
        .get("td[class='d-bg-white w-3 d-datatable-column-left']")
        .contains(demoCompanyToTestFor.companyName)
        .should("exist")
        .url()
        .should("contain", `countryCode=${demoCompanyToTestFor.countryCode}`);
    }
  );
  it("Checks that the sector filter synchronises between the search bar and the drop down and works", () => {
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
      .get("div.p-multiselect-panel")
      .find(`li:contains('${demoCompanyToTestFor.sector}')`)
      .click()
      .get("td[class='d-bg-white w-3 d-datatable-column-left']")
      .contains(demoCompanyToTestFor.companyName)
      .should("exist")
      .url()
      .should("contain", `sector=${demoCompanyToTestFor.sector}`);
  });
  it("Checks that the reset button works as expected", () => {
    const demoCompanyToTestFor = companiesWithEuTaxonomyDataForNonFinancials[0].companyInformation;
    cy.ensureLoggedIn();
    cy.visit(
      `/companies?sector=${demoCompanyToTestFor.sector}&countryCode=${demoCompanyToTestFor.countryCode}&framework=eutaxonomy-non-financials`
    )
      .get("span:contains('RESET')")
      .eq(0)
      .click()
      .url()
      .should("eq", getBaseUrl() + "/companies");
  });
  it(
    "Check that the filter dropdowns close when you scroll down from the top or anywhere in the middle, or when you scroll up",
    { scrollBehavior: false },
    () => {
      cy.ensureLoggedIn();
      cy.intercept("**/api/companies/meta-information").as("companies-meta-information");

      cy.visit("/companies")
        .wait("@companies-meta-information")
        .get("#framework-filter")
        .click()
        .get("div.p-multiselect-panel")
        .should("exist");

      cy.scrollTo(0, 500, { duration: 300 }).get("div.p-multiselect-panel").should("not.exist");
      cy.get("#framework-filter").click().get("div.p-multiselect-panel").should("exist");
      cy.scrollTo(0, 600, { duration: 300 }).get("div.p-multiselect-panel").should("not.exist");
      cy.get("#framework-filter").click().get("div.p-multiselect-panel").should("exist");
      cy.scrollTo(0, 500, { duration: 300 }).get("div.p-multiselect-panel").should("not.exist");
    }
  );
  it(
    "Check that the filter dropdowns close on the resulting query when you check a box while you are not at the top of the page",
    { scrollBehavior: false },
    () => {
      cy.ensureLoggedIn();
      cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
      cy.visit("/companies")
        .wait("@companies-meta-information")
        .get("td[class='d-bg-white w-3 d-datatable-column-left']");
      cy.scrollTo(0, 500, { duration: 300 })
        .get("#framework-filter")
        .click()
        .get("div.p-multiselect-panel")
        .find("li.p-multiselect-item")
        .first()
        .click()
        .get("td[class='d-bg-white w-3 d-datatable-column-left']")
        .should("exist")
        .get("div.p-multiselect-panel")
        .should("not.exist");
    }
  );

  describeIf(
    "As a user, I expect the search results to adjust according to the framework filter",
    {
      executionEnvironments: ["developmentLocal", "development", "development_2"],
      dataEnvironments: ["fakeFixtures"],
    },
    function () {
      beforeEach(function () {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-argument
        cy.ensureLoggedIn(uploader_name, uploader_pw);
      });

      it(
        "Upload a company without uploading framework data for it, assure that its sector does not appear as filter " +
          "option, and check if it neither appears in the autocomplete suggestions nor in the search results, " +
          "even though no framework filter is set.",
        () => {
          const companyName = "ThisCompanyShouldNeverBeFound12349876";
          const sector = "ThisSectorShouldNeverAppearInDropdown";
          createCompanyAndGetId(companyName, sector);
          cy.visit(`/companies`);
          cy.intercept("**/api/companies/meta-information").as("getFilterOptions");
          cy.wait("@getFilterOptions", { timeout: 2 * 1000 }).then(() => {
            cy.get("#sector-filter")
              .click({ scrollBehavior: false })
              .get('input[placeholder="Search sectors"]')
              .type(sector, { scrollBehavior: false })
              .get("li")
              .should("contain", `No results found`);
          });
          cy.intercept("**/api/companies*").as("searchCompany");
          cy.get("input[name=search_bar_top]")
            .click({ force: true })
            .type(companyName, { scrollBehavior: false })
          cy.wait("@searchCompany", { timeout: 2 * 1000 }).then(() => {
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
          createCompanyAndGetId(companyName).then((companyId) => uploadDummyEuTaxonomyDataForFinancials(companyId));
          cy.intercept("**/api/companies/meta-information").as("companies-meta-information");
          cy.visit(`/companies?input=${companyName}`)
            .wait("@companies-meta-information")
            .get("td[class='d-bg-white w-3 d-datatable-column-left']")
            .contains(companyName)
            .should("exist");
          cy.visit(`/companies?input=${companyName}&framework=eutaxonomy-financials`)
            .get("td[class='d-bg-white w-3 d-datatable-column-left']")
            .contains(companyName)
            .should("exist");
          cy.visit(`/companies?input=${companyName}&framework=eutaxonomy-non-financials`)
            .get("div[class='col-12 text-left']")
            .should("contain.text", "Sorry! Your search didn't return any results.");
          cy.visit(
            `/companies?input=${companyName}&framework=eutaxonomy-non-financials&framework=eutaxonomy-financials`
          )
            .get("td[class='d-bg-white w-3 d-datatable-column-left']")
            .contains(companyName)
            .should("exist");
        }
      );

      function checkFirstAutoCompleteSuggestion(companyNamePrefix: string, frameworkToFilterFor: string): void {
        cy.visit(`/companies?framework=${frameworkToFilterFor}`);
        cy.intercept("**/api/companies*").as("searchCompany");
        cy.get("input[name=search_bar_top]").click({ force: true }).type(companyNameMarker);
        cy.wait("@searchCompany", { timeout: 2 * 1000 }).then(() => {
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
          createCompanyAndGetId(companyNameFinancial).then((companyId) =>
            uploadDummyEuTaxonomyDataForFinancials(companyId)
          );
          checkFirstAutoCompleteSuggestion(companyNameFinancialPrefix, "eutaxonomy-financials");

          const companyNameNonFinancialPrefix = "CompanyWithNonFinancial";
          const companyNameNonFinancial = companyNameNonFinancialPrefix + companyNameMarker;
          createCompanyAndGetId(companyNameNonFinancial).then((companyId) =>
            uploadDummyEuTaxonomyDataForNonFinancials(companyId)
          );
          checkFirstAutoCompleteSuggestion(companyNameNonFinancialPrefix, "eutaxonomy-non-financials");
        }
      );
    }
  );
});
