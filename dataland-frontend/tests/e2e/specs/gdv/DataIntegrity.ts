import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type GdvData,
  GdvDataControllerApi,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadGenericFrameworkData } from "@e2e/utils/FrameworkUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { getBaseFrameworkDefinition } from "@/frameworks/BaseFrameworkRegistry";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { compareObjectKeysAndValuesDeep } from "@e2e/utils/GeneralUtils";

let gdvFixtureForTest: FixtureData<GdvData>;
before(function () {
  cy.fixture("CompanyInformationWithGdvPreparedFixtures").then(function (jsonContent) {
    const preparedFixturesGdv = jsonContent as Array<FixtureData<GdvData>>;
    gdvFixtureForTest = getPreparedFixture("Gdv-dataset-with-no-null-fields", preparedFixturesGdv);
  });
});

describeIf(
  "As a user, I expect to be able to edit and submit GDV data via the upload form",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    it(
      "Create a company and a GDV dataset via api, then re-upload it with the upload form in Edit mode and " +
        "assure that the re-uploaded dataset equals the pre-uploaded one",
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = "Company-Created-In-Gdv-Blanket-Test-" + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return uploadGenericFrameworkData(token, storedCompany.companyId, "2021", gdvFixtureForTest.t, (config) =>
              getBaseFrameworkDefinition(DataTypeEnum.Gdv)!.getFrameworkApiClient(config),
            ).then((dataMetaInformation) => {
              cy.intercept(`**/api/data/${DataTypeEnum.Gdv}/${dataMetaInformation.dataId}`).as("fetchDataForPrefill");
              cy.visitAndCheckAppMount(
                "/companies/" +
                  storedCompany.companyId +
                  "/frameworks/" +
                  DataTypeEnum.Gdv +
                  "/upload?templateDataId=" +
                  dataMetaInformation.dataId,
              );
              cy.wait("@fetchDataForPrefill", { timeout: Cypress.env("medium_timeout_in_ms") as number });
              cy.get("h1").should("contain", testCompanyName);
              cy.intercept({
                url: `**/api/data/${DataTypeEnum.Gdv}`,
                times: 1,
              }).as("postCompanyAssociatedData");
              submitButton.clickButton();
              cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
                (postInterception) => {
                  cy.url().should("eq", getBaseUrl() + "/datasets");
                  const dataMetaInformationOfReuploadedDataset = postInterception.response?.body as DataMetaInformation;
                  return new GdvDataControllerApi(new Configuration({ accessToken: token }))
                    .getCompanyAssociatedGdvData(dataMetaInformationOfReuploadedDataset.dataId)
                    .then((axiosResponse) => {
                      const frontendSubmittedGdvDataset = axiosResponse.data.data;

                      compareObjectKeysAndValuesDeep(
                        gdvFixtureForTest.t as Record<string, object>,
                        frontendSubmittedGdvDataset as Record<string, object>,
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
