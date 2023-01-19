import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { LksgData } from "@clients/backend";
import { uploadOneEuTaxonomyFinancialsDatasetViaApi } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { generateEuTaxonomyDataForFinancials } from "@e2e/fixtures/eutaxonomy/financials/EuTaxonomyDataForFinancialsFixtures";
import { getPreparedLksgFixture, uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgApiUtils";

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
    const financialsDropdownItem = "EU Taxonomy Financials";
    const lksgDropdownItem = "LkSG";
    let companyId: string;

    function selectCompanyViaUniqueSearchRequest(framework: string): void {
      cy.visit(`/companies?input=${companyName}&framework=${framework}`);
      const alias = "retrieveMetaData";
      cy.intercept("**/api/metadata**").as(alias);
      cy.get("a span:contains( VIEW)").first().click();
      cy.wait(`@${alias}`);
    }

    function selectCompanyViaAutocompleteClick(framework: string): void {
      cy.visit(`/companies?framework=${framework}`);
      const searchBarSelector = "input#search_bar_top";
      cy.get(searchBarSelector).click();
      cy.get(searchBarSelector).type(companyName, { force: true });
      const alias = "retrieveMetaData";
      cy.intercept("**/api/metadata**").as(alias);
      cy.get(".p-autocomplete-item").first().click();
      cy.wait(`@${alias}`);
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
      cy.get(`${dropdownItemsSelector}:contains(${frameworkToSelect})`).click({ force: true });
    }

    function validateFinancialsPage(): void {
      cy.url().should("contain", `${companyId}/frameworks/eutaxonomy-financials`); // TODO remove companyId
      cy.get("h2").should("contain", "EU Taxonomy Data");
    }

    function validateLksgPage(): void {
      cy.url().should("contain", `${companyId}/frameworks/lksg`);  // TODO remove companyId
      cy.get("h2").should("contain", "LkSG data");
    }

    it("Upload an lksg company and an additional financials data set", () => { // TODO put it into before
      let preparedFixtures: Array<FixtureData<LksgData>>;
      cy.fixture("CompanyInformationWithLksgPreparedFixtures")
        .then(function (jsonContent) {
          preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
        })
        .then(() => {
          const fixture = getPreparedLksgFixture(companyName, preparedFixtures);
          return getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
            return uploadCompanyAndLksgDataViaApi(token, fixture.companyInformation, fixture.t).then((uploadIds) => {
            companyId = uploadIds.companyId;
            Cypress.env(companyName, companyId);
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
      companyId = Cypress.env(companyName) as string;

      cy.ensureLoggedIn(uploader_name, uploader_pw);

      selectCompanyViaAutocompleteClick("eutaxonomy-financials");
      validateFinancialsPage();
      selectCompanyViaUniqueSearchRequest("eutaxonomy-financials");
      validateFinancialsPage();
      validateDropdown("EU Taxonomy for financial companies"); // TODO variables instead of strings
      dropdownSelect(lksgDropdownItem);
      validateLksgPage();
      validateDropdown("LkSG");
      dropdownSelect(financialsDropdownItem);
      validateFinancialsPage();

      selectCompanyViaAutocompleteClick("lksg");
      validateLksgPage();
      selectCompanyViaUniqueSearchRequest("lksg");
      validateLksgPage();
      validateDropdown("LkSG");
    });
  }
);
