import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsControllerApi,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { compareObjectKeysAndValuesDeep, checkToggleEmptyFieldsSwitch } from "@e2e/utils/GeneralUtils";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

let euTaxonomyFinancialsFixtureForTest: FixtureData<EuTaxonomyDataForFinancials>;
before(function () {
  cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
    const preparedFixturesEuTaxonomyFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
    euTaxonomyFinancialsFixtureForTest = getPreparedFixture(
      "company-for-all-types",
      preparedFixturesEuTaxonomyFinancials,
    );
  });
});

describeIf(
  "As a user, I expect to be able to edit and submit Eu Taxonomy Financials data via the upload form",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    it(
      "Create a company and a Eu Taxonomy Financials dataset via api, then re-upload it with the upload form in Edit mode and " +
        "assure that the re-uploaded dataset equals the pre-uploaded one",
      () => {
        const testCompanyName = "Company-Created-In-Eu-Taxonomy-Financials-Blanket-Test-Company";
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return uploadFrameworkData(
              DataTypeEnum.EutaxonomyFinancials,
              token,
              storedCompany.companyId,
              "2023",
              euTaxonomyFinancialsFixtureForTest.t,
              true,
            ).then((dataMetaInformation) => {
              cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyFinancials}/${dataMetaInformation.dataId}`).as(
                "fetchDataForPrefill",
              );
              cy.visitAndCheckAppMount(
                "/companies/" +
                  storedCompany.companyId +
                  "/frameworks/" +
                  DataTypeEnum.EutaxonomyFinancials +
                  "/upload?templateDataId=" +
                  dataMetaInformation.dataId,
              );
              cy.wait("@fetchDataForPrefill", { timeout: Cypress.env("medium_timeout_in_ms") as number });
              cy.get("h1").should("contain", testCompanyName);
              cy.intercept({
                url: `**/api/data/${DataTypeEnum.EutaxonomyFinancials}`,
                times: 1,
              }).as("postCompanyAssociatedData");
              submitButton.clickButton();
              cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
                (postInterception) => {
                  cy.url().should("eq", getBaseUrl() + "/datasets");
                  const dataMetaInformationOfReuploadedDataset = postInterception.response?.body as DataMetaInformation;
                  return new EuTaxonomyDataForFinancialsControllerApi(new Configuration({ accessToken: token }))
                    .getCompanyAssociatedEuTaxonomyDataForFinancials(dataMetaInformationOfReuploadedDataset.dataId)
                    .then((axiosResponse) => {
                      const frontendSubmittedEuTaxonomyFinancialsDataset = axiosResponse.data
                        .data as unknown as EuTaxonomyDataForFinancials;

                      frontendSubmittedEuTaxonomyFinancialsDataset.financialServicesTypes?.sort();
                      euTaxonomyFinancialsFixtureForTest.t.financialServicesTypes?.sort();

                      compareObjectKeysAndValuesDeep(
                        euTaxonomyFinancialsFixtureForTest.t as unknown as Record<string, object>,
                        frontendSubmittedEuTaxonomyFinancialsDataset as Record<string, object>,
                      );
                      checkToggleEmptyFieldsSwitch("Mon, 27 Nov 2023, 09:43", "Scope Of Entities");
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
