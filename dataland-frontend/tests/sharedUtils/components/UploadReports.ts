export const uploadReports = {
  selectFile(filename: string): void {
    cy.get('button[data-test="upload-files-button"]').click();
    cy.get("input[type=file]").selectFile(`../testing/data/documents/${filename}.pdf`, { force: true });
  },
  selectDummyFile(filename: string, contentSize: number): void {
    cy.get('button[data-test="upload-files-button"]').click();
    cy.get("input[type=file]").selectFile(
      {
        contents: new Cypress.Buffer(contentSize),
        fileName: `${filename}.pdf`,
        mimeType: "application/pdf",
      },
      { force: true }
    );
  },
  numberOfReportsToUploadShouldBe(expectedNumberOfReportsToUpload: number): void {
    cy.get('[data-test="report-to-upload-form"]').should("have.length", expectedNumberOfReportsToUpload);
    cy.get('[data-test="report-to-upload-form"]').should("have.length", expectedNumberOfReportsToUpload);
  },
  fillAllReportsToUploadForms(expectedNumberOfReportsToUpload?: number): void {
    if (expectedNumberOfReportsToUpload) {
      this.numberOfReportsToUploadShouldBe(expectedNumberOfReportsToUpload);
    }
    cy.get('[data-test="report-to-upload-form"]').each((element) => {
      cy.wrap(element).find(`[data-test="reportDate"] button`).should("have.class", "p-datepicker-trigger").click();
      cy.get("div.p-datepicker").find('button[aria-label="Previous Month"]').click();
      cy.get("div.p-datepicker").find(`span:contains("12")`).click();
      cy.wrap(element).find(`input[name="currency"]`).clear().type("zzz");
      cy.wrap(element).find(`input[value="No"]`).click();
    });
  },
  validateReportToUploadIsListed(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"]`).should("exist");
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("exist");
  },
  removeReportToUpload(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"] button`).click();
  },
  removeAllReportsToUpload(): void {
    cy.get('button[data-test="files-to-upload-remove"]').each((element) => Cypress.$(element).trigger("click"));
  },
  removeUploadedReport(reportName: string): Cypress.Chainable {
    return cy.get(`[data-test="${reportName}AlreadyUploadedContainer"] button`).click();
  },
  checkNoReportIsListed(): void {
    cy.get('[data-test="files-to-upload"]').should("not.exist");
    cy.get('[data-test="report-to-upload-form"]').should("not.exist");
    cy.get('[data-test="report-uploaded-form"]').should("not.exist");
  },
  reportIsNotListed(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"]`).should("not.exist");
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("not.exist");
    cy.get(`[data-test="${reportName}AlreadyUploadedContainer"]`).should("not.exist");
  },
};
