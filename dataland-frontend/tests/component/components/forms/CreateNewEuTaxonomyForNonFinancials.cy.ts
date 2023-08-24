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

  /**
   * this method fills and checks the general section
   * @param reports the name of the reports that are uploaded
   */
  function fillAndValidateGeneralSection(reports: string[]): void {
    cy.get('[data-test="fiscalYearEnd"] button').should("have.class", "p-datepicker-trigger").click();
    cy.get("div.p-datepicker").find('button[aria-label="Next Month"]').click();
    cy.get("div.p-datepicker").find('span:contains("11")').click();
    cy.get('input[name="fiscalYearEnd"]').invoke("val").should("contain", "11");
    cy.get('input[name="fiscalYearDeviation"][value="Deviation"]').check();
    cy.get('div[data-test="submitSideBar"] li:last a').click();
    cy.get('input[name="scopeOfEntities"][value="Yes"]').check();
    cy.get('input[name="euTaxonomyActivityLevelReporting"][value="Yes"]').check();
    cy.get('input[name="numberOfEmployees"]').clear().type("-13");
    cy.get('em[title="Number Of Employees"]').click();
    cy.get(`[data-message-type="validation"]`).should("contain", "at least 0").should("exist");
    cy.get('input[name="numberOfEmployees"]').clear().type("333");
    cy.get('input[name="nfrdMandatory"][value="Yes"]').check();
    cy.get('select[name="assurance"]').select(2);
    cy.get('input[name="provider"]').clear().type("Assurance Provider");
    cy.get('div[label="General"] select[name="report"]').select(reports);
    cy.get('div[label="General"] input[name="page"]').clear().type("-13");
    cy.get('div[label="General"] em[title="Page"]').click();
    cy.get(`[data-message-type="validation"]`).should("contain", "at least 0").should("exist");
    cy.get('div[label="General"] input[name="page"]').clear().type("3");
  }

  /**
   * this method fills and checks the Revenue section, excluding the activity component
   * @param reports the name of the reports that are uploaded
   */
  function fillAndValidateOtherSections(reports: string[]): void {
    cy.get('div[label="Revenue"] input[name="value"]').clear().type("130000");
    cy.get('div[label="Revenue"] select[name="unit"]').select(1);
    cy.get('div[label="Revenue"] select[name="report"]').select(reports[0]);
    cy.get('div[label="Revenue"] input[name="page"]').clear().type("5");
    cy.get('div[label="Revenue"] select[name="quality"]').select(2);
    cy.get('div[label="Revenue"] textarea[name="comment"]').clear().type("just a comment");
    cy.get('div[label="Revenue"] input[name="relativeShareInPercent"]').eq(0).clear().type("a");
    cy.get('div[label="Revenue"] em[title="Total Eligible Revenue"]').click();
    cy.get(`div[label="Revenue"] [data-message-type="validation"]`).should("contain", "must be a number").should("exist");
    cy.get('div[label="Revenue"] input[name="relativeShareInPercent"]').eq(0).clear().type("120");
    cy.get('div[label="Revenue"] em[title="Total Eligible Revenue"]').click();
    cy.get(`div[label="Revenue"] [data-message-type="validation"]`).should("contain", "must be between 0 and 100").should("exist");
    cy.get('div[label="Revenue"] input[name="relativeShareInPercent"]').eq(0).clear().type("25");
    cy.get('div[label="Revenue"] input[name="amount"]').eq(0).clear().type("5000");
    cy.get('div[label="Revenue"] select[name="currency"]').eq(0).select(5);
    cy.get('div[label="Revenue"] input[name="relativeShareInPercent"]').eq(1).clear().type("50");
    cy.get('div[label="Revenue"] input[name="amount"]').eq(1).clear().type("4000");
    cy.get('div[label="Revenue"] select[name="currency"]').eq(1).select(51);
    cy.get('div[label="Revenue"] input[name="climateMitigation"]').clear().type("a");
    cy.get('div[label="Revenue"] em[title="Total Eligible Revenue"]').click();
    cy.get(`div[label="Revenue"] [data-message-type="validation"]`).should("contain", "must be a number").should("exist");
    cy.get('div[label="Revenue"] input[name="climateMitigation"]').clear().type("-12");
    cy.get('div[label="Revenue"] em[title="Total Eligible Revenue"]').click();
    cy.get(`div[label="Revenue"] [data-message-type="validation"]`).should("contain", "must be between 0 and 100").should("exist");
    cy.get('div[label="Revenue"] input[name="climateMitigation"]').clear().type("15");
    cy.get('div[label="Revenue"] input[name="climateAdaptation"]').clear().type("15");
    cy.get('div[label="Revenue"] input[name="water"]').clear().type("15");
    cy.get('div[label="Revenue"] input[name="circularEconomy"]').clear().type("15");
    cy.get('div[label="Revenue"] input[name="pollutionPrevention"]').clear().type("15");
    cy.get('div[label="Revenue"] input[name="biodiversity"]').clear().type("15");
    cy.get('div[label="Revenue"] input[name="relativeShareInPercent"]').eq(2).clear().type("11");
    cy.get('div[label="Revenue"] input[name="amount"]').eq(2).clear().type("12000");
    cy.get('div[label="Revenue"] select[name="currency"]').eq(2).select(51);
    cy.get('div[label="Revenue"] input[name="relativeShareInPercent"]').eq(3).clear().type("13");
    cy.get('div[label="Revenue"] input[name="amount"]').eq(3).clear().type("13000");
    cy.get('div[label="Revenue"] select[name="currency"]').eq(3).select(53);
    cy.get('div[label="Revenue"] input[name="totalEnablingShare"]').clear().type("12");
    cy.get('div[label="Revenue"] input[name="totalTransitionalShare"]').clear().type("12");
  }

  /**
   * this method fills and checks the fields in Aligned Activities
   * several activity fields can be added and removed
   * only one type of activity per activity field
   * one or multiple Nace codes ca be selected based on the selected activity
   */
  function fillAndValidateAlignedActivities(): void {
    cy.get('div[label="Revenue"] [data-test="alignedActivities"] button').click();
    cy.get('div[label="Revenue"] [data-test="dataTestChooseActivityButton"] button').click();
    cy.get('div[label="Revenue"] em[data-test="removeButton"]').first().click();
    //cy.get('div[label="Revenue"] [data-test="alignedActivities"] button').click();
    //TODO: transfer to ActivityFormField.cy.ts


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
          referencedReportsForPrefill: companyAssociatedNewEuTaxoFinancialsData?.data?.general?.referencedReports,
          companyAssociatedNewEuTaxonomyDataForNonFinancials: companyAssociatedNewEuTaxoFinancialsData,
        };
      },
    }).then(() => {
      checkFileWithExistingFilenameIsNotBeingAdded();
      checkThatFilesMustBeReferenced();
    });
  });

  it("Open upload page, fill out and validate the upload form, except for new activities", () => {
    cy.stub(DataPointFormWithToggle);
    cy.mountWithPlugins(CreateNewEuTaxonomyForNonFinancials, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          referencedReportsForPrefill: companyAssociatedNewEuTaxoFinancialsData?.data?.general?.referencedReports,
          companyAssociatedNewEuTaxonomyDataForNonFinancials: companyAssociatedNewEuTaxoFinancialsData,
        };
      },
    }).then(() => {
      uploadDocuments.selectFile(TEST_PDF_FILE_NAME, "referencedReports");
      fillAndValidateGeneralSection([TEST_PDF_FILE_NAME]);
      fillAndValidateOtherSections([TEST_PDF_FILE_NAME]);
      fillAndValidateAlignedActivities();
    });
  });
});

//TODO This file has to be modified to work
