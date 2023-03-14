import { getKeycloakToken } from "@e2e/utils/Auth";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { Configuration, LksgData, LksgDataControllerApi } from "@clients/backend";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { getPreparedFixture } from "@e2e/utils/GeneralApiUtils";
import { uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgUpload";
import { describeIf } from "@e2e/support/TestUtility";

describeIf(
  "Validates the edit button functionality on the view framework page",
  {
    executionEnvironments: ["developmentLocal", "ci"],
    dataEnvironments: ["fakeFixtures", "realData"],
  },
  () => {
    let preparedFixtures: Array<FixtureData<LksgData>>;

    before(function () {
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
      });
    });

    it("Should display a working edit button to data uploaders on the LKSG page", () => {
      const preparedFixture = getPreparedFixture("lksg-all-fields", preparedFixtures);
      getKeycloakToken(uploader_name, uploader_pw)
        .then(async (token: string) =>
          uploadCompanyAndLksgDataViaApi(
            token,
            preparedFixture.companyInformation,
            preparedFixture.t,
            preparedFixture.reportingPeriod
          )
        )
        .then((uploadId) => {
          cy.ensureLoggedIn(uploader_name, uploader_pw);
          cy.visit(`/companies/${uploadId.companyId}/frameworks/lksg`);
          cy.get("h2").contains("LkSG Data").should("be.visible");
          cy.get("button:contains(EDIT)").should("be.visible").click();
          cy.get("a").contains("EDIT").should("be.visible").click();
          cy.get("div").contains("New Dataset - LkSG").should("be.visible");
          cy.get("button").contains("ADD DATA").should("exist").click();
          cy.get("h4")
            .contains("Upload successfully executed.")
            .should("exist")
            .then(() => {
              return getKeycloakToken(uploader_name, uploader_pw).then(async (token) => {
                const data = await new LksgDataControllerApi(
                  new Configuration({ accessToken: token })
                ).getAllCompanyLksgData(uploadId.companyId, false);
                expect(data.data).to.have.length(2);
                expect(data.data[0].data).to.deep.equal(data.data[1].data);
              });
            });
        });
    });
  }
);
