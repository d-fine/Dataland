import { getKeycloakToken } from "@e2e/utils/Auth";
import { admin_name, admin_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { DataTypeEnum, type LksgData } from "@clients/backend";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { describeIf } from "@e2e/support/TestUtility";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { type UploadIds } from "@e2e/utils/GeneralApiUtils";
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
      cy.get("h1", { timeout: Cypress.env("medium_timeout_in_ms") as number }).should(
        "contain.text",
        lksgFixture.companyInformation.companyName,
      );
      submitButton.buttonAppearsEnabled();

      cy.get("button[data-test='files-to-upload-remove']")
        .first()
        .parents('[data-test^="BaseDataPointFormField"]')
        .first()
        .then(($parent) => {
          cy.wrap($parent)
            .find("input.p-radiobutton")
            .eq(1)
            .click()
            .find("button[data-test='files-to-upload-remove']")
            .should("not.exist");

          cy.wrap($parent).find("input.p-radiobutton").eq(0).click();

          cy.wrap($parent).find('button[data-test^="upload-files-button"]').should("be.visible");
          cy.wrap($parent)
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
