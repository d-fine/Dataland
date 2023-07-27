import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { DataTypeEnum, SfdrData } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadSfdrDataViaForm } from "@e2e/utils/SfdrUpload";

describeIf(
  "As a user, I expect that the upload form works correctly when editing and uploading a new SFDR dataset",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn(admin_name, admin_pw);
    });

    let testData: FixtureData<SfdrData>;

    before(function () {
      cy.fixture("CompanyInformationWithSfdrDataPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<SfdrData>>;
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    const keycloakToken = "";
    const frontendDocumentHash = "";

    it(
      "Upload Sfdr Dataset via form, check that redirect to MyDatasets works and assure that it can be " +
        "viewed and edited, and that file selection, upload and download works properly",
      () => {
        getKeycloakToken(admin_name, admin_pw).then((token) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation("All fields filled")).then(
            (storedCompany) => {
              cy.intercept(`**/companies**`).as("getDataForMyDatasetsPage");
              uploadSfdrDataViaForm(storedCompany.companyId);
              cy.url().should("eq", getBaseUrl() + "/datasets");
              cy.wait("@getDataForMyDatasetsPage");

              cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.Sfdr}`);

              cy.get("[data-test='companyNameTitle']").contains("All fields filled");
              checkAllDataProvided();
              clickEditButtonAndEditAndValidateChange(storedCompany.companyId).then((templateDataId) => {
                checkFileWithExistingFilenameCanNotBeResubmitted();
                checkExistingFilenameDialogDidNotBreakSubsequentSelection();
                checkThatFilesMustBeReferenced();
                checkThatFilesWithSameContentDontGetReuploaded(storedCompany.companyId, templateDataId);
                checkIfLinkedReportsAreDownloadable(storedCompany.companyId);
              });
            }
          );
        });
      }
    );
  }
);
