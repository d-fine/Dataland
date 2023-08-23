import CreateNewEuTaxonomyForNonFinancials from "@/components/forms/CreateNewEuTaxonomyForNonFinancials.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { TEST_PDF_FILE_BASEPATH, TEST_PDF_FILE_NAME } from "@sharedUtils/ConstantsForPdfs";
import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";
import { type CompanyAssociatedDataNewEuTaxonomyDataForNonFinancials } from "@clients/backend";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import DataPointFormWithToggle from "@/components/forms/parts/kpiSelection/DataPointFormWithToggle.vue";

describe("Component tests for the CreateP2pDataset that test dependent fields", () => {
  /**
   * On the eu taxonomy for non-financial services edit page, this method checks that there can not be a file uploaded
   * whose name equals the one of a file selected before
   */
  function checkFileWithExistingFilenameIsNotBeingAdded(): void {
    const reportThatCanBeUploaded = "test-report";
    const reportThatAlreadyExists = TEST_PDF_FILE_NAME;
    uploadDocuments.selectFile(reportThatCanBeUploaded, "referencedReports");
    uploadDocuments.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportThatCanBeUploaded);
    uploadDocuments.selectFile(reportThatAlreadyExists, "referencedReports");
    uploadDocuments.validateReportIsListedAsAlreadyUploaded(reportThatAlreadyExists);
    uploadDocuments.validateReportIsNotInFileSelectorAndHasNoInfoForm(reportThatAlreadyExists);
    uploadDocuments.validateNumberOfReportsSelectedForUpload(1);
  }

  /**
   * On the eu taxonomy for non-financial services edit page, this method checks that there can not be a file uploaded
   * whose name equals the one of a file selected before
   */
  function checkFileWithExistingFilenameOpensDialogWithWarning(): void {
    uploadDocuments.selectFile(TEST_PDF_FILE_NAME, "referencedReports");
    cy.get(`button[data-test='upload-files-button-referencedReports']`).click();
    cy.get("input[type=file]").selectFile(
      `../${TEST_PDF_FILE_BASEPATH}/more-pdfs-in-seperate-directory/${TEST_PDF_FILE_NAME}.pdf`,
      { force: true },
    );
    cy.get(".p-dialog-content").should("contain.text", "already uploaded");
    cy.get(".p-dialog-header-close").click();
    cy.get(`[data-test="${TEST_PDF_FILE_NAME}ToUploadContainer"]`).should("have.length", 1);
  }

  /**
   * Adds a report to upload and removes it again afterwards checking that no dialog regarding a duplicate file name
   * is wrongly triggered and that the file is correctly removed.
   */
  function checkExistingFilenameDialogDidNotBreakSubsequentSelection(): void {
    const reportNameA = TEST_PDF_FILE_NAME;
    const reportNameB = `${TEST_PDF_FILE_NAME}2`;
    uploadDocuments.selectFile(reportNameB, "referencedReports");

    cy.get(".p-dialog-content").should("not.exist");
    uploadDocuments.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportNameA);
    uploadDocuments.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportNameB);

    uploadDocuments.removeReportFromSelectionForUpload(TEST_PDF_FILE_NAME);

    uploadDocuments.validateReportIsNotAlreadyUploadedOrSelectedForUpload(reportNameA);
    uploadDocuments.validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportNameB);
    uploadDocuments.validateNumberOfReportsSelectedForUpload(1);

    uploadDocuments.removeReportFromSelectionForUpload(reportNameB);

    uploadDocuments.validateReportIsNotAlreadyUploadedOrSelectedForUpload(reportNameB);
    uploadDocuments.validateNumberOfReportsSelectedForUpload(0);
  }

  /**
   * On the eu taxonomy for non-financial services edit page, this method checks that submission is denied
   * if a report is not referenced
   */
  function checkThatFilesMustBeReferenced(): void {
    uploadDocuments.fillAllFormsOfReportsSelectedForUpload();
    submitButton.clickButton();
    cy.get('[data-test="failedUploadMessage"]').should("exist").should("contain.text", "test-report");
    cy.get('[data-test="failedUploadMessage"]')
      .should("exist")
      .should("satisfy", (element: JQuery<HTMLElement>) => {
        const expectedStrings = ["Not all uploaded reports are used", "test-report"];
        const elementText = element.text();
        return expectedStrings.every((expectedString) => elementText.includes(expectedString));
      });
  }

  //TODO: create function for uploading reports

  /**
   * this method fills and checks the general section
   * @param reportName the name of the report that is uploaded
   */
  function fillAndValidateGeneralSection(reportName: string): void {
    cy.get('[data-test="fiscalYearEnd"] button').should("have.class", "p-datepicker-trigger").click();
    cy.get("div.p-datepicker").find('button[aria-label="Next Month"]').click();
    cy.get("div.p-datepicker").find('span:contains("11")').click();
    cy.get('input[name="fiscalYearEnd"]').invoke("val").should("contain", "11");
    cy.get('input[name="fiscalYearDeviation"][value="Deviation"]').check();
    cy.get('div[data-test="submitSideBar"] li:last a').click();

    cy.get('input[name="scopeOfEntities"][value="Yes"]').check();

    cy.get('input[name="euTaxonomyActivityLevelReporting"][value="Yes"]').check();

    cy.get('input[name="numberOfEmployees"]').type("-13");
    cy.get('em[title="Number Of Employees"]').click();
    cy.get(`[data-message-type="validation"]`).should("contain", "at least 0").should("exist");
    cy.get('input[name="numberOfEmployees"]').clear().type("333");

    cy.get('input[name="nfrdMandatory"][value="Yes"]').check();

    cy.get('select[name="assurance"]').select(1);
    cy.get('input[name="provider"]').type("Assurance Provider");
    cy.get('select[name="report"]').select(reportName);
    cy.get('input[name="page"]').type("-13");
    cy.get('em[title="Assurance"]').click();
    cy.get(`[data-message-type="validation"]`).should("exist").should("contain", "at least 0");
    cy.get('[data-test="assuranceSection"] input[name="page"]').clear().type("1");
  }

  /**
   * this method fills and checks the other sections
   * @param reports the name of the reports that are uploaded
   */
  function fillAndValidateOtherSections(reports: string[]): void {
    for (const section of ["revenueSection", "capexSection", "opexSection"]) {
      cy.get('input[name="totalAmount"]').type("130");
      //cy.get('[data-test="currency???"] select[name="unit???"]').select(1);
      cy.get('select[name="report"]').select(reports[0]);
    }
  }

  /**
   * This method returns a mocked dataset for eu taxonomy for non financials with some fields filled.
   * @returns the dataset
   */
  function createMockCompanyAssociatedDataNewEuTaxoNonFinancials(): CompanyAssociatedDataNewEuTaxonomyDataForNonFinancials {
    return {
      companyId: "abc",
      reportingPeriod: "2020",
      data: {
        capex: {
          totalAmount: {
            quality: "Estimated",
            dataSource: {
              report: `${TEST_PDF_FILE_NAME}FileCopy`,
              page: 12,
            },
            comment: "test",
            value: 12000000,
            unit: "EUR",
          },
        },
        opex: {
          totalAmount: {
            quality: "Estimated",
            dataSource: {
              report: "None...",
              page: 12,
            },
            comment: "test",
            value: 10000000,
            unit: "EUR",
          },
        },
        revenue: {
          totalAmount: {
            quality: "Estimated",
            dataSource: {
              report: "None...",
              page: 12,
            },
            comment: "test",
            value: 40000000,
            unit: "EUR",
          },
        },
        general: {
          fiscalYearDeviation: "Deviation",
          fiscalYearEnd: "2023-09-11",
          scopeOfEntities: "Yes",
          nfrdMandatory: "Yes",
          euTaxonomyActivityLevelReporting: "Yes",
          assurance: {
            assurance: "None",
            provider: "Assurance Provider",
            dataSource: {
              report: TEST_PDF_FILE_NAME,
              page: 1,
            },
          },
          numberOfEmployees: 333,
          referencedReports: {
            [`${TEST_PDF_FILE_NAME}FileCopy`]: {
              reference: "bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7",
              isGroupLevel: "No",
              reportDate: "2023-07-12",
              currency: "EUR",
            },
            [TEST_PDF_FILE_NAME]: {
              reference: "bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7",
              isGroupLevel: "No",
              reportDate: "2023-07-12",
              currency: "EUR",
            },
          },
        },
      },
    };
  }

  const companyAssociatedNewEuTaxoFinancialsData = createMockCompanyAssociatedDataNewEuTaxoNonFinancials();

  it("Check that warning appears if two pdf files with same name are selected for upload", () => {
    cy.stub(DataPointFormWithToggle);
    cy.mountWithDialog(
      CreateNewEuTaxonomyForNonFinancials,
      {
        keycloak: minimalKeycloakMock({}),
      },
      { companyID: "company-id-does-not-matter-in-this-test" },
    ).then(() => {
      checkFileWithExistingFilenameOpensDialogWithWarning();
      checkExistingFilenameDialogDidNotBreakSubsequentSelection();
    });
  });

  it("Open upload page prefilled and assure that only the sections that the dataset holds are displayed", () => {
    cy.stub(DataPointFormWithToggle);
    cy.mountWithPlugins(CreateNewEuTaxonomyForNonFinancials, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          formInputsModel: companyAssociatedNewEuTaxoFinancialsData,
          templateDataset: companyAssociatedNewEuTaxoFinancialsData.data,
        };
      },
    }).then(() => {
      checkFileWithExistingFilenameIsNotBeingAdded();
      checkThatFilesMustBeReferenced();
    });
  });

});



//TODO This file has to be modified to work
