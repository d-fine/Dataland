import { getKeycloakToken } from "@e2e/utils/Auth";
import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { Configuration, DataTypeEnum, LksgData, LksgDataControllerApi } from "@clients/backend";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { checkStickynessOfSubmitSideBar, uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgUpload";
import { describeIf } from "@e2e/support/TestUtility";
import { humanizeString } from "@/utils/StringHumanizer";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { UploadIds } from "@e2e/utils/GeneralApiUtils";
import { assertDefined } from "@/utils/TypeScriptUtils";

describeIf(
  "Validates the edit button functionality on the view framework page",
  {
    executionEnvironments: ["developmentLocal", "ci"],
    dataEnvironments: ["fakeFixtures", "realData"],
  },
  () => {
    let uploadIds: UploadIds;
    let preparedFixture: FixtureData<LksgData>;

    before(() => {
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
        preparedFixture = getPreparedFixture("lksg-all-fields", preparedFixtures);
        getKeycloakToken(admin_name, admin_pw)
          .then(async (token: string) =>
            uploadCompanyAndLksgDataViaApi(
              token,
              preparedFixture.companyInformation,
              preparedFixture.t,
              preparedFixture.reportingPeriod
            )
          )
          .then((idsUploaded) => {
            uploadIds = idsUploaded;
          });
      });
    });

    it("Editing Lksg data without changes should create a copy when uploaded", function () {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visit(`/companies/${uploadIds.companyId}/frameworks/lksg`);
      cy.get('[data-test="frameworkDataTableTitle"]').should("contain.text", humanizeString(DataTypeEnum.Lksg));
      cy.get('[data-test="editDatasetButton"]').should("be.visible").click();
      cy.get("div").contains("New Dataset - LkSG").should("be.visible");
      const expectedCountry = preparedFixture.t.general?.productionSpecific?.listOfProductionSites?.[0]
        ?.addressOfProductionSite?.country as string;
      cy.get('[data-test="AddressFormField0"] [data-test="country"]').should("contain", `(${expectedCountry})`);
      submitButton.buttonIsUpdateDataButton();
      submitButton.buttonAppearsEnabled();
      checkStickynessOfSubmitSideBar();
      submitButton.clickButton();
      cy.get("h4")
        .contains("Upload successfully executed.")
        .should("exist")
        .then(() => {
          return getKeycloakToken(admin_name, admin_pw).then(async (token) => {
            const data = await new LksgDataControllerApi(
              new Configuration({ accessToken: token })
            ).getAllCompanyLksgData(uploadIds.companyId, false);
            expect(data.data).to.have.length(2);
            expect(data.data[0].data).to.deep.equal(data.data[1].data);
          });
        });
    });

    it("Edit and subsequent upload should work properly when removing or changing referenced documents", () => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visit(`/companies/${uploadIds.companyId}/frameworks/lksg`);
      cy.get('[data-test="frameworkDataTableTitle"]').should("contain.text", humanizeString(DataTypeEnum.Lksg));
      cy.get('[data-test="editDatasetButton"]').should("be.visible").click();
      submitButton.buttonAppearsEnabled();
      cy.get("button[data-test=files-to-upload-remove]")
        .first()
        .parents(".form-field:first")
        .invoke("attr", "data-test")
        .then((dataTest) => {
          cy.get(`div[data-test=${assertDefined(dataTest)}]`)
            .find(`input[id=value-option-no]`)
            .click();
          cy.get(`div[data-test=${assertDefined(dataTest)}]`)
            .find(`button[data-test=files-to-upload-remove]`)
            .should("not.exist");
        });
      cy.get("button[data-test=files-to-upload-remove]")
        .eq(0)
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
            `div[data-test=${assertDefined(dataTest)}] button[data-test=upload-files-button-${assertDefined(dataTest)}]`
          ).should("be.visible");
          cy.get(`div[data-test=${assertDefined(dataTest)}]`)
            .find("input[type=file]")
            .selectFile(`../testing/data/documents/test-report.pdf`, { force: true, log: true });
        });
      submitButton.buttonAppearsEnabled();
      submitButton.clickButton();
      cy.get("h4").contains("Upload successfully executed.").should("exist");
    });
  }
);
