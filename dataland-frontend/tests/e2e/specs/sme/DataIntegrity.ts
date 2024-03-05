import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  SmeDataControllerApi,
  type SmeData,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { compareObjectKeysAndValuesDeep } from "@e2e/utils/GeneralUtils";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

let smeFixtureForTest: FixtureData<SmeData>;
before(function () {
  cy.fixture("CompanyInformationWithSmePreparedFixtures").then(function (jsonContent) {
    const preparedFixturesSme = jsonContent as Array<FixtureData<SmeData>>;
    smeFixtureForTest = getPreparedFixture("Sme-dataset-with-no-null-fields", preparedFixturesSme);
  });
});

describeIf(
  "As a user, I expect to be able to edit and submit Sme data via the upload form",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    it(
      "Create a company and a Sme dataset via api, then re-upload it with the upload form in Edit mode and " +
        "assure that the re-uploaded dataset equals the pre-uploaded one",
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = "Company-Created-In-Sme-Blanket-Test-" + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return uploadFrameworkData(
              DataTypeEnum.Sme,
              token,
              storedCompany.companyId,
              "2021",
              smeFixtureForTest.t,
            ).then((dataMetaInformation) => {
              cy.intercept("**/api/companies/" + storedCompany.companyId + "/info").as("getCompanyInformation");
              cy.visitAndCheckAppMount(
                "/companies/" +
                  storedCompany.companyId +
                  "/frameworks/" +
                  DataTypeEnum.Sme +
                  "/upload?templateDataId=" +
                  dataMetaInformation.dataId,
              );
              cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
              cy.get("h1").should("contain", testCompanyName);
              cy.intercept({
                url: `**/api/data/${DataTypeEnum.Sme}`,
                times: 1,
              }).as("postCompanyAssociatedData");
              submitButton.clickButton();
              cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
                (postInterception) => {
                  cy.url().should("eq", getBaseUrl() + "/datasets");
                  const dataMetaInformationOfReuploadedDataset = postInterception.response?.body as DataMetaInformation;
                  return new SmeDataControllerApi(new Configuration({ accessToken: token }))
                    .getCompanyAssociatedSmeData(dataMetaInformationOfReuploadedDataset.dataId)
                    .then((axiosGetResponse) => {
                      const frontendSubmittedSmeDataset = axiosGetResponse.data.data as unknown as Record<
                        string,
                        object
                      >;
                      const originallyUploadedSmeDataset = smeFixtureForTest.t as unknown as Record<string, object>;
                      compareObjectKeysAndValuesDeep(originallyUploadedSmeDataset, frontendSubmittedSmeDataset);
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
