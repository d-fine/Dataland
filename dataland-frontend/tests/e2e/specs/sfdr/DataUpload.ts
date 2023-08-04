import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { DataTypeEnum, SfdrData } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { selectReportedQualityForAllFields, uploadOneSfdrDataset, uploadSfdrDataViaForm } from "@e2e/utils/SfdrUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { toggleRowGroup } from "@sharedUtils/components/ToggleRowFunction";

let companiesWithSfdrData: Array<FixtureData<SfdrData>>;
let testSfdrCompany: FixtureData<SfdrData>;
before(function () {
  cy.fixture("CompanyInformationWithSfdrPreparedFixtures").then(function (jsonContent) {
    companiesWithSfdrData = jsonContent as Array<FixtureData<SfdrData>>;
    testSfdrCompany = getPreparedFixture("CompanyInformationWithSfdrData", companiesWithSfdrData);
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

    it("Create a company via api and upload an Sfdr dataset via the Sfdr upload form", () => {
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-DataJourney-Form-" + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw)
        .then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
        })
        .then((storedCompany) => {
          cy.intercept("**/api/companies/" + storedCompany.companyId).as("getCompanyInformation");
          cy.visitAndCheckAppMount(
            "/companies/" + storedCompany.companyId + "/frameworks/" + DataTypeEnum.Sfdr + "/upload",
          );
          cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
          cy.url().should(
            "eq",
            getBaseUrl() + "/companies/" + storedCompany.companyId + "/frameworks/" + DataTypeEnum.Sfdr + "/upload",
          );
          cy.get("h1").should("contain", testCompanyName);
          uploadSfdrDataViaForm(storedCompany.companyId);
          validateFormUploadedData(storedCompany.companyId);
        });
    });

    /**
     * validates that the data uploaded via the function `uploadSfdrDataViaForm` is displayed correctly for a company
     * @param companyId the company associated to the data uploaded via form
     */
    function validateFormUploadedData(companyId: string): void {
      cy.visit("/companies/" + companyId + "/frameworks/" + DataTypeEnum.Sfdr);
      cy.get(".p-datatable-tbody").find(".p-rowgroup-header").eq(0).should("have.text", "General");

      cy.get(".p-datatable-tbody")
        .find(".p-rowgroup-header")
        .eq(1)
        .should("have.text", "Anti-corruption and anti-bribery");
      cy.get(".p-datatable-tbody").find(".p-rowgroup-header").eq(2).should("have.text", "Biodiversity");

      cy.get(".p-datatable-tbody").find(".p-rowgroup-header").eq(3).find(".p-row-toggler").click();

      cy.contains("td.headers-bg", "Data Date").should("exist");
      cy.contains("td.headers-bg", "Data Date").siblings("td").should("have.text", "2023-07-13");

      cy.contains("td.headers-bg", "Carbon Reduction Initiatives").should("exist");
      cy.contains("td.headers-bg", "Carbon Reduction Initiatives").siblings("td").should("have.text", "Yes");
    }

    it("Create a company via api and upload a SFDR dataset via the api", () => {
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-DataJourney-Form-" + uniqueCompanyMarker;
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
          return uploadOneSfdrDataset(token, storedCompany.companyId, "2021", testSfdrCompany.t).then(
            (dataMetaInformation) => {
              console.log(testSfdrCompany.t);
              cy.intercept("**/api/companies/" + storedCompany.companyId).as("getCompanyInformation");
              cy.visitAndCheckAppMount(
                "/companies/" +
                  storedCompany.companyId +
                  "/frameworks/" +
                  DataTypeEnum.Sfdr +
                  "/upload" +
                  "?templateDataId=" +
                  dataMetaInformation.dataId,
              );
              cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });

              cy.url().should(
                "eq",
                getBaseUrl() +
                  "/companies/" +
                  storedCompany.companyId +
                  "/frameworks/" +
                  DataTypeEnum.Sfdr +
                  "/upload" +
                  "?templateDataId=" +
                  dataMetaInformation.dataId,
              );

              cy.get("h1").should("contain", testCompanyName);
              //TODO remove selectReportedQualityForAllFields(), shift the validation into the existing function, remove other test once quality is no longer mandatory
              selectReportedQualityForAllFields();
              submitButton.clickButton();
              cy.url().should("eq", getBaseUrl() + "/datasets");
              cy.visit("/companies/" + storedCompany.companyId + "/frameworks/" + DataTypeEnum.Sfdr);
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
              cy.contains("td.headers-bg", "Rare Or Endangered Ecosystems Exposure")
                .siblings("td")
                .should("have.text", "Yes");
            },
          );
        });
      });
    });
  },
);
