import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { DataMetaInformation, DataTypeEnum, PathwaysToParisData, StoredCompany } from "@clients/backend";
import { uploadOneP2pDatasetViaApi } from "@e2e/utils/P2pUpload";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { submitButton } from "@sharedUtils/components/SubmitButton";

let companiesWithP2pData: Array<FixtureData<PathwaysToParisData>>;
let testP2pCompany: FixtureData<PathwaysToParisData>;
before(function () {
  cy.fixture("CompanyInformationWithP2pData").then(function (jsonContent) {
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
     * Toggles the data-table row group with the given key
     * @param groupKey the key of the row group to expand
     */
    function toggleRowGroup(groupKey: string): void {
      cy.get(`span[data-test=${groupKey}]`).siblings("button").last().click();
    }

    /**
     * validates that the data uploaded via the function `uploadOneP2pDatasetViaApi` is displayed correctly for a company
     * @param companyId the company associated to the data uploaded via form
     * @param dataId
     */
    function validateFormUploadedData(companyId: string, dataId: string): void {
      const firstSector = testP2pCompany.t.general.general.sector[0];
      const secondSector = testP2pCompany.t.general.general.sector[1];
      cy.visit(`/companies/${companyId}/frameworks/${DataTypeEnum.P2p}/${dataId}`);
      cy.get('td > [data-test="general"]').click();
      cy.contains('Show "Sector"').click();
      cy.get(".p-dialog").find(".p-dialog-title").should("have.text", "Sector");
      cy.get(".p-dialog th").eq(0).should("have.text", "Sector");
      cy.get(".p-dialog tr").eq(1).find("td").eq(0).find("li").should("have.length", 10);
      cy.get(".p-dialog tr").eq(1).find("td").eq(0).find("li").eq(0).should("have.text", firstSector);
      cy.get(".p-dialog tr").eq(1).find("td").eq(0).find("li").eq(1).should("have.text", secondSector);
      cy.get('td > [data-test="emissionsPlanning"]').click();
      //cy.contains('6503');
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
                `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.P2p}/upload?templateDataId=${dataMetaInformation.dataId}`
              );
              cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
              cy.get("h1").should("contain", testCompanyName);
              validateFormUploadedData(storedCompany.companyId, dataMetaInformation.dataId);
              submitButton.clickButton();
              cy.url().should("eq", getBaseUrl() + "/datasets");
            }
          );
        });
      });
    });
  }
);
