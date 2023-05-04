export const uploadReports = {
  uploadFile(filename: string): void {
    cy.get('button[data-test="upload-files-button"]').click();
    cy.get("input[type=file]").selectFile(`../testing/data/documents/${filename}.pdf`, { force: true });
  },
  fillAllReportInfoForms(): void {
    cy.get('[data-test="report-info"]').each((element) => {
      cy.wrap(element).find(`[data-test="reportDate"] button`).should("have.class", "p-datepicker-trigger").click();
      cy.get("div.p-datepicker").find('button[aria-label="Previous Month"]').click();
      cy.get("div.p-datepicker").find(`span:contains("12")`).click();
      cy.wrap(element).find(`input[name="currency"]`).clear().type("zzz");
      cy.wrap(element).find(`input[value="No"]`).click();
    });
  },
  validateSingleFileInUploadedList(filename: string, fileDimension: string): void {
    cy.get('div[data-test="uploaded-files"]')
      .should("exist")
      .find('[data-test="uploaded-files-title"]')
      .should("contain", filename + ".pdf");
    cy.get('div[data-test="uploaded-files"]')
      .find('[data-test="uploaded-files-size"]')
      .should("contain", fileDimension);
    cy.get('input[name="reference"]').should("exist");
  },
  fillReportCurrency(filename: string): void {
    cy.get(`[data-test="${filename}ToUploadContainer"]`).find('input[name="currency"]').type("www");
  },
  removeSingleUploadedFileFromUploadedList(): void {
    cy.get('button[data-test="uploaded-files-remove"]').click();
  },
  removeUploadedReportFromReportInfos(filename: string): Cypress.Chainable {
    return cy.get(`[data-test="${filename}AlreadyUploadedContainer"] button`).click();
  },
  checkNoReportIsListed(): void {
    cy.get('div[data-test="uploaded-files"]').should("not.exist");
    cy.get('[data-test="report-info"]').should("not.exist");
    cy.get('input[name="reference"]').should("not.exist");
    cy.get('input[name="reportDate"]').should("not.exist");
  },
};
