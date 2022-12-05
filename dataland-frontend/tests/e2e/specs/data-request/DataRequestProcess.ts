describe("As a user I expect a data request page where I can download an excel template, fill it, and submit it", (): void => {
  function setReloadOnClicksToAvoidPageLoadBug(): void {
    cy.window()
      .document()
      .then((document) => {
        document.addEventListener("click", () => {
          setTimeout(function () {
            document.location.reload();
          }, 5000);
        });
        cy.get("a[id=download-data-request-excel-template]").click();
      });
  }

  it(`Test if Excel template for data request is downloadable and assert that it equals the expected Excel file`, () => {
    cy.visitAndCheckAppMount("/requests");
    setReloadOnClicksToAvoidPageLoadBug();

    const downloadedFilename = Cypress.config("downloadsFolder") + "/Dataland_Request_Template.xlsx";

    cy.readFile("./src/assets/excel-files/Dataland_Request_Template.xlsx", "base64", { timeout: 15000 }).then(
      (expectedBase64EncodedExcelTemplate) => {
        cy.readFile(downloadedFilename, "base64", { timeout: 15000 }).should("eq", expectedBase64EncodedExcelTemplate);
      }
    );
  });

  // TODO More test cases:   Upload an excel file,  ... (?)
});
