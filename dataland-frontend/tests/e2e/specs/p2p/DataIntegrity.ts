import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { DataTypeEnum, type PathwaysToParisData } from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { formatPercentageNumberAsString } from "@/utils/Formatter";

let p2pFixtureForTest: FixtureData<PathwaysToParisData>;
before(function () {
  cy.fixture("CompanyInformationWithP2pPreparedFixtures").then(function (jsonContent) {
    const preparedFixturesP2p = jsonContent as Array<FixtureData<PathwaysToParisData>>;
    p2pFixtureForTest = getPreparedFixture("one-p2p-data-set-with-four-sectors", preparedFixturesP2p);
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
     * validates that the data uploaded via api is displayed correctly for a company
     * @param companyId the company associated to the data uploaded via form
     * @param dataId the company p2p id for accessing its view page
     */
    function validateFormUploadedData(companyId: string, dataId: string): void {
      cy.visit(`/companies/${companyId}/frameworks/${DataTypeEnum.P2p}/${dataId}`);
      cy.contains(`Show ${p2pFixtureForTest.t.general.general.sectors.length} values`).click();
      cy.get(".p-dialog").find(".p-dialog-title").should("have.text", "Sectors");
      p2pFixtureForTest.t.general.general.sectors.forEach((sector) => {
        cy.get("td").contains(humanizeStringOrNumber(sector)).should("exist");
      });
      cy.get(".p-dialog").find(".p-dialog-header-icon").click();
      cy.get('tr[data-section-label="Emissions planning"]').click();
      cy.contains(
        formatPercentageNumberAsString(
          assertDefined(p2pFixtureForTest.t.general.emissionsPlanning?.relativeEmissionsInPercent),
        ),
      );
      cy.contains("CEMENT").click();
      cy.contains("Material").click();
      cy.contains(
        formatPercentageNumberAsString(
          assertDefined(p2pFixtureForTest.t.cement?.material?.preCalcinedClayUsageInPercent),
        ),
      );
    }
    /* TODO additional tst code from main => check and integrate into this test!
    cy.contains(assertDefined(p2pFixtureForTest.t.cement?.material?.preCalcinedClayUsageInPercent).toFixed(0));
      cy.contains("FREIGHT TRANSPORT BY ROAD").click();
      cy.contains("Technology").click();
      cy.get("td > span > a").contains("Drive mix per fleet segment").click();
      cy.get(".p-dialog").contains(
        assertDefined(
          p2pFixtureForTest.t.freightTransportByRoad?.technology?.driveMixPerFleetSegment?.SmallTrucks
            ?.driveMixPerFleetSegmentInPercent,
        ).toFixed(2),
      );
     */

    it(
      "Create a company and a P2P dataset via the api, then open the P2P dataset in the upload form via " +
        "edit mode and re-submit it",
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = "Company-Created-In-DataJourney-Form-" + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return uploadFrameworkData(
              DataTypeEnum.P2p,
              token,
              storedCompany.companyId,
              "2021",
              p2pFixtureForTest.t,
            ).then((dataMetaInformation) => {
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
            });
          });
        });
      },
    );
  },
);
