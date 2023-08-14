import { TEST_PDF_FILE_BASEPATH } from "@sharedUtils/Constants";

export const uploadDocuments = {
  selectFile(filename: string, fieldName = "UploadReports"): void {
    cy.get(`button[data-test='upload-files-button-${fieldName}']`).click();
    cy.get("input[type=file]").selectFile(`../${TEST_PDF_FILE_BASEPATH}/${filename}.pdf`, { force: true });
  },
  selectMultipleFilesAtOnce(filenames: string[], fieldName = "UploadReports"): void {
    cy.get(`button[data-test='upload-files-button-${fieldName}']`).click();
    const filenamePaths = filenames.map((filename) => `../testing/data/documents/${filename}.pdf`);
    cy.get("input[type=file]").selectFile(filenamePaths, { force: true });
  },
  selectDummyFile(filename: string, contentSize: number, fieldName = "UploadReports"): void {
    cy.get(`button[data-test='upload-files-button-${fieldName}']`).click();
    cy.get("input[type=file]").selectFile(
      {
        contents: new Cypress.Buffer(contentSize),
        fileName: `${filename}.pdf`,
        mimeType: "application/pdf",
      },
      { force: true },
    );
  },
  numberOfReportsSelectedForUploadShouldBe(expectedNumberOfReportsToUpload: number): void {
    cy.get('[data-test="report-to-upload-form"]').should("have.length", expectedNumberOfReportsToUpload);
  },
  fillAllFormsOfReportsSelectedForUpload(expectedNumberOfReportsToUpload?: number): void {
    if (expectedNumberOfReportsToUpload) {
      this.numberOfReportsSelectedForUploadShouldBe(expectedNumberOfReportsToUpload);
    }
    cy.get('[data-test="report-to-upload-form"]').each((element) => {
      cy.wrap(element).find(`[data-test="reportDate"] button`).should("have.class", "p-datepicker-trigger").click();
      cy.get("div.p-datepicker").find('button[aria-label="Previous Month"]').click();
      cy.get("div.p-datepicker").find(`span:contains("12")`).click();
      cy.wrap(element).find("select[name=currency]").select("EUR");
      cy.wrap(element).find(`input[value="No"]`).click();
    });
  },
  validateReportToUploadIsListedInTheFileSelectorList(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"]`).should("exist");
  },

  validateReportToUploadHasContainerWithInfoForm(reportName: string): void {
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("exist");
  },

  validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportName: string): void {
    this.validateReportToUploadIsListedInTheFileSelectorList(reportName);
    this.validateReportToUploadHasContainerWithInfoForm(reportName);
  },

  validateReportToUploadIsNotListedInTheFileSelectorList(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"]`).should("not.exist");
  },

  validateReportToUploadHasNoContainerWithInfoForm(reportName: string): void {
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("not.exist");
  },

  validateReportToUploadIsNotListedInFileSelectorAndHasNoInfoForm(reportName: string): void {
    this.validateReportToUploadIsNotListedInTheFileSelectorList(reportName);
    this.validateReportToUploadHasNoContainerWithInfoForm(reportName);
  },

  validateReportIsListedAsAlreadyUploaded(reportName: string): void {
    cy.get(`[data-test="${reportName}AlreadyUploadedContainer`).should("exist");
  },

  removeReportFromSelectionForUpload(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"] button`).click();
    cy.get(`[data-test="${reportName}FileUploadContainer"]`).should("not.exist");
  },
  removeAllReportsFromSelectionForUpload(): void {
    cy.get('button[data-test="files-to-upload-remove"]').each((element) => Cypress.$(element).trigger("click"));
    cy.get('button[data-test="files-to-upload-remove"]').should("not.exist");
  },
  removeAlreadyUploadedReport(reportName: string): Cypress.Chainable {
    return cy.get(`[data-test="${reportName}AlreadyUploadedContainer"] button`).click();
  },
  checkNoReportAreSelectedForUploadOrAlreadyUploaded(): void {
    cy.get('[data-test="files-to-upload"]').should("not.be.visible");
    cy.get('[data-test="report-to-upload-form"]').should("not.exist");
    cy.get('[data-test="report-uploaded-form"]').should("not.exist");
  },
  reportIsNotListedInFileSelectorAndHasNoInfoForm(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"]`).should("not.exist");
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("not.exist");
    cy.get(`[data-test="${reportName}AlreadyUploadedContainer"]`).should("not.exist");
  },

  selectDocumentAtEachFileSelector(filename: string): void {
    cy.window().then((win) => {
      win.document.querySelectorAll<HTMLInputElement>('input[type="file"]').forEach((element) => {
        cy.wrap(element).selectFile(`../testing/data/documents/${filename}.pdf`, { force: true });
      });
    });
  },
};
