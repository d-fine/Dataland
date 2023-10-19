import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  type CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type EuTaxonomyDataForNonFinancials,
  EuTaxonomyDataForNonFinancialsControllerApi,
  type StoredCompany,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { roundNumber } from "@/utils/NumberConversionUtils";
import { compareObjectKeysAndValuesDeep } from "@e2e/utils/GeneralUtils";

let euTaxonomyForNonFinancialsFixtureForTest: FixtureData<EuTaxonomyDataForNonFinancials>;
before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancialsPreparedFixtures").then(function (jsonContent) {
    const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForNonFinancials>>;
    euTaxonomyForNonFinancialsFixtureForTest = getPreparedFixture(
      "all-fields-defined-for-eu-taxo-non-financials",
      preparedFixtures,
    );
  });
});

describeIf(
  "As a user, I expect to be able to upload EU taxonomy data for non-financials via the api, and that the uploaded data is displayed " +
    "correctly in the frontend",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    /**
     * validates that the data uploaded via api is displayed correctly for a company
     * @param company the company associated to the data uploaded via form
     * @param dataId the company data id for accessing its view page
     */
    function validateSomeValuesForTheReuploadedDataset(company: StoredCompany, dataId: string): void {
      cy.visit(`/companies/${company.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}/${dataId}`);
      cy.get("h1").should("contain", company.companyInformation.companyName);
      cy.get('span[data-test="_general"]').contains("General").should("exist");
      ["Assurance", "CapEx", "OpEx"].forEach((category) => {
        cy.get(`span[data-test="${category}"]`).contains(category.toUpperCase()).should("exist");
      });
      cy.get('td > [data-test="fiscalYearEnd"]')
        .parent()
        .next("td")
        .contains(assertDefined(euTaxonomyForNonFinancialsFixtureForTest?.t?.general?.fiscalYearEnd))
        .should("exist");
      cy.get('div > [data-test="CapEx"]').click();
      cy.get('td > [data-test="relativeShareInPercent"]')
        .parent()
        .next("td")
        .contains(
          roundNumber(
            assertDefined(euTaxonomyForNonFinancialsFixtureForTest?.t?.capex?.eligibleShare?.relativeShareInPercent),
            2,
          ),
        )
        .should("exist");
    }

    it(
      "Create a company and an EU taxonomy for non-financials dataset via api, then re-upload it with the " +
        "upload form in Edit mode and assure that it worked by validating a couple of values",
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = "Company-Created-In-DataJourney-Form-" + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return uploadFrameworkData(
              DataTypeEnum.EutaxonomyNonFinancials,
              token,
              storedCompany.companyId,
              "2021",
              euTaxonomyForNonFinancialsFixtureForTest.t,
            ).then((dataMetaInformation) => {
              let dataSetFromPrefillRequest: EuTaxonomyDataForNonFinancials;
              cy.ensureLoggedIn(admin_name, admin_pw);
              cy.intercept({
                url: `api/data/${dataMetaInformation.dataType}/${dataMetaInformation.dataId}`,
                times: 1,
              }).as("getDataToPrefillForm");
              cy.visitAndCheckAppMount(
                "/companies/" +
                  storedCompany.companyId +
                  "/frameworks/" +
                  DataTypeEnum.EutaxonomyNonFinancials +
                  "/upload?templateDataId=" +
                  dataMetaInformation.dataId,
              );
              cy.wait("@getDataToPrefillForm", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
                (interception) => {
                  dataSetFromPrefillRequest = (
                    interception.response?.body as CompanyAssociatedDataEuTaxonomyDataForNonFinancials
                  ).data;
                },
              );
              cy.get("h1").should("contain", testCompanyName);
              cy.intercept({
                url: `**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}`,
                times: 1,
              }).as("postCompanyAssociatedData");
              submitButton.clickButton();
              cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
                (interception) => {
                  cy.url().should("eq", getBaseUrl() + "/datasets");
                  const dataMetaInformationOfReuploadedDataset = interception.response?.body as DataMetaInformation;
                  return new EuTaxonomyDataForNonFinancialsControllerApi(new Configuration({ accessToken: token }))
                    .getCompanyAssociatedEuTaxonomyDataForNonFinancials(dataMetaInformationOfReuploadedDataset.dataId)
                    .then((axiosResponse) => {
                      const reuploadedDatasetFromBackend = axiosResponse.data.data;
                      compareObjectKeysAndValuesDeep(
                        dataSetFromPrefillRequest as Record<string, object>,
                        reuploadedDatasetFromBackend as Record<string, object>,
                      );
                      validateSomeValuesForTheReuploadedDataset(
                        storedCompany,
                        dataMetaInformationOfReuploadedDataset.dataId,
                      );
                    });
                },
              );
            });
          });
        });
      },
    );
  },
);
