export const uploadReports = {
  uploadFile(filename: string) {
    cy.get('button[data-test="upload-files-button"]').click();
    cy.get("input[type=file]").selectFile(`tests/e2e/fixtures/${filename}`, { force: true });
  },
  fillAllReportInfoForms() {
    cy.get('[data-test="report-info"]').each((element) => {
      cy.wrap(element).find(`[data-test="reportDate"] button`).should("have.class", "p-datepicker-trigger").click();
      cy.get("div.p-datepicker").find('button[aria-label="Previous Month"]').click();
      cy.get("div.p-datepicker").find(`span:contains("12")`).click();
      cy.wrap(element).find(`input[name="currency"]`).type("zzz")
      cy.wrap(element).find(`input[value="No"]`).click()
    })
  },
  validateSingleFileInUploadedList(filename = ".pdf", fileDimension: string) {
    cy.get('div[data-test="uploaded-files"]')
      .should("exist")
      .find('[data-test="uploaded-files-title"]')
      .should("contain", filename);
    cy.get('div[data-test="uploaded-files"]').find('[data-test="uploaded-files-size"]').should("contain", fileDimension);
  },
  validateSingleFileInfo() {
    cy.get('[data-test="report-info"]').find('input[name="currency"]').type("www");
  },
  removeSingleUploadedFileFromUploadedList() {
    cy.get('button[data-test="uploaded-files-remove"]').click();
  },
  checkNoReportIsListed() {
    cy.get('div[data-test="uploaded-files"]').should("not.exist");
    cy.get('[data-test="report-info"]').should("not.exist");
  },
}