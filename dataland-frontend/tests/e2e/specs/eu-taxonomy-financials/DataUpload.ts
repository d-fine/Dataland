import { describeIf } from "@e2e/support/TestUtility";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import {
  fillEuTaxonomyForFinancialsUploadForm,
} from "@e2e/utils/EuTaxonomyFinancialsUpload";
import {
  CompanyInformation,
  EuTaxonomyDataForFinancials,
  DataTypeEnum, CompanyAssociatedDataEuTaxonomyDataForFinancials,
} from "@clients/backend";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {uploadReports} from "@sharedUtils/components/UploadReports";
import {assertDefined} from "@/utils/TypeScriptUtils";
import {CyHttpMessages} from "cypress/types/net-stubbing";

describeIf(
  "As a user, I expect that the upload form works correctly when editing and uploading a new dataset",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function () {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    let preparedFixtures: Array<FixtureData<EuTaxonomyDataForFinancials>>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancialsPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
      });
    });

    /**
     * Uploads a company via POST-request, then an EU Taxonomy dataset for financial companies for the uploaded company
     * via the form in the frontend, and then visits the view page where that dataset is displayed
     * and
     *
     * @param companyInformation Company information to be used for the company upload
     * @param testData EU Taxonomy dataset for financial companies to be uploaded
     * @param beforeFormFill is performed before filling the fields of the upload form
     * @param afterFormFill is performed after filling the fields of the upload form
     * @param submissionDataIntercept performs checks on the request itself
     * @param afterDatasetSubmission is performed after the data has been submitted
     */
    function uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm(
      companyInformation: CompanyInformation,
      testData: EuTaxonomyDataForFinancials,
      beforeFormFill: () => void,
      afterFormFill:() => void,
      submissionDataIntercept: (request: CyHttpMessages.IncomingHttpRequest) => void,
      afterDatasetSubmission: (companyId: string) => void,
    ): void {
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
          (storedCompany): void => {
            cy.ensureLoggedIn(uploader_name, uploader_pw);
            cy.visitAndCheckAppMount(
              `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`
            );
            beforeFormFill();
            fillEuTaxonomyForFinancialsUploadForm(testData);
            afterFormFill();
            cy.intercept("POST", `**/api/data/**`, submissionDataIntercept).as("postCompanyAssociatedData");
            cy.get('button[data-test="submitButton"]').click();
            cy.wait("@postCompanyAssociatedData", { timeout: 100000 }).then((interception) => {
              expect(interception.response?.statusCode).to.eq(200);
            });
            afterDatasetSubmission(storedCompany.companyId);
          }
        );
      });
    }

    function gotoEditForm(companyId: string, expectIncludedFile: boolean) {
      cy.intercept("GET", "**/api/data/**").as("dataView")
      cy.visit(
        `/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`
      );
      cy.wait("@dataView");
      cy.intercept("GET", `**/api/data/**`).as("loadTemplate")
      cy.get('[data-test="editDatasetButton"]').click();
      cy.wait(`@loadTemplate`).its("response").then((response) => {
        const data = assertDefined((response?.body as CompanyAssociatedDataEuTaxonomyDataForFinancials)?.data);
        expect(assertDefined(data.referencedReports).pdfTest !== undefined).to.equal(expectIncludedFile);
        expect(assertDefined(data.referencedReports).pdfTest2 !== undefined).to.equal(true); // TODO this should not be expected
        expect(assertDefined(data.referencedReports).pdfTest3 !== undefined).to.equal(false);
      });
    }

    it(
      "Create an Eu Taxonomy Financial dataset via upload form with all financial company types selected to assure " +
      "that the upload form works fine with all options",
      () => {
        const testData = getPreparedFixture("company-for-all-types", preparedFixtures);
        uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm(
          testData.companyInformation,
          testData.t,
          () => {
            const filename = "pdfTest.pdf";
            uploadReports.uploadFile(filename)
            uploadReports.validateSingleFileInUploadedList(filename, "KB")
            uploadReports.validateSingleFileInfo()
            uploadReports.removeSingleUploadedFileFromUploadedList()
            uploadReports.checkNoReportIsListed()
          },
          () => undefined,
          () => undefined,
          () => undefined,
        );
      }
    );

    it.only(
      "Check if the file upload info remove button works as expected",
      () => {
        const testData = getPreparedFixture("company-for-all-types", preparedFixtures);
        uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm(
          testData.companyInformation,
          testData.t,
          () => undefined,
          () => {
            uploadReports.uploadFile("pdfTest.pdf")
            uploadReports.uploadFile("pdfTest2.pdf")
            uploadReports.fillAllReportInfoForms()
            cy.get(`[data-test="assetManagementKpis"]`).find(`[data-test="banksAndIssuers"]`).find('select[name="report"]').select(2);
          },
          (request) => {
            const data = assertDefined((request.body as CompanyAssociatedDataEuTaxonomyDataForFinancials).data);
            expect(assertDefined(data.referencedReports).pdfTest !== undefined).to.equal(true);
            expect(assertDefined(data.referencedReports).pdfTest2 !== undefined).to.equal(true); // TODO this should not be expected
            expect(assertDefined(data.referencedReports).pdfTest3 !== undefined).to.equal(false);
          },
          (companyId) => {
            gotoEditForm(companyId, true)
            // TODO the following breaks the test before it???
            // cy.intercept("POST", `**/api/data/**`, (request) => {
            //   const data = assertDefined((request.body as CompanyAssociatedDataEuTaxonomyDataForFinancials).data);
            //   expect(assertDefined(data.referencedReports).pdfTest !== undefined).to.equal(false);
            //   expect(assertDefined(data.referencedReports).pdfTest2 !== undefined).to.equal(true); // TODO this should not be expected
            //   expect(assertDefined(data.referencedReports).pdfTest3 !== undefined).to.equal(false);
            // }).as("postModifiedData");
            // cy.get('button[data-test="submitButton"]').click();
            // cy.wait("@postModifiedData", { timeout: 100000 }).then((interception) => {
            //   expect(interception.response?.statusCode).to.eq(200);
            // });
          }
        );
      },
    );
  }
);
