import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, reader_name, reader_pw, reader_userId } from "@e2e/utils/Cypress";
import { getKeycloakToken, login, logout } from "@e2e/utils/Auth";
import { CompanyDataControllerApi, type CompanyDataOwners, Configuration } from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM } from "@/utils/Constants";

describeIf(
  "As a user, I expect to be able to upload data for one company for which I am data owner",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  () => {
    /**
     * This method verifies that the summary panel is presented as expected
     */
    function checkFrameworks(){
      ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM.forEach((frameworkName) => {
        const frameworkSummaryPanelSelector = `div[data-test="${frameworkName}-summary-panel"]`;
        cy.get(frameworkSummaryPanelSelector).should("exist");
        cy.get(`${frameworkSummaryPanelSelector} a[data-test="${frameworkName}-provide-data-button"]`).should(
            "exist",
        );
      });
    }
    it("Upload a company, set a user as the data owner and then verify that the upload pages are displayed for that user", () => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-Data-Owner-Test-" + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
          cy.intercept("**/api/companies/" + storedCompany.companyId + "/data-owners/*").as("postDataOwner");
          void postDataOwner(token, reader_userId, storedCompany.companyId);
          cy.wait("@postDataOwner", { timeout: Cypress.env("medium_timeout_in_ms") as number });
          logout();
          login(reader_name, reader_pw);
          cy.visitAndCheckAppMount("/companies/" + storedCompany.companyId);
          cy.get("h1").should("contain", testCompanyName);
          checkFrameworks();
          cy.get(`div[data-test="lksg-summary-panel"] a[data-test="lksg-provide-data-button"]`).should("exist").click();

          cy.get(`div[data-pc-section="title"]`).should("contain", "New Dataset - LkSG");
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
 * @returns the api response of the postDataOwner endpoint
 */
export async function postDataOwner(token: string, userId: string, companyId: string): Promise<CompanyDataOwners> {
  const apiResponse = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).postDataOwner(
    companyId,
    userId,
  );
  return apiResponse.data;
}
