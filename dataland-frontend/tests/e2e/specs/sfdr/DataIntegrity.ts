import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { DataTypeEnum, SfdrData } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import {
    selectsReportsForUploadInSfdrForm,
    uploadCompanyAndSfdrDataViaApi,
} from "@e2e/utils/SfdrUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { toggleRowGroup } from "@sharedUtils/components/ToggleRowFunction";

let testSfdrCompany: FixtureData<SfdrData>;
before(function () {
  cy.fixture("CompanyInformationWithSfdrPreparedFixtures").then(function (jsonContent) {
    const sfdrPreparedFixtures = jsonContent as Array<FixtureData<SfdrData>>;
    testSfdrCompany = getPreparedFixture("CompanyInformationWithSfdrData", sfdrPreparedFixtures);
  });
});
describeIf(
  "As a user, I expect that the upload form works correctly when editing and uploading a new SFDR dataset",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    /**
     * validates that the data uploaded via the function `uploadSfdrDataViaForm` is displayed correctly for a company
     * @param companyId the company associated to the data uploaded via form
     */
    function validateFormUploadedData(companyId: string): void {
      cy.visit("/companies/" + companyId + "/frameworks/" + DataTypeEnum.Sfdr);
      cy.get(".p-datatable-tbody").find(".p-rowgroup-header").eq(0).should("have.text", "General");

      cy.get(".p-datatable-tbody").find(".p-rowgroup-header").eq(2).should("have.text", "Biodiversity");
      toggleRowGroup("biodiversity");
      cy.contains("td.headers-bg", "Primary Forest And Wooded Land Of Native Species Exposure").should("exist");
      cy.contains("td.headers-bg", "Primary Forest And Wooded Land Of Native Species Exposure")
        .siblings("td")
        .should("have.text", "Yes");
      cy.contains("td.headers-bg", "Protected Areas Exposure").should("exist");
      cy.contains("td.headers-bg", "Protected Areas Exposure").siblings("td").should("have.text", "No");
      cy.contains("td.headers-bg", "Rare Or Endangered Ecosystems Exposure").should("exist");
      cy.contains("td.headers-bg", "Rare Or Endangered Ecosystems Exposure").siblings("td").should("have.text", "Yes");
    }

    /**
     * Set the quality for the sfdr test dataset
     */
    function setQualityInSfdrUploadForm(): void {
      cy.get('[data-test="primaryForestAndWoodedLandOfNativeSpeciesExposure"]')
        .find('select[name="quality"]')
        .select(3);
      cy.get('[data-test="protectedAreasExposure"]').find('select[name="quality"]').select(3);
      cy.get('[data-test="rareOrEndangeredEcosystemsExposure"]').find('select[name="quality"]').select(3);
    }
    it("Create a company via api and upload a SFDR dataset via the api", () => {
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-Sfdr-DataIntegrity-Test-" + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyAndSfdrDataViaApi(token, generateDummyCompanyInformation(testCompanyName), testSfdrCompany.t, "2021" ).then(
            (uploadIds) => {
              cy.intercept("**/api/companies/" + uploadIds.companyId).as("getCompanyInformation");
              cy.visitAndCheckAppMount(
                "/companies/" +
                  uploadIds.companyId +
                  "/frameworks/" +
                  DataTypeEnum.Sfdr +
                  "/upload" +
                  "?templateDataId=" +
                  uploadIds.dataId,
              );
              cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });

              cy.get("h1").should("contain", testCompanyName);
              selectsReportsForUploadInSfdrForm();
              setQualityInSfdrUploadForm();
              submitButton.clickButton();
              cy.url().should("eq", getBaseUrl() + "/datasets");
              validateFormUploadedData(uploadIds.companyId);
            },
          );
        });
      });
  },
);
