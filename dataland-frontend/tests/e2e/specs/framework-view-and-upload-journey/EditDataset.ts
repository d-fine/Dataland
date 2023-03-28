import { getKeycloakToken } from "@e2e/utils/Auth";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { Configuration, DataTypeEnum, LksgData, LksgDataControllerApi } from "@clients/backend";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgUpload";
import { describeIf } from "@e2e/support/TestUtility";
import { humanizeString } from "@/utils/StringHumanizer";

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
        .then((uploadIds) => {
          cy.ensureLoggedIn(uploader_name, uploader_pw);
          cy.visit(`/companies/${uploadIds.companyId}/frameworks/lksg`);
          cy.get('[data-test="frameworkDataTableTitle"]').should("contain.text", humanizeString(DataTypeEnum.Lksg));
          cy.get('[data-test="editDatasetButton"]').should("be.visible").click();
          cy.get("div").contains("New Dataset - LkSG").should("be.visible");
          cy.get("button").contains("UPDATE DATA").should("exist").click();
          cy.get("h4")
            .contains("Upload successfully executed.")
            .should("exist")
            .then(() => {
              return getKeycloakToken(uploader_name, uploader_pw).then(async (token) => {
                const data = await new LksgDataControllerApi(
                  new Configuration({ accessToken: token })
                ).getAllCompanyLksgData(uploadIds.companyId, false);
                expect(data.data).to.have.length(2);
                expect(data.data[0].data).to.deep.equal(data.data[1].data);
              });
            });
        });
    });
  }
);
