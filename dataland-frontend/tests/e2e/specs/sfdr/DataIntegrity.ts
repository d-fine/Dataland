import { describeIf } from "@e2e/support/TestUtility";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { SfdrData } from "@clients/backend";
import { uploadCompanyAndSfdrDataViaApi } from "@e2e/utils/SfdrUpload";
import { getPreparedFixture } from "@e2e/utils/GeneralApiUtils";

describeIf(
  "As a user, I expect SFDR data that I upload for a company to be displayed correctly",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    let preparedFixtures: Array<FixtureData<SfdrData>>;

    before(function () {
      cy.fixture("CompanyInformationWithSfdrPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<SfdrData>>;
      });
    });

    it("Check Sfdr view page for company with one Sfdr data set", () => {
      const preparedFixture = getPreparedFixture("company-with-sfdr-data-set", preparedFixtures);
      const companyInformation = preparedFixture.companyInformation;
      const sfdrData = preparedFixture.t;

      getKeycloakToken(uploader_name, uploader_pw).then(async (token: string) => {
        return uploadCompanyAndSfdrDataViaApi(token, companyInformation, sfdrData).then((uploadIds) => {
          cy.intercept("**/api/data/sfdr/company/*").as("retrieveSfdrData");
          cy.visitAndCheckAppMount(`/companies/${uploadIds.companyId}/frameworks/sfdr`);
          cy.wait("@retrieveSfdrData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(() => {
            cy.get(`h1`).should("contain", companyInformation.companyName);

            cy.get("table.p-datatable-table")
              .find(`span:contains(${sfdrData.social!.general!.fiscalYearEnd!})`)
              .should("exist");

            cy.get("button.p-row-toggler").eq(0).click();
            cy.get("table.p-datatable-table")
              .find(`span:contains(${sfdrData.social!.general!.fiscalYearEnd!})`)
              .should("not.exist");

            cy.get("button.p-row-toggler").eq(0).click();
            cy.get("table.p-datatable-table")
              .find(`span:contains(${sfdrData.social!.general!.fiscalYearEnd!})`)
              .should("exist");
          });
        });
      });
    });
  }
);
