import { TEST_PDF_FILE_BASEPATH } from "@sharedUtils/ConstantsForPdfs";

export class UploadDocuments {
  private name: string;
  private selector: string;
  private addButtonSelector: string;
  constructor(name: string = "UploadReports") {
    this.name = name;
    this.selector = `div[data-test='upload-files-button-${name}']`;
    this.addButtonSelector = `button[data-test='upload-documents-${name}']`;
  }

  selectFile(filename: string): void {
    cy.get(this.addButtonSelector).click();
    cy.get(this.selector)
      .find("input[type=file]")
      .selectFile(`../${TEST_PDF_FILE_BASEPATH}/${filename}.pdf`, { force: true });
  }

  selectMultipleFilesAtOnce(filenames: string[]): void {
    cy.get(this.addButtonSelector).click();
    const filenamePaths = filenames.map((filename) => `../testing/data/documents/${filename}.pdf`);
    cy.get(this.selector).find("input[type=file]").selectFile(filenamePaths, { force: true });
  }
  selectDummyFile(filename: string, contentSize: number): void {
    cy.get(this.addButtonSelector).click();
    cy.get(this.selector)
      .find("input[type=file]")
      .selectFile(
        {
          contents: new Cypress.Buffer(contentSize),
          fileName: `${filename}.pdf`,
          mimeType: "application/pdf",
        },
        { force: true },
      );
  }
  selectDummyFileOfType(filename: string, fileType: string, contentSize: number): void {
    cy.get(this.addButtonSelector).click();
    cy.get(this.addButtonSelector)
      .parents(".p-fileupload")
      .find("input[type=file]")
      .selectFile(
        {
          contents: new Cypress.Buffer(contentSize),
          fileName: `${filename}.${fileType}`,
          mimeType: "application/pdf",
        },
        { force: true },
      );
  }

  fillAllFormsOfReportsSelectedForUpload(expectedNumberOfReportsToUpload?: number): void {
    if (expectedNumberOfReportsToUpload) {
      this.validateNumberOfReportsSelectedForUpload(expectedNumberOfReportsToUpload);
    }
    cy.get('[data-test="report-to-upload-form"]').each((element) => {
      cy.wrap(element).find(`[data-test="reportDate"] button`).should("have.class", "p-datepicker-trigger").click();
      cy.get("div.p-datepicker").find('button[aria-label="Previous Month"]').click();
      cy.get("div.p-datepicker").find(`span:contains("12")`).click();
      cy.wrap(element).find("select[name=currency]").select("EUR");
      cy.wrap(element).find(`input[value="No"]`).click();
    });
  }

  validateNumberOfReportsSelectedForUpload(expectedNumberOfReportsToUpload: number): void {
    cy.get('[data-test="report-to-upload-form"]').should("have.length", expectedNumberOfReportsToUpload);
  }

  validateReportToUploadHasContainerInTheFileSelector(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"]`).should("exist");
  }

  validateReportToUploadHasContainerWithInfoForm(reportName: string): void {
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("exist");
  }

  validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportName: string): void {
    this.validateReportToUploadHasContainerInTheFileSelector(reportName);
    this.validateReportToUploadHasContainerWithInfoForm(reportName);
  }

  validateReportIsNotInFileSelector(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"]`).should("not.exist");
  }

  validateReportHasNoContainerWithInfoForm(reportName: string): void {
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("not.exist");
  }

  validateReportIsNotInFileSelectorAndHasNoInfoForm(reportName: string): void {
    this.validateReportIsNotInFileSelector(reportName);
    this.validateReportHasNoContainerWithInfoForm(reportName);
  }

  validateReportIsListedAsAlreadyUploaded(reportName: string): void {
    cy.get(`[data-test="${reportName}AlreadyUploadedContainer`).should("exist");
  }

  validateNoReportsAreAlreadyUploadedOrSelectedForUpload(): void {
    cy.get('[data-test="files-to-upload"]').should("not.be.visible");
    cy.get('[data-test="report-to-upload-form"]').should("not.exist");
    cy.get('[data-test="report-uploaded-form"]').should("not.exist");
  }
  validateReportIsNotAlreadyUploadedOrSelectedForUpload(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"]`).should("not.exist");
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("not.exist");
    cy.get(`[data-test="${reportName}AlreadyUploadedContainer"]`).should("not.exist");
  }

  removeReportFromSelectionForUpload(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"] button`).click();
    cy.get(`[data-test="${reportName}FileUploadContainer"]`).should("not.exist");
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("not.exist");
  }
  removeAlreadyUploadedReport(reportName: string): Cypress.Chainable {
    return cy.get(`[data-test="${reportName}AlreadyUploadedContainer"] button`).click();
  }

  selectDocumentAtEachFileSelector(filename: string): void {
    cy.window().then((win) => {
      win.document.querySelectorAll<HTMLInputElement>('input[type="file"]').forEach((element) => {
        cy.wrap(element).selectFile(`../testing/data/documents/${filename}.pdf`, { force: true });
      });
    });
  }
  errorMessage(): Cypress.Chainable {
    return cy.get(".p-fileupload .p-message-error");
  }
  dismissErrorMessage(): void {
    this.errorMessage().find(".p-message-close-icon").click();
  }
}
