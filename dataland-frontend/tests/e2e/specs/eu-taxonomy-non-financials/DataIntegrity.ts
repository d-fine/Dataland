import { describeIf } from "@e2e/support/TestUtility";
import { getBaseUrl, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { DataTypeEnum, EuTaxonomyDataForNonFinancials } from "@clients/backend";
import {
  uploadEuTaxonomyDataForNonFinancialsViaForm,
  uploadOneEuTaxonomyNonFinancialsDatasetViaApi,
} from "@e2e/utils/EuTaxonomyNonFinancialsUpload";
import { uploadReports } from "@sharedUtils/components/UploadReports";
import { TEST_PDF_FILE_NAME, TEST_PDF_FILE_PATH } from "@e2e/utils/Constants";

describeIf(
  "As a user, I expect Eu Taxonomy Data for non-financials that I upload for a company to be displayed correctly",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    dataEnvironments: ["fakeFixtures"],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
    });

    let preparedFixtures: Array<FixtureData<EuTaxonomyDataForNonFinancials>>;

    before(function () {
      cy.fixture("CompanyInformationWithEuTaxonomyDataForNonFinancialsPreparedFixtures").then(function (jsonContent) {
        preparedFixtures = jsonContent as Array<FixtureData<EuTaxonomyDataForNonFinancials>>;
      });
    });

    /**
     * Rounds a number to two decimal places.
     * @param inputNumber The number which should be rounded
     * @returns the rounded number
     */
    function roundNumberToTwoDecimalPlaces(inputNumber: number): number {
      return Math.round(inputNumber * 100) / 100;
    }

    /**
     * This function uploads fixture data of one company and the associated data via API. Afterwards the result is
     * checked using the provided verifier.
     * @param fixtureData the company and its associated data
     * @param euTaxonomyPageVerifier the verify method for the EU Taxonomy Page
     */
    function uploadCompanyAndEuTaxonomyDataForNonFinancialsViaApiAndRunVerifier(
      fixtureData: FixtureData<EuTaxonomyDataForNonFinancials>,
      euTaxonomyPageVerifier: () => void
    ): void {
      getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
        return uploadCompanyViaApi(
          token,
          generateDummyCompanyInformation(fixtureData.companyInformation.companyName)
        ).then((storedCompany) => {
          return uploadOneEuTaxonomyNonFinancialsDatasetViaApi(
            token,
            storedCompany.companyId,
            fixtureData.reportingPeriod,
            fixtureData.t
          ).then(() => {
            cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}/*`).as("retrieveTaxonomyData");
            cy.visitAndCheckAppMount(
              `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
            );
            cy.wait("@retrieveTaxonomyData", { timeout: Cypress.env("long_timeout_in_ms") as number }).then(() => {
              euTaxonomyPageVerifier();
            });
          });
        });
      });
    }

    it("Create a EU Taxonomy Dataset via Api with total(€) and eligible(%) numbers", () => {
      const preparedFixture = getPreparedFixture("only-eligible-and-total-numbers", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForNonFinancialsViaApiAndRunVerifier(preparedFixture, () => {
        cy.get("body").should("contain", `Out of total of`);
        cy.get("body")
          .should("contain", "Eligible Revenue")
          .should(
            "contain",
            `${roundNumberToTwoDecimalPlaces(100 * preparedFixture.t.revenue!.eligiblePercentage!.value!)}%`
          );
        cy.get(".font-medium.text-3xl").should("contain", "€");
      });
    });

    it("Create a EU Taxonomy Dataset via Api with only eligible(%) numbers", () => {
      const preparedFixture = getPreparedFixture("only-eligible-numbers", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForNonFinancialsViaApiAndRunVerifier(preparedFixture, () => {
        cy.get("body")
          .should("contain", "Eligible OpEx")
          .should(
            "contain",
            `${roundNumberToTwoDecimalPlaces(100 * preparedFixture.t.revenue!.eligiblePercentage!.value!)}%`
          );
        cy.get("body").should("contain", "Eligible Revenue").should("not.contain", `Out of total of`);
        cy.get(".font-medium.text-3xl").should("not.contain", "€");
      });
    });

    it("Create a EU Taxonomy Dataset via Api without referenced reports and ensure that the reports banner is not displayed", () => {
      const preparedFixture = getPreparedFixture("company_without_reports", preparedFixtures);
      uploadCompanyAndEuTaxonomyDataForNonFinancialsViaApiAndRunVerifier(preparedFixture, () => {
        cy.get("div[data-test='reportsBanner']").should("not.exist");
      });
    });

    it(
      "Upload EU Taxonomy Dataset via form, check that redirect to MyDatasets works and assure that it can be " +
        "viewed and edited, and that ",
      () => {
        // TODO Emanuel: description is missing a lot more stuff that is actually happening here

        // TODO Emanuel: this test is pretty long and also contains stuff that fits better to the "UploadReports" test file.  we should consider moving some of the test code here

        // TODO Emanuel: furthermore this test could be done in a shorter amount of time =>  e.g. some of the page reloads are actually not needed and could be worked around.

        getKeycloakToken(uploader_name, uploader_pw).then((token) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation("All fields filled")).then(
            (storedCompany) => {
              cy.intercept(`**/companies**`).as("getDataForMyDatasetsPage");
              uploadEuTaxonomyDataForNonFinancialsViaForm(storedCompany.companyId);
              cy.url().should("eq", getBaseUrl() + "/datasets");
              cy.wait("@getDataForMyDatasetsPage");

              // TEST IF ALL VALUES THERE ON VIEW PAGE          TODO comment supports reading the test while working on it => delete at the very end
              cy.visitAndCheckAppMount(
                `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
              );
              cy.get("[data-test='companyNameTitle']").contains("All fields filled");
              cy.get("body").should("contain", "Eligible Revenue").should("contain", "%");
              cy.get("body").should("contain", "Aligned Revenue").should("contain", "%");
              cy.get("body").should("contain", "Eligible CapEx").should("contain", "%");
              cy.get("body").should("contain", "Aligned CapEx").should("contain", "%");
              cy.get("body").should("contain", "Eligible OpEx").should("contain", "%");
              cy.get("body").should("contain", "Aligned OpEx").should("contain", "%");

              // TEST IF A CHANGED VALUE WIE THE "EDIT" FUNCTION IS VIEWABLE          TODO comment supports reading the test while working on it => delete at the very end
              const newValueForEligibleRevenueAfterEdit = "30";
              cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}/*`).as("getDataToPrefillForm");
              cy.get('button[data-test="editDatasetButton"]').click();
              cy.wait("@getDataToPrefillForm");
              cy.get('[data-test="pageWrapperTitle"]').should("contain", "Edit");
              cy.get(`div[data-test=revenueSection] div[data-test=eligible] input[name="value"]`)
                .clear()
                .type(newValueForEligibleRevenueAfterEdit);
              cy.get('button[data-test="submitButton"]').click();
              cy.wait("@getDataForMyDatasetsPage");
              cy.visitAndCheckAppMount(
                `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
              );
              cy.get("body")
                .should("contain", "Eligible Revenue")
                .should("contain", newValueForEligibleRevenueAfterEdit + "%");

              // TEST IF A FILE WITH AN ALREADY EXISTING NAME CANNOT BE SUBMITTED          TODO comment supports reading the test while working on it => delete at the very end
              cy.get('button[data-test="editDatasetButton"]').click();
              cy.wait("@getDataToPrefillForm");
              cy.get(`[data-test="${TEST_PDF_FILE_NAME}AlreadyUploadedContainer`).should("exist");
              cy.get("input[type=file]").selectFile(`../${TEST_PDF_FILE_PATH}`, { force: true });
              cy.get('[data-test="file-name-already-exists"]').should("exist");
              cy.get(`[data-test="${TEST_PDF_FILE_NAME}ToUploadContainer"]`).should("not.exist");
              cy.get('button[data-test="submitButton"]').click();
              cy.get('[data-test="failedUploadMessage"]').should("contain.text", `${TEST_PDF_FILE_NAME}`);
              // TEST IF UPLOADING A REPORT IS NOT POSSIBLE IF IT IS NOT REFERENCED BY AT LEAST ONE DATAPOINT         TODO comment supports reading the test while working on it => delete at the very end
              cy.get(`button[data-test="remove-${TEST_PDF_FILE_NAME}"]`).click();
              cy.get('[data-test="file-name-already-exists"]').should("not.exist");
              cy.get("input[type=file]").selectFile(
                {
                  contents: `../${TEST_PDF_FILE_PATH}`,
                  fileName: "someOtherFileName" + ".pdf",
                },
                { force: true }
              );
              uploadReports.fillAllReportInfoForms();
              cy.get('button[data-test="submitButton"]').click();
              cy.get('[data-test="failedUploadMessage"]').should("exist").should("contain.text", "someOtherFileName");

              // TEST IF UPLOADING A REPORT WHICH HAS THE CONTENT OF AN ALREADY EXISTING PDF FILE LEADS TO NO ACTUAL RE-UPLOAD OF IT         TODO comment supports reading the test while working on it => delete at the very end
              cy.visitAndCheckAppMount(
                `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
              );
              cy.get("[data-test='taxocard']").should("exist");
              cy.get('button[data-test="editDatasetButton"]').click();
              cy.wait("@getDataToPrefillForm");
              cy.get('[data-test="pageWrapperTitle"]').should("contain", "Edit");
              const differentFileNameForSameFile = `${TEST_PDF_FILE_NAME}FileCopy`;
              cy.get("input[type=file]").selectFile(
                {
                  contents: `../${TEST_PDF_FILE_PATH}`,
                  fileName: differentFileNameForSameFile + ".pdf",
                },
                { force: true }
              );
              uploadReports.fillAllReportInfoForms();
              cy.get(`div[data-test=capexSection] div[data-test=total] select[name="report"]`).select(
                differentFileNameForSameFile
              );
              cy.intercept(`**/documents/*/exists`).as("documentExists");
              cy.intercept(`**/documents/`).as("postDocument");
              cy.intercept(`**/api/data/${DataTypeEnum.EutaxonomyNonFinancials}`).as("postCompanyAssociatedData");
              cy.get('button[data-test="submitButton"]').click();
              cy.wait("@documentExists", { timeout: Cypress.env("short_timeout_in_ms") as number })
                .its("response.body")
                .should("deep.equal", { documentExists: true });
              // cy.get("@postDocument", { timeout: Cypress.env("short_timeout_in_ms") as number }) // TODO Emanuel: We need to assert that this call does not happen here!
              cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then(
                (req) => {
                  cy.log(req.response!.body as string);
                }
              );
              cy.wait("@getDataForMyDatasetsPage");

              // TEST IF THE UPLOADED REPORT CAN BE DOWNLOADED AND ACTUALLY CONTAINS THE UPLOADED PDF         TODO comment supports reading the test while working on it => delete at the very end
              cy.visitAndCheckAppMount(
                `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
              );
              const expectedPathToDownloadedReport = Cypress.config("downloadsFolder") + `/${TEST_PDF_FILE_NAME}.pdf`;
              const downloadLinkSelector = `span[data-test="Report-Download-${differentFileNameForSameFile}"]`;
              cy.readFile(expectedPathToDownloadedReport).should("not.exist");
              cy.get(downloadLinkSelector)
                .click()
                .then(() => {
                  cy.readFile(`../${TEST_PDF_FILE_PATH}`, "binary", {
                    timeout: Cypress.env("medium_timeout_in_ms") as number,
                  }).then((expectedPdfBinary) => {
                    cy.task("calculateHash", expectedPdfBinary).then((expectedPdfHash) => {
                      cy.readFile(expectedPathToDownloadedReport, "binary", {
                        timeout: Cypress.env("medium_timeout_in_ms") as number,
                      }).then((receivedPdfHash) => {
                        cy.task("calculateHash", receivedPdfHash).should("eq", expectedPdfHash);
                      });
                      cy.task("deleteFolder", Cypress.config("downloadsFolder"));
                    });
                  });
                });
            }
          );
        });
      }
    );

    it(
      "Upload EU Taxonomy Dataset via form with no values for revenue and assure that it can be viewed on the framework " +
        "data view page with an appropriate message shown for the missing revenue data",
      () => {
        const companyName = "Missing field company";
        const missingDataMessage = "No data has been reported";
        getKeycloakToken(uploader_name, uploader_pw).then((token) => {
          return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyName)).then((storedCompany) => {
            uploadEuTaxonomyDataForNonFinancialsViaForm(storedCompany.companyId, true);
            cy.intercept(`/api/data/${DataTypeEnum.EutaxonomyNonFinancials}/*`).as("retrieveTaxonomyData");
            cy.visitAndCheckAppMount(
              `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
            );
            cy.wait("@retrieveTaxonomyData", { timeout: Cypress.env("long_timeout_in_ms") as number }).then(() => {
              cy.get("h1[class='mb-0']").contains(companyName);
              cy.get("body").should("contain", "Eligible Revenue").should("contain", missingDataMessage);
            });
          });
        });
      }
    );
  }
);
