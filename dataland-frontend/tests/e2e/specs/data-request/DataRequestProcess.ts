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
      });
  }

  afterEach(cy.deleteDownloadsFolder);

  it(`Test if Excel template for data request is downloadable and assert that it equals the expected Excel file`, () => {
    cy.visitAndCheckAppMount("/requests");
    setReloadOnClicksToAvoidPageLoadBug();

    const expectedPathToDownloadedExcelTemplate = Cypress.config("downloadsFolder") + "/Dataland_Request_Template.xlsx";

    cy.readFile(expectedPathToDownloadedExcelTemplate).should("not.exist");
    cy.get("a[id=download-data-request-excel-template]").click();
    cy.readFile("./public/Dataland_Request_Template.xlsx", "binary", { timeout: 15000 }).then(
      (expectedExcelTemplateBinary) => {
        cy.readFile(expectedPathToDownloadedExcelTemplate, "binary", { timeout: 15000 }).should(
          "eq",
          expectedExcelTemplateBinary
        );
      }
    );
  });
  // TODO More test cases:   Upload an excel file,  ... (?)
});
