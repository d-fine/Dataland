import { describeIf } from "@e2e/support/TestUtility";
import { uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm } from "@e2e/utils/EuTaxonomyFinancialsUpload";
import { EuTaxonomyDataForFinancials } from "@clients/backend";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { uploadReports } from "@sharedUtils/components/UploadReports";
import { TEST_PDF_FILE_NAME } from "@e2e/utils/Constants";

describeIf(
  "As a user, I expect that filling in and submitting the eu-taxonomy dataset for a financial companies works",

  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    let testData: FixtureData<EuTaxonomyDataForFinancials>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        const preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
        testData = getPreparedFixture("company-for-all-types", preparedFixtures);
      });
    });

    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    it(
      "Create an Eu Taxonomy Financial dataset via upload form with all financial company types selected to assure " +
        "that the upload form works fine with all options",
      () => {
        testData.companyInformation.companyName = "financials-upload-form";
        uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm(
          testData.companyInformation,
          testData.t,
          () => {
            uploadReports.uploadFile(TEST_PDF_FILE_NAME);
            uploadReports.validateSingleFileInUploadedList(TEST_PDF_FILE_NAME, "KB");
            uploadReports.fillReportCurrency(TEST_PDF_FILE_NAME);
            uploadReports.removeSingleUploadedFileFromUploadedList();
            uploadReports.checkNoReportIsListed();
          },
          () => undefined,
          () => undefined,
          () => undefined
        );
      }
    );
  }
);
