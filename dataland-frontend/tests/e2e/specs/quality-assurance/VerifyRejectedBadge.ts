import { describeIf } from "@e2e/support/TestUtility";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { LksgData } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgUpload";

describeIf(
  "Validation for correct display of 'Rejected' badge",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  () => {
    it("Verifies that the badge is shown as expected when an uploaded Lksg dataset gets rejected", () => {
      let preparedFixture: FixtureData<LksgData>;
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
        preparedFixture = getPreparedFixture("one-lksg-data-set-with-two-production-sites", preparedFixtures);
        cy.intercept("/api/data/lksg*", { middleware: true }, (req) => {
          req.headers["REQUIRE-QA"] = "true";
        });
        getKeycloakToken(admin_name, admin_pw)
          .then(async (token: string) =>
            uploadCompanyAndLksgDataViaApi(
              token,
              preparedFixture.companyInformation,
              preparedFixture.t,
              preparedFixture.reportingPeriod
            )
          )
          .then((uploadIds) => {
            cy.ensureLoggedIn(admin_name, admin_pw);
            cy.visit(`/qualityassurance`);
            cy.get("td", { timeout: Cypress.env("long_timeout_in_ms") as number }).should("exist");
            cy.get("button.p-paginator-last").click();
            cy.contains(`${uploadIds.dataId}`)
              .click();
            cy.get("button[aria-label='Reject Dataset']").click();
            cy.visit(`/datasets`);
            cy.get(`a[href="/companies/${uploadIds.companyId}/frameworks/lksg/${uploadIds.dataId}"]`)
              .parents("tr[role=row]")
              .find("td[role=cell]")
              .find("div[class='p-badge badge-red']")
              .should("exist")
              .should("contain", "REJECTED");
          });
      });
    });
  }
);
