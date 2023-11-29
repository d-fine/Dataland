import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  P2pDataControllerApi,
  type PathwaysToParisData,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { compareObjectKeysAndValuesDeep, checkToggleEmptyFieldsSwitch } from "@e2e/utils/GeneralUtils";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";

let p2pFixtureForTest: FixtureData<PathwaysToParisData>;
before(function () {
  cy.fixture("CompanyInformationWithP2pPreparedFixtures").then(function (jsonContent) {
    const preparedFixturesP2p = jsonContent as Array<FixtureData<PathwaysToParisData>>;
    p2pFixtureForTest = getPreparedFixture("P2p-dataset-with-no-null-fields", preparedFixturesP2p);
  });
});

describeIf(
  "As a user, I expect to be able to edit and submit P2P data via the upload form",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    it(
      "Create a company and a P2P dataset via api, then re-upload it with the upload form in Edit mode and " +
        "assure that the re-uploaded dataset equals the pre-uploaded one",
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = "Company-Created-In-P2p-Blanket-Test-" + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return uploadFrameworkData(
              DataTypeEnum.P2p,
              token,
              storedCompany.companyId,
              "2021",
              p2pFixtureForTest.t,
            ).then((dataMetaInformation) => {
              cy.intercept("**/api/companies/" + storedCompany.companyId + "/info").as("getCompanyInformation");
              cy.visitAndCheckAppMount(
                "/companies/" +
                  storedCompany.companyId +
                  "/frameworks/" +
                  DataTypeEnum.P2p +
                  "/upload?templateDataId=" +
                  dataMetaInformation.dataId,
              );
              cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
              cy.get("h1").should("contain", testCompanyName);
              cy.intercept({
                url: `**/api/data/${DataTypeEnum.P2p}`,
                times: 1,
              }).as("postCompanyAssociatedData");
              submitButton.clickButton();
              cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
                (postInterception) => {
                  cy.url().should("eq", getBaseUrl() + "/datasets");
                  const dataMetaInformationOfReuploadedDataset = postInterception.response?.body as DataMetaInformation;
                  return new P2pDataControllerApi(new Configuration({ accessToken: token }))
                    .getCompanyAssociatedP2pData(dataMetaInformationOfReuploadedDataset.dataId)
                    .then((axiosGetResponse) => {
                      const frontendSubmittedP2pDataset = axiosGetResponse.data.data as unknown as Record<
                        string,
                        object
                      >;
                      const originallyUploadedP2pDataset = p2pFixtureForTest.t as unknown as Record<string, object>;
                      compareObjectKeysAndValuesDeep(originallyUploadedP2pDataset, frontendSubmittedP2pDataset);
                      cy.get("tr").contains(testCompanyName).click();
                      checkToggleEmptyFieldsSwitch(testCompanyName, "Drive mix per fleet segment");
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
