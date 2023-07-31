import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { DataTypeEnum, SfdrData } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadSfdrDataViaForm } from "@e2e/utils/SfdrUpload";
import { uploadLksgDataViaForm } from "@e2e/utils/LksgUpload";

describeIf(
  "As a user, I expect that the upload form works correctly when editing and uploading a new SFDR dataset",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    let testData: FixtureData<SfdrData>;

    before(function () {
      cy.fixture("CompanyInformationWithSfdrPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<SfdrData>>;
        testData = getPreparedFixture("company-with-one-sfdr-data-set", preparedFixtures);
      });
    });

    const keycloakToken = "";
    const frontendDocumentHash = "";

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
          //validateFormUploadedData(storedCompany.companyId);
        });
    });

    /**
     * validates that the data uploaded via the function `uploadSfdrDataViaForm` is displayed correctly for a company
     * @param companyId the company associated to the data uploaded via form
     */
    function validateFormUploadedData(companyId: string): void {
      cy.visit("/companies/" + companyId + "/frameworks/" + DataTypeEnum.Sfdr);
      cy.get('td > [data-test="productionSpecificOwnOperations"]').click();
      cy.contains('Show "Most Important Products"').click();
      cy.get(".p-dialog").find(".p-dialog-title").should("have.text", "Most Important Products");
      cy.get(".p-dialog th").eq(0).should("have.text", "Product Name");
      cy.get(".p-dialog th").eq(1).should("have.text", "Production Steps");
      cy.get(".p-dialog th").eq(2).should("have.text", "Related Corporate Supply Chain");
      cy.get(".p-dialog tr").should("have.length", 3);
      cy.get(".p-dialog tr").eq(1).find("td").eq(0).should("have.text", "Test Product 1");
      cy.get(".p-dialog tr").eq(1).find("td").eq(1).find("li").should("have.length", 2);
      cy.get(".p-dialog tr").eq(1).find("td").eq(1).find("li").eq(0).should("have.text", "first");
      cy.get(".p-dialog tr").eq(1).find("td").eq(1).find("li").eq(1).should("have.text", "second");
      cy.get(".p-dialog tr").eq(1).find("td").eq(2).should("have.text", "Description of something");
      cy.get(".p-dialog tr").eq(2).find("td").eq(0).should("have.text", "Test Product 2");
    }
  },
);
