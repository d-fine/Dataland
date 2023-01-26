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

      function selectCompanyViaUniqueSearchRequest(framework: string, companyName: string): void {
        cy.visit(`/companies?input=${companyName}&framework=${framework}`);
        interceptAndWaitForMetaDataRequest(() => {
          const companySelector = "a span:contains( VIEW)";
          cy.get(companySelector).first().scrollIntoView();
          cy.get(companySelector).first().click({ force: true });
        });
      }

      function selectCompanyViaAutocompleteOnCompaniesPage(framework: string, companyName: string): void {
        cy.visit(`/companies?framework=${framework}`);
        searchCompanyViaLocalSearchBarAndSelect(companyName);
      }

      function searchCompanyViaLocalSearchBarAndSelect(
        companyName: string,
        searchBarSelector = "input#search_bar_top"
      ): void {
        cy.get(searchBarSelector).click();
        cy.get(searchBarSelector).type(companyName, { force: true });
        interceptAndWaitForMetaDataRequest(() => {
          const companySelector = ".p-autocomplete-item";
          cy.get(companySelector).first().scrollIntoView();
          cy.get(companySelector).first().click({ force: true });
        });
      }

      function validateDropdown(expectedDropdownText: string): void {
        cy.get(dropdownSelector).find(".p-dropdown-label").should("have.text", expectedDropdownText);
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

      function dropdownSelect(frameworkToSelect: string): void {
        cy.get(dropdownSelector).click();
        interceptAndWaitForMetaDataRequest(() => {
          cy.get(`${dropdownItemsSelector}:contains(${frameworkToSelect})`).click({ force: true });
        });
      }

      function validateFinancialsPage(): void {
        cy.url().should("contain", `/frameworks/eutaxonomy-financials`);
        cy.get("h2").should("contain", "EU Taxonomy Data");
      }

      function validateNonFinancialsPage(): void {
        cy.url().should("contain", `/frameworks/eutaxonomy-non-financials`);
        cy.get("h2").should("contain", "EU Taxonomy Data");
      }

      function validateLksgPage(): void {
        cy.url().should("contain", `/frameworks/lksg`);
        cy.get("h2").should("contain", "LkSG data");
      }

      function uploadLksgAndFinancialCompany(): void {
        let preparedFixtures: Array<FixtureData<LksgData>>;
        cy.fixture("CompanyInformationWithLksgPreparedFixtures")
          .then(function (jsonContent) {
            preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
          })
          .then(() => {
            const fixture = getPreparedFixture(lksgAndFinancialCompanyName, preparedFixtures);
            return getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
              return uploadCompanyAndLksgDataViaApi(token, fixture.companyInformation, fixture.t).then((uploadIds) => {
                return uploadOneEuTaxonomyFinancialsDatasetViaApi(
                  token,
                  uploadIds.companyId,
                  generateEuTaxonomyDataForFinancials()
                );
              });
            });
          });
      }

      function uploadNonFinancialCompany(): void {
        getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
          const companyInformation = generateCompanyInformation();
          companyInformation.companyName = nonFinancialCompanyName;
          await uploadCompanyViaApi(token, companyInformation).then(async (storedCompany) => {
            const nonFinancialDataSet = generateEuTaxonomyDataForNonFinancials();
            await uploadOneEuTaxonomyNonFinancialsDatasetViaApi(token, storedCompany.companyId, nonFinancialDataSet);
          });
        });
      }

      before(() => {
        uploadLksgAndFinancialCompany();

        uploadNonFinancialCompany();
      });

      it("Check that the redirect depends correctly on the applied filters and the framework select dropdown works as expected", () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        selectCompanyViaAutocompleteOnCompaniesPage("eutaxonomy-financials", lksgAndFinancialCompanyName);
        validateFinancialsPage();
        selectCompanyViaUniqueSearchRequest("eutaxonomy-financials", lksgAndFinancialCompanyName);
        validateFinancialsPage();
        validateDropdown(financialsDropdownItem);
        dropdownSelect(lksgDropdownItem);
        validateLksgPage();
        validateDropdown(lksgDropdownItem);
        dropdownSelect(financialsDropdownItem);
        validateFinancialsPage();

        selectCompanyViaAutocompleteOnCompaniesPage("lksg", lksgAndFinancialCompanyName);
        validateLksgPage();
        selectCompanyViaUniqueSearchRequest("lksg", lksgAndFinancialCompanyName);
        validateLksgPage();
        validateDropdown(lksgDropdownItem);
      });

      it("Check that from a framework page you can search a company without this framework", () => {
        cy.ensureLoggedIn();
        selectCompanyViaUniqueSearchRequest("lksg", lksgAndFinancialCompanyName);
        validateLksgPage();
        searchCompanyViaLocalSearchBarAndSelect(nonFinancialCompanyName, "input#framework_data_search_bar_standard");
        validateNonFinancialsPage();
      });
    }
  );
});
