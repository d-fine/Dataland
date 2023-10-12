import { getKeycloakToken } from "@e2e/utils/Auth";
import { admin_name, admin_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { Configuration, DataTypeEnum, type LksgData, LksgDataControllerApi } from "@clients/backend";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { checkStickynessOfSubmitSideBar } from "@e2e/utils/LksgUpload";
import { describeIf } from "@e2e/support/TestUtility";
import { humanizeStringOrNumber } from "@/utils/StringHumanizer";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { type UploadIds } from "@e2e/utils/GeneralApiUtils";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { objectDropNull, type ObjectType } from "@/utils/UpdateObjectUtils";
import { uploadCompanyAndFrameworkData } from "@e2e/utils/FrameworkUpload";

describeIf(
  "Validates the edit button functionality on the view framework page",
  {
    executionEnvironments: ["developmentLocal", "ci"],
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
            uploadCompanyAndFrameworkData(
              DataTypeEnum.Lksg,
              token,
              preparedFixture.companyInformation,
              preparedFixture.t,
              preparedFixture.reportingPeriod,
            ),
          )
          .then((idsUploaded) => {
            uploadIds = idsUploaded;
          });
      });
    });

    it("Editing Lksg data without changes should create a copy when uploaded", function () {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.visit(`/companies/${uploadIds.companyId}/frameworks/lksg`);
      cy.get('[data-test="frameworkDataTableTitle"]').should("contain.text", humanizeStringOrNumber(DataTypeEnum.Lksg));
      cy.get('[data-test="editDatasetButton"]').should("be.visible").click();
      cy.get("div").contains("New Dataset - LkSG").should("be.visible");
      const expectedCountry = preparedFixture.t.general?.productionSpecific?.listOfProductionSites?.[0]
        ?.addressOfProductionSite?.country as string;
      cy.get('[data-test="AddressFormField"]')
        .first()
        .find('[data-test="country"]')
        .should("contain", `(${expectedCountry})`);
      submitButton.buttonIsUpdateDataButton();
      submitButton.buttonAppearsEnabled();
      checkStickynessOfSubmitSideBar();
      submitButton.clickButton();
      cy.get("h4")
        .contains("Upload successfully executed.")
        .should("exist")
        .then(() => {
          return getKeycloakToken(admin_name, admin_pw).then(async (token) => {
            const listOfLksgDatasetsForCompany = await new LksgDataControllerApi(
              new Configuration({ accessToken: token }),
            ).getAllCompanyLksgData(uploadIds.companyId, false);
            expect(listOfLksgDatasetsForCompany.data).to.have.length(2);
            const firstLksgDataset = objectDropNull(
              listOfLksgDatasetsForCompany.data[0].data as unknown as ObjectType,
            ) as unknown as LksgData;
            const secondLksgDataset = objectDropNull(
              listOfLksgDatasetsForCompany.data[1].data as unknown as ObjectType,
            ) as unknown as LksgData;
            expect(firstLksgDataset).to.deep.equal(secondLksgDataset);
          });
        });
    });

    it("Edit and subsequent upload should work properly when removing or changing referenced documents", () => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
      cy.visit(`/companies/${uploadIds.companyId}/frameworks/lksg`);
      cy.get('[data-test="frameworkDataTableTitle"]').should("contain.text", humanizeStringOrNumber(DataTypeEnum.Lksg));
      cy.get('[data-test="editDatasetButton"]').should("be.visible").click();
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
