import { UploadDocuments } from '@sharedUtils/components/UploadDocuments';

export class UploadReports extends UploadDocuments {
  private readonly uploadReportsSelector: string;
  constructor(name: string = 'UploadReports') {
    super(name);
    this.uploadReportsSelector = `div[data-test="upload-reports-${name}"]`;
  }

  fillAllFormsOfReportsSelectedForUpload(expectedNumberOfReportsToUpload: number = 1): void {
    this.validateNumberOfReportsSelectedForUpload(expectedNumberOfReportsToUpload);
    cy.get(`${this.uploadReportsSelector} [data-test="report-to-upload-form"]`).each((element) => {
      cy.task('log', 'About to click publicationDate datepicker dropdown button (report-to-upload-form)');
      cy.wrap(element)
        .find(`[data-test="publicationDate"] button`)
        .should('have.class', 'p-datepicker-dropdown')
        .click();
      cy.task('log', 'About to click Previous Month button in datepicker header (report-to-upload-form)');
      cy.get('.p-datepicker-header').find('button[aria-label="Previous Month"]').click();
      cy.task('log', 'About to click day "12" in datepicker day view (report-to-upload-form)');
      cy.get('.p-datepicker-day-view').find(`span:contains("12")`).click();
    });
  }

  validateReportToUploadHasContainerInTheFileSelector(reportName: string): void {
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}FileUploadContainer"]`).should('exist');
  }

  validateReportIsNotInFileSelector(reportName: string): void {
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}FileUploadContainer"]`).should('not.exist');
  }

  validateNumberOfReportsSelectedForUpload(expectedNumberOfReportsToUpload: number): void {
    cy.get(`${this.uploadReportsSelector} [data-test="report-to-upload-form"]`).should(
      'have.length',
      expectedNumberOfReportsToUpload
    );
  }

  removeAlreadyUploadedReport(reportName: string): Cypress.Chainable {
    cy.task('log', `About to click remove button for already-uploaded report: ${reportName}`);
    return cy.get(`${this.uploadReportsSelector} [data-test="${reportName}AlreadyUploadedContainer"] button`).click();
  }

  removeReportFromSelectionForUpload(reportName: string): void {
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}FileUploadContainer"] button`).click();
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}FileUploadContainer"]`).should('not.exist');
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}ToUploadContainer"]`).should('not.exist');
  }

  validateReportIsNotAlreadyUploadedOrSelectedForUpload(reportName: string): void {
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}FileUploadContainer"]`).should('not.exist');
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}ToUploadContainer"]`).should('not.exist');
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}AlreadyUploadedContainer"]`).should('not.exist');
  }

  validateReportIsListedAsAlreadyUploaded(reportName: string): void {
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}AlreadyUploadedContainer`).should('exist');
  }

  validateReportToUploadHasContainerWithInfoForm(reportName: string): void {
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}ToUploadContainer"]`).should('exist');
  }

  validateReportToUploadIsListedInFileSelectorAndHasInfoForm(reportName: string): void {
    this.validateReportToUploadHasContainerInTheFileSelector(reportName);
    this.validateReportToUploadHasContainerWithInfoForm(reportName);
  }

  validateReportHasNoContainerWithInfoForm(reportName: string): void {
    cy.get(`${this.uploadReportsSelector} [data-test="${reportName}ToUploadContainer"]`).should('not.exist');
  }

  validateReportIsNotInFileSelectorAndHasNoInfoForm(reportName: string): void {
    this.validateReportIsNotInFileSelector(reportName);
    this.validateReportHasNoContainerWithInfoForm(reportName);
  }
}
