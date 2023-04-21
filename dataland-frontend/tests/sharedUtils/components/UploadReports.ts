export const uploadReports = {
  uploadFile(filename: string): void {
    cy.get('button[data-test="upload-files-button"]').click();
    cy.get("input[type=file]").selectFile(`tests/e2e/fixtures/${filename}`, { force: true });
  },
  validateSingleFileInUploadedList(filename = ".pdf", fileDimension: string): void {
    cy.get('div[data-test="uploaded-files"]')
      .should("exist")
      .find('[data-test="uploaded-files-title"]')
      .should("contain", filename);
    cy.get('div[data-test="uploaded-files"]')
      .find('[data-test="uploaded-files-size"]')
      .should("contain", fileDimension);
  },
  validateSingleFileInfo(): void {
    cy.get('[data-test="report-info"]').find('input[name="currency"]').type("www");
  },
  removeSingleUploadedFileFromUploadedList(): void {
    cy.get('button[data-test="uploaded-files-remove"]').click();
  },
  checkNoReportIsListed(): void {
    cy.get('div[data-test="uploaded-files"]').should("not.exist");
    cy.get('[data-test="report-info"]').should("not.exist");
  },
};
