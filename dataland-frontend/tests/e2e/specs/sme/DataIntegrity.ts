import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import {
  Configuration,
  type DataMetaInformation,
  DataTypeEnum,
  SmeDataControllerApi,
  type SmeData,
  type StoredCompany,
} from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { uploadSmeFrameworkData } from "@e2e/utils/FrameworkUpload";
import { compareObjectKeysAndValuesDeep } from "@e2e/utils/GeneralUtils";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import * as MLDT from "@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils";
import { UploadReports } from "@sharedUtils/components/UploadReports";
import { TEST_PDF_FILE_NAME, TEST_PDF_FILE_PATH } from "@sharedUtils/ConstantsForPdfs";

let smeFixtureForTest: FixtureData<SmeData>;

let tokenForAdminUser: string;
let storedTestCompany: StoredCompany;
let dataMetaInfoOfTestDataset: DataMetaInformation;
const uploadReports = new UploadReports("referencedReports");
before(function () {
  cy.fixture("CompanyInformationWithSmePreparedFixtures").then(function (jsonContent) {
    const preparedFixturesSme = jsonContent as Array<FixtureData<SmeData>>;
    smeFixtureForTest = getPreparedFixture("Sme-dataset-with-no-null-fields", preparedFixturesSme);
  });
});

describeIf(
  "As a user, I expect to be able to edit and submit Sme data via the upload form",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  function (): void {
    it(
      "Create a company and a Sme dataset via api, then re-upload it with the upload form in Edit mode and " +
        "assure that the re-uploaded dataset equals the pre-uploaded one",
      () => {
        const uniqueCompanyMarker = Date.now().toString();
        const testCompanyName = "Company-Created-In-Sme-Blanket-Test-" + uniqueCompanyMarker;
        let dataMetaInformationOfReuploadedDataset: DataMetaInformation;
        getKeycloakToken(admin_name, admin_pw)
          .then((token: string) => {
            tokenForAdminUser = token;
            return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
          })
          .then((storedCompany) => {
            storedTestCompany = storedCompany;
            const Files: File[] = [];
            return uploadSmeFrameworkData(
              tokenForAdminUser,
              storedCompany.companyId,
              "2021",
              smeFixtureForTest.t,
              Files,
            );
          })
          .then((dataMetaInfo) => {
            dataMetaInfoOfTestDataset = dataMetaInfo;
            cy.ensureLoggedIn(admin_name, admin_pw);
            cy.intercept("**/api/companies/" + storedTestCompany.companyId + "/info").as("getCompanyInformation");
            cy.visitAndCheckAppMount(
              "/companies/" +
                storedTestCompany.companyId +
                "/frameworks/" +
                DataTypeEnum.Sme +
                "/upload?templateDataId=" +
                dataMetaInfoOfTestDataset.dataId,
            );
            cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
            cy.get("h1").should("contain", storedTestCompany.companyInformation.companyName);
            cy.intercept({
              url: `**/api/data/${DataTypeEnum.Sme}`,
              times: 1,
            }).as("postCompanyAssociatedData");
            submitButton.clickButton();
            cy.wait(100);
            cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number })
              .then((postResponseInterception) => {
                cy.url().should("eq", getBaseUrl() + "/datasets");
                dataMetaInformationOfReuploadedDataset = postResponseInterception.response?.body as DataMetaInformation;
                return new SmeDataControllerApi(
                  new Configuration({ accessToken: tokenForAdminUser }),
                ).getCompanyAssociatedSmeData(dataMetaInformationOfReuploadedDataset.dataId);
              })
              .then((axiosGetResponse) => {
                const frontendSubmittedSmeDataset = axiosGetResponse.data.data;
                frontendSubmittedSmeDataset.insurances?.naturalHazards?.naturalHazardsCovered?.sort();
                compareObjectKeysAndValuesDeep(
                  smeFixtureForTest.t as unknown as Record<string, object>,
                  frontendSubmittedSmeDataset as unknown as Record<string, object>,
                );
                return uploadRealFile(storedTestCompany.companyId, dataMetaInformationOfReuploadedDataset.dataId);
              })
              .then(() => {
                checkDocumentIsDownloadable(storedTestCompany.companyId, dataMetaInformationOfReuploadedDataset.dataId);
              });

            /**
             * uploads a real file and replaces the fake file for power consumption
             * @param companyId the company associated to the data uploaded
             * @param dataId the latest version of sme data for the company
             */
            function uploadRealFile(companyId: string, dataId: string): void {
              cy.visitAndCheckAppMount(
                "/companies/" + companyId + "/frameworks/" + DataTypeEnum.Sme + "/upload?templateDataId=" + dataId,
              );
              uploadReports.selectFile(TEST_PDF_FILE_NAME);
              uploadReports.validateReportToUploadHasContainerInTheFileSelector(TEST_PDF_FILE_NAME);
              uploadReports.validateReportToUploadHasContainerWithInfoForm(TEST_PDF_FILE_NAME);
              cy.get('div[name="fileName"]').click();
              cy.get("ul.p-dropdown-items li").contains(TEST_PDF_FILE_NAME).click();
              cy.intercept({
                url: `**/api/data/${DataTypeEnum.Sme}`,
                times: 1,
              }).as("postCompanyAssociatedData");
              submitButton.clickButton();
              cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
                (postResponseInterception) => {
                  cy.url().should("eq", getBaseUrl() + "/datasets");
                  dataMetaInformationOfReuploadedDataset = postResponseInterception.response
                    ?.body as DataMetaInformation;
                },
              );
            }

            /**
             * validates that the document pertaining to power consumption is displayed correctly and can be downloaded by the data owner
             * @param companyId the company associated to the data uploaded
             * @param dataId the latest version of sme data for the company
             */
            function checkDocumentIsDownloadable(companyId: string, dataId: string): void {
              cy.visitAndCheckAppMount("/companies/" + companyId + "/frameworks/" + DataTypeEnum.Sme + "/" + dataId);

              MLDT.getSectionHead("Power").should("have.attr", "data-section-expanded", "false").click();
              MLDT.getSectionHead("Consumption").should("have.attr", "data-section-expanded", "false").click();
              MLDT.getCellValueContainer("Power consumption in MWh")
                .find("a.link")
                .should("include.text", "MWh")
                .click();
              const expectedPathToDownloadedReport = Cypress.config("downloadsFolder") + `/${TEST_PDF_FILE_NAME}.pdf`;
              cy.readFile(expectedPathToDownloadedReport).should("not.exist");
              cy.intercept("**/documents/*").as("documentDownload");
              cy.get('[data-test="download-link"]').click();
              cy.wait("@documentDownload");
              cy.wait(500);
              cy.readFile(`../${TEST_PDF_FILE_PATH}`, "binary", {
                timeout: Cypress.env("medium_timeout_in_ms") as number,
              }).then((expectedFileBinary) => {
                cy.task("calculateHash", expectedFileBinary).then((expectedFileHash) => {
                  cy.readFile(expectedPathToDownloadedReport, "binary", {
                    timeout: Cypress.env("medium_timeout_in_ms") as number,
                  }).then((receivedFileHash) => {
                    cy.task("calculateHash", receivedFileHash).should("eq", expectedFileHash);
                  });
                  cy.task("deleteFolder", Cypress.config("downloadsFolder"));
                });
              });
            }
          });
      },
    );
  },
);
