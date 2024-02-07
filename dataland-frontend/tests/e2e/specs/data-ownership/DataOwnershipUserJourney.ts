import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, reader_name, reader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken, login, logout } from "@e2e/utils/Auth";
import {
  CompanyDataControllerApi,
  type CompanyDataOwners,
  Configuration,
  DataTypeEnum,
  type PathwaysToParisData,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM } from "@/utils/Constants";

let p2pFixtureForTest: FixtureData<PathwaysToParisData>;
before(function () {
  cy.fixture("CompanyInformationWithP2pPreparedFixtures").then(function (jsonContent) {
    const preparedFixturesP2p = jsonContent as Array<FixtureData<PathwaysToParisData>>;
    p2pFixtureForTest = getPreparedFixture("P2p-dataset-with-no-null-fields", preparedFixturesP2p);
  });
});

describeIf(
  "As a user, I expect to be able to upload data for one company for which iam data owner",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    it("Upload a company, set a user as the data owner and then verify that the upload pages are displayed for that user", () => {
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-Data-Owner-Test-" + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
          return uploadFrameworkData(
            DataTypeEnum.P2p,
            token,
            storedCompany.companyId,
            "2021",
            p2pFixtureForTest.t,
          ).then(() => {
            postDataOwner(token, "18b67ecc-1176-4506-8414-1e81661017ca", storedCompany.companyId).then(() => {});
            logout();
            login(reader_name, reader_pw);
            cy.intercept("**/api/companies/" + storedCompany.companyId + "/info").as("getCompanyInformation");
            cy.visitAndCheckAppMount("/companies/" + storedCompany.companyId);
            cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
            cy.get("h1").should("contain", testCompanyName);
            Object.entries(ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM.entries()).forEach(([frameworkName]) => {
              const frameworkSummaryPanelSelector = `div[data-test="${frameworkName}-summary-panel"]`;
              cy.get(frameworkSummaryPanelSelector).should("exist");
              cy.get(`${frameworkSummaryPanelSelector} a[data-test="${frameworkName}-provide-data-button"]`).should(
                "exist",
              );
            });
            cy.get(`div[data-test="lksg-summary-panel"]`).should("exist").click();

            cy.get(`div[data-pc-section="title"]`).should("contain", "New Dataset - LkSG");
          });
        });
      });
    });
  },
);

/**
 * Method that sets a user as a data owner of the specified company
 * @param token authentication token of the user doing the post request
 * @param userId of the user that should be set as a data owner
 * @param companyId of the company for which the user should be set as a data owner
 */
export async function postDataOwner(token: string, userId: string, companyId: string): Promise<CompanyDataOwners> {
  const apiResponse = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).postDataOwner(
    companyId,
    userId,
  );
  return apiResponse.data;
}
