import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  type LksgData,
  LksgDataControllerApi,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { checkToggleEmptyFieldsSwitch, compareObjectKeysAndValuesDeep } from "@e2e/utils/GeneralUtils";

describeIf(
  "As a user, I expect to be able to upload LkSG data via an upload form, and that the uploaded data is displayed " +
    "correctly in the frontend",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    let lksgFixtureWithNoNullFields: FixtureData<LksgData>;

    before(function () {
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        const preparedFixturesLksg = jsonContent as Array<FixtureData<LksgData>>;
        lksgFixtureWithNoNullFields = getPreparedFixture("lksg-all-fields", preparedFixturesLksg);
      });
    });

    it(
      "Create a company and a Lksg dataset via api, then re-upload it with the upload form in Edit mode and " +
        "assure that the re-uploaded dataset equals the pre-uploaded one",
      () => {
        cy.ensureLoggedIn(admin_name, admin_pw);
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = "Company-Created-In-Lksg-Blanket-Test" + uniqueCompanyMarker;
        getKeycloakToken(admin_name, admin_pw).then((token: string) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
            return uploadFrameworkData(
              DataTypeEnum.Lksg,
              token,
              storedCompany.companyId,
              "2021",
              lksgFixtureWithNoNullFields.t,
            ).then((dataMetaInformation) => {
              cy.intercept("**/api/companies/" + storedCompany.companyId + "/info").as("getCompanyInformation");
              cy.visitAndCheckAppMount(
                "/companies/" +
                  storedCompany.companyId +
                  "/frameworks/" +
                  DataTypeEnum.Lksg +
                  "/upload?templateDataId=" +
                  dataMetaInformation.dataId,
              );
              cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
              cy.get("h1").should("contain", testCompanyName);
              cy.intercept({
                url: `**/api/data/${DataTypeEnum.Lksg}`,
                times: 1,
              }).as("postCompanyAssociatedData");
              submitButton.clickButton();
              cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
                (postInterception) => {
                  cy.url().should("eq", getBaseUrl() + "/datasets");
                  const dataMetaInformationOfReuploadedDataset = postInterception.response?.body as DataMetaInformation;
                  return new LksgDataControllerApi(new Configuration({ accessToken: token }))
                    .getCompanyAssociatedLksgData(dataMetaInformationOfReuploadedDataset.dataId)
                    .then((axiosGetResponse) => {
                      const frontendSubmittedP2pDataset = axiosGetResponse.data.data as unknown as Record<
                        string,
                        object
                      >;
                      const originallyUploadedP2pDataset = lksgFixtureWithNoNullFields.t as unknown as Record<
                        string,
                        object
                      >;
                      compareObjectKeysAndValuesDeep(originallyUploadedP2pDataset, frontendSubmittedP2pDataset);
                      checkToggleEmptyFieldsSwitch("Mon, 27 Nov 2023, 11:57", "Industry");
                    });
                },
              );
            });
          });
        });
      },
    );
  },
);
