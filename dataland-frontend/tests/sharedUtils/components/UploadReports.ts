export const uploadReports = {
  uploadFile(filename: string): void {
    cy.get('button[data-test="upload-files-button"]').click();
    cy.get("input[type=file]").selectFile(`../testing/data/${filename}.pdf`, { force: true });
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
  validateSingleFileInfo(filename: string): void {
    cy.get(`[data-test="${filename}ToUploadContainer"]`).find('input[name="currency"]').type("www"); // TODO this is typing, not a validation?
  },
  removeSingleUploadedFileFromUploadedList(): void {
    cy.get('button[data-test="uploaded-files-remove"]').click();
  },
  checkNoReportIsListed(): void {
    cy.get('div[data-test="uploaded-files"]').should("not.exist");
    cy.get('[data-test="report-info"]').should("not.exist");
    cy.get('input[name="reference"]').should("not.exist");
    cy.get('input[name="reportDate"]').should("not.exist");
  },
};
