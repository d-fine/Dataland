export const uploadReports = {
  selectFile(filename: string): void {
    cy.get('button[data-test="upload-files-button"]').click();
    cy.get("input[type=file]").selectFile(`../testing/data/documents/${filename}.pdf`, { force: true });
  },
  fillAllReportInfoForms(): void {
    cy.get('[data-test="report-info"]').each((element) => {
      cy.wrap(element).find(`[data-test="reportDate"] button`).should("have.class", "p-datepicker-trigger").click();
      cy.get("div.p-datepicker").find('button[aria-label="Previous Month"]').click();
      cy.get("div.p-datepicker").find(`span:contains("12")`).click();
      cy.wrap(element).find(`input[name="currency"]`).clear().type("zzz"); // TODO we have a method  "fillReportCurrency".  Why do we have that if we fill it manually here?
      cy.wrap(element).find(`input[value="No"]`).click();
    });
  },
  validateSingleFileInUploadList(filename: string, fileDimension: string): void {
    // TODO very complicated implementation. WHy not just checking for the uploadContainer of that file??
    cy.get('div[data-test="files-to-upload"]')
      .should("exist")
      .find('[data-test="files-to-upload-title"]')
      .should("contain", filename + ".pdf");
    cy.get('div[data-test="files-to-upload"]')
      .find('[data-test="files-to-upload-size"]')
      .should("contain", fileDimension);
    cy.get('input[name="reference"]').should("exist");
  },
  fillReportCurrency(filename: string): void {
    // TODO we have a "fillAllReportInfoForms" method.  Why do we need this one here then?
    cy.get(`[data-test="${filename}ToUploadContainer"]`).find('input[name="currency"]').type("www");
  },
  removeSingleFileFromUploadList(): void {
    // TODO it is unsafe to use this since it breaks as soon as multiple files are on the upload list
    // TODO instead it should accept a file name and explicitly click the remove button for THAT specific file
    // TODO e.g. compare it to "removeUploadedReportFromReportInfos"
    cy.get('button[data-test="files-to-upload-remove"]').click();
  },
  removeAllFilesFromUploadList(): void {
    cy.get('button[data-test="files-to-upload-remove"]').each((element) => Cypress.$(element).click());
  },
  removeUploadedReportFromReportInfos(filename: string): Cypress.Chainable {
    return cy.get(`[data-test="${filename}AlreadyUploadedContainer"] button`).click();
  },
  checkNoReportIsListed(): void {
    cy.get('div[data-test="files-to-upload"]').should("not.exist");
    cy.get('[data-test="report-info"]').should("not.exist");
    cy.get('input[name="reference"]').should("not.exist");
    cy.get('input[name="reportDate"]').should("not.exist");
  },
  specificReportInfoIsNotListed(filename: string): void {
    // TODO rename to "specificReportInfoIsNotListedInReportsToUpload" because it only checks there!
    // TODO if we want to check it everywhere, we'd have to also check for the containers of already uploaded reports!
    cy.get(`[data-test="${filename}ToUploadContainer"]`).should("not.exist");
  },
};
