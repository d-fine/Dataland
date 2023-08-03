import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { DataTypeEnum, PathwaysToParisData } from "@clients/backend";
import { uploadOneP2pDatasetViaApi } from "@e2e/utils/P2pUpload";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { FixtureData } from "@sharedUtils/Fixtures";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { humanizeString } from "@/utils/StringHumanizer";

let companiesWithP2pData: Array<FixtureData<PathwaysToParisData>>;
let testP2pCompany: FixtureData<PathwaysToParisData>;
before(function () {
  cy.fixture("CompanyInformationWithP2pPreparedFixtures").then(function (jsonContent) {
    companiesWithP2pData = jsonContent as Array<FixtureData<PathwaysToParisData>>;
    testP2pCompany = companiesWithP2pData[0];
  });
});

describeIf(
  "As a user, I expect to be able to upload P2P data via the api, and that the uploaded data is displayed " +
    "correctly in the frontend",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    /**
     * validates that the data uploaded via the function `uploadOneP2pDatasetViaApi` is displayed correctly for a company
     * @param companyId the company associated to the data uploaded via form
     * @param dataId the company p2p id for accessing its view page
     */
    function validateFormUploadedData(companyId: string, dataId: string): void {
      cy.visit(`/companies/${companyId}/frameworks/${DataTypeEnum.P2p}/${dataId}`);
      cy.contains('Show "Sectors"').click();
      cy.get(".p-dialog").find(".p-dialog-title").should("have.text", "Sectors");
      cy.get(".p-dialog th").eq(0).should("have.text", "Sectors");
      testP2pCompany.t.general.general.sectors.forEach((sector) => {
        cy.get("span").contains(sector).should("exist");
      });
      cy.get(".p-dialog").find(".p-dialog-header-icon").click();
      cy.get('td > [data-test="emissionsPlanning"]').click();
      cy.contains("8245");
      cy.contains("AUTOMOTIVE").click();
      cy.contains("1672");

    }

    it("Create a company via api and upload a P2P dataset via the api", () => {
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-DataJourney-Form-" + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
          return uploadOneP2pDatasetViaApi(token, storedCompany.companyId, "2021", testP2pCompany.t).then(
            (dataMetaInformation) => {
              cy.intercept("**/api/companies/" + storedCompany.companyId).as("getCompanyInformation");
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
              submitButton.clickButton();
              cy.url().should("eq", getBaseUrl() + "/datasets");
              validateFormUploadedData(storedCompany.companyId, dataMetaInformation.dataId);
            },
          );
        });
      });
    });
  },
);
