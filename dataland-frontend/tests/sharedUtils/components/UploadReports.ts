export const uploadReports = {
  selectFile(filename: string): void {
    cy.get('button[data-test="upload-files-button"]').click();
    cy.get("input[type=file]").selectFile(`../testing/data/documents/${filename}.pdf`, { force: true });
  },
  numberOfReportsToUploadShouldBe(expectedNumberOfReportsToUpload: number): void {
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
  validateReportInFileUploadList(filename: string, fileDimension: string): void {
    // TODO very complicated implementation. WHy not just checking for the uploadContainer of that file??
    //  because there is none for this list, yet...
    //  also this one checks if all data is displayed
    cy.get('div[data-test="files-to-upload"]')
      .should("exist")
      .find('[data-test="files-to-upload-title"]')
      .should("contain", filename + ".pdf");
    cy.get('div[data-test="files-to-upload"]')
      .find('[data-test="files-to-upload-size"]')
      .should("contain", fileDimension);
    cy.get('input[name="reference"]').should("exist");
  },
  validateReportToUploadHasForm(reportName: string): void {
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("exist");
  },
  removeReportToUpload(reportName: string): void {
    cy.get(`[data-test="${reportName}FileUploadContainer"] button`).click();
  },
  removeAllReportsToUpload(): void {
    cy.get('button[data-test="files-to-upload-remove"]').each((element) => Cypress.$(element).click());
  },
  removeUploadedReport(reportName: string): Cypress.Chainable {
    return cy.get(`[data-test="${reportName}AlreadyUploadedContainer"] button`).click();
  },
  checkNoReportIsListed(): void {
    cy.get('div[data-test="files-to-upload"]').should("not.exist");
    cy.get('[data-test="report-to-upload-form"]').should("not.exist");
    cy.get('input[name="reference"]').should("not.exist");
    cy.get('input[name="reportDate"]').should("not.exist");
    // TODO check other lists
  },
  specificReportInfoIsNotListed(reportName: string): void {
    // TODO rename to "specificReportInfoIsNotListedInReportsToUpload" because it only checks there!
    // TODO if we want to check it everywhere, we'd have to also check for the containers of already uploaded reports!
    cy.get(`[data-test="${reportName}ToUploadContainer"]`).should("not.exist");
  },
};
