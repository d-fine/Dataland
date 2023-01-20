import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { LksgData } from "@clients/backend";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { generateEuTaxonomyDataForFinancials } from "@e2e/fixtures/eutaxonomy/financials/EuTaxonomyDataForFinancialsFixtures";
import { uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgApiUtils";
import { MEDIUM_TIMEOUT_IN_MS } from "../../utils/Constants";
import { getPreparedFixture } from "../../utils/GeneralApiUtils";

describe("The shared header of the framework pages should act as expected", { scrollBehavior: false }, () => {
  describeIf(
    "As a user, I expect the framework selection dropdown to work correctly " +
      "to make it possible to switch between framework views",
    {
      executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
      dataEnvironments: ["fakeFixtures"],
    },
    function (): void {
      const companyName = "two-different-data-set-types";
      const dropdownSelector = "div#frameworkDataDropdown";
      const dropdownItemsSelector = "div.p-dropdown-items-wrapper li";
      const financialsDropdownItem = "EU Taxonomy for financial companies";
      const lksgDropdownItem = "LkSG";

      function interceptFrameworkPageLoad(trigger: () => void): void {
        const metaDataAlias = "retrieveMetaData";
        cy.intercept("**/api/metadata**").as(metaDataAlias);
        trigger();
        cy.wait(`@${metaDataAlias}`, { timeout: MEDIUM_TIMEOUT_IN_MS });
        cy.wait(3000);
      }

      function selectCompanyViaUniqueSearchRequest(framework: string): void {
        cy.visit(`/companies?input=${companyName}&framework=${framework}`);
        interceptFrameworkPageLoad(() => {
          const companySelector = "a span:contains( VIEW)";
          cy.get(companySelector).first().scrollIntoView();
          cy.get(companySelector).first().click({ force: true });
        });
      }

      function selectCompanyViaAutocompleteClick(framework: string): void {
        cy.visit(`/companies?framework=${framework}`);
        const searchBarSelector = "input#search_bar_top";
        cy.get(searchBarSelector).click();
        cy.get(searchBarSelector).type(companyName, { force: true });
        interceptFrameworkPageLoad(() => {
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
        interceptFrameworkPageLoad(() => {
          cy.get(`${dropdownItemsSelector}:contains(${frameworkToSelect})`).click({ force: true });
        });
      }

      function validateFinancialsPage(): void {
        cy.url().should("contain", `/frameworks/eutaxonomy-financials`);
        cy.get("h2").should("contain", "EU Taxonomy Data");
      }

      function validateLksgPage(): void {
        cy.url().should("contain", `/frameworks/lksg`);
        cy.get("h2").should("contain", "LkSG data");
      }

      before(() => {
        let preparedFixtures: Array<FixtureData<LksgData>>;
        cy.fixture("CompanyInformationWithLksgPreparedFixtures")
          .then(function (jsonContent) {
            preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
          })
          .then(() => {
            const fixture = getPreparedFixture(companyName, preparedFixtures);
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
      });

      it("Check that the redirect depends correctly on the applied filters and the framework select dropdown works as expected", () => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);

        selectCompanyViaAutocompleteClick("eutaxonomy-financials");
        validateFinancialsPage();
        selectCompanyViaUniqueSearchRequest("eutaxonomy-financials");
        validateFinancialsPage();
        validateDropdown(financialsDropdownItem);
        dropdownSelect(lksgDropdownItem);
        validateLksgPage();
        validateDropdown(lksgDropdownItem);
        dropdownSelect(financialsDropdownItem);
        validateFinancialsPage();

        selectCompanyViaAutocompleteClick("lksg");
        validateLksgPage();
        selectCompanyViaUniqueSearchRequest("lksg");
        validateLksgPage();
        validateDropdown(lksgDropdownItem);
      });
    }
  );
});
