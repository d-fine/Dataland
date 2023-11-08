import { getKeycloakToken } from "@e2e/utils/Auth";
import { admin_name, admin_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { DataTypeEnum, type LksgData } from "@clients/backend";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { describeIf } from "@e2e/support/TestUtility";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { type UploadIds } from "@e2e/utils/GeneralApiUtils";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { uploadCompanyAndFrameworkData } from "@e2e/utils/FrameworkUpload";

describeIf(
  "Validates the edit button functionality on the view framework page",
  {
    executionEnvironments: ["developmentLocal", "ci"],
  },
  () => {
    let uploadIds: UploadIds;
    let lksgFixture: FixtureData<LksgData>;

    before(() => {
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        const lksgsPreparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
        lksgFixture = getPreparedFixture("lksg-all-fields", lksgsPreparedFixtures);
        lksgFixture.companyInformation.identifiers = {};
        getKeycloakToken(admin_name, admin_pw)
          .then(async (token: string) =>
            uploadCompanyAndFrameworkData(
              DataTypeEnum.Lksg,
              token,
              lksgFixture.companyInformation,
              lksgFixture.t,
              lksgFixture.reportingPeriod,
            ),
          )
          .then((idsUploaded) => {
            uploadIds = idsUploaded;
          });
      });
    });

    it("Edit and subsequent upload should work properly when removing or changing referenced documents", () => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
      cy.visitAndCheckAppMount(
        "/companies/" +
          uploadIds.companyId +
          "/frameworks/" +
          DataTypeEnum.Lksg +
          "/upload?templateDataId=" +
          uploadIds.dataId,
      );
      submitButton.buttonAppearsEnabled();
      cy.get("button[data-test=files-to-upload-remove]")
        .first()
        .parents(".form-field:first")
        .invoke("attr", "data-test")
        .then((dataTest) => {
          cy.get(`div[data-test=${assertDefined(dataTest)}]`)
            .find("input.p-radiobutton")
            .eq(1)
            .click();
          cy.get(`div[data-test=${assertDefined(dataTest)}]`)
            .find(`button[data-test=files-to-upload-remove]`)
            .should("not.exist");
        });
      cy.get("button[data-test=files-to-upload-remove]")
        .first()
        .parents(".form-field:first")
        .invoke("attr", "data-test")
        .then((dataTest) => {
          cy.get(`div[data-test=${assertDefined(dataTest)}]`)
            .find(`div[class=upload-files-button]`)
            .should("not.exist");
          cy.get(`div[data-test=${assertDefined(dataTest)}] button[data-test=files-to-upload-remove]`)
            .should("be.visible")
            .click();
          cy.get(
            `div[data-test=${assertDefined(dataTest)}] button[data-test=upload-files-button-${assertDefined(
              dataTest,
            )}]`,
          ).should("be.visible");
          cy.get(`div[data-test=${assertDefined(dataTest)}]`)
            .find("input[type=file]")
            .selectFile(`../testing/data/documents/test-report.pdf`, { force: true, log: true });
        });
      submitButton.exists();
      submitButton.buttonAppearsEnabled();
      submitButton.clickButton();
      cy.get("h4").contains("Upload successfully executed.").should("exist");
    });
  },
);
