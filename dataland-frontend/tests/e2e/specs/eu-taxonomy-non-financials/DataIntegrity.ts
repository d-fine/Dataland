import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { DataTypeEnum, type EuTaxonomyDataForNonFinancials, type StoredCompany } from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { assertDefined } from "@/utils/TypeScriptUtils";

let euTaxonomyForNonFinancialsFixtureForTest: FixtureData<EuTaxonomyDataForNonFinancials>;
before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancialsPreparedFixtures").then(function (jsonContent) {
    const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForNonFinancials>>;
    euTaxonomyForNonFinancialsFixtureForTest = getPreparedFixture("only-eligible-and-total-numbers", preparedFixtures);
    // "only-eligible-and-total-numbers" should be replaced later with a more suitable fake fixture TODO
    // or manually add field values here like euTaxonomyForNonFinancialsFixtureForTest.t.fieldX = {...}
  });
});

describeIf(
  "As a user, I expect to be able to upload EU taxonomy data for non-financials via the api, and that the uploaded data is displayed " +
    "correctly in the frontend",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    /**
     * validates that the data uploaded via api is displayed correctly for a company
     * @param company the company associated to the data uploaded via form
     * @param dataId the company data id for accessing its view page
     */
    function validateFormUploadedData(company: StoredCompany, dataId: string): void {
      cy.visit(`/companies/${company.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/${dataId}`);
      cy.get("h1").should("contain", company.companyInformation.companyName);
      cy.get('span[data-test="_basicInformation"]').contains("Basic Information").should("exist");
      ["Assurance", "CapEx", "OpEx"].forEach((category) => {
        console.log("category", category);
        cy.get(`span[data-test="${category}"]`).contains(category.toUpperCase()).should("exist");
      });
      cy.get('td > [data-test="fiscalYearEnd"]')
        .parent()
        .next("td")
        .contains(assertDefined(euTaxonomyForNonFinancialsFixtureForTest?.t?.general?.fiscalYearEnd))
        .should("exist");
      cy.get('div > [data-test="CapEx"]').click();
      cy.get('span[data-test="eligibleShare"]').filter(":visible").click();
      cy.get('td > [data-test="relativeShareInPercent"]')
        .parent()
        .next("td")
        .contains(
          assertDefined(
            euTaxonomyForNonFinancialsFixtureForTest?.t?.capex?.eligibleShare?.relativeShareInPercent,
          ).toFixed(2),
        )
        .should("exist");
    }

    it("Create a company via api and upload an EU taxonomy data for non-financials dataset via the api", () => {
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-DataJourney-Form-" + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
          console.log("storedCompany", storedCompany);
          return uploadFrameworkData(
            DataTypeEnum.EutaxonomyNonFinancials,
            token,
            storedCompany.companyId,
            "2021",
            euTaxonomyForNonFinancialsFixtureForTest.t,
          ).then((dataMetaInformation) => {
            cy.intercept("**/api/companies/" + storedCompany.companyId).as("getCompanyInformation");
            cy.visitAndCheckAppMount(
              "/companies/" +
                storedCompany.companyId +
                "/frameworks/" +
                DataTypeEnum.EutaxonomyNonFinancials +
                "/upload?templateDataId=" +
                dataMetaInformation.dataId,
            );
            cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
            cy.get("h1").should("contain", testCompanyName);
            submitButton.clickButton();
            cy.url().should("eq", getBaseUrl() + "/datasets");
            validateFormUploadedData(storedCompany, dataMetaInformation.dataId);
          });
        });
      });
    });
  },
);
