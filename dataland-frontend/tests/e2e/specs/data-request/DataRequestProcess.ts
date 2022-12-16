import { exportFixturesEuTaxonomyFinancial } from "../../fixtures/eutaxonomy/financials";
import { UPLOAD_MAX_FILE_SIZE } from "../../../../src/utils/Constants";
import { Interception } from "cypress/types/net-stubbing";

describe("As a user I expect a data request page where I can download an excel template, fill it, and submit it", (): void => {
  const inviteInterceptionAlias = "invite";

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

  function uploadDummyExcelFile(filename: string, content: Blob | null = null) {
    cy.get("div[class=p-fileupload-content]").attachFile(
      {
        fileContent: content ? content : new Blob(["File content"]),
        fileName: filename,
        mimeType: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      },
      { subjectType: "drag-n-drop" }
    );
  }

  function submitAndValidateSuccess(moreValidation = (interception: Interception) => {}) {
    interceptInviteAndDisableEmail();
    cy.get("button[name=submit_request_button]").click();
    validateSuccessResponse(moreValidation);
  }

  function interceptInviteAndDisableEmail() {
    cy.intercept("**/api/invite*", (req) => {
      req.headers["DATALAND-NO-EMAIL"] = "true";
    }).as(inviteInterceptionAlias);
  }

  function validateSuccessResponse(moreValidation = (interception: Interception) => {}) {
    cy.wait(`@${inviteInterceptionAlias}`).then((interception) => {
      expect(interception.response!.statusCode).to.be.within(200, 299);
      expect(interception.response!.body.uploadSuccessful).to.equal(true);
      // TODO adapt expect(interception.response!.body.uploadMessage).to.contain(" 2 ");
      moreValidation(interception);
    });
  }

  function getUploadBoxFiles(): string[] {
    const filenames: string[] = [];
    // TODO implement
    expect(filenames.length).to.be.at.most(1);
    return filenames;
  }

  function uploadBoxEntryShouldBe(filename: string) {
    let filenames = getUploadBoxFiles();
    expect(filenames.length).to.equal(1);
    expect(filenames[0]).to.equal(filename);
  }

  function validateErrorMessageContaining(messages: string[]) {
    messages.forEach((it) => {
      cy.get("div[class=p-message-text]").should("contain.text", it);
    });
  }

  function validateSubmitButtonDisabled() {
    cy.get("button[name=submit_request_button]").should("be.disabled");
  }

  /*it(`Test if Excel template for data request is downloadable and assert that it equals the expected Excel file`, () => {
    cy.ensureLoggedIn()
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
    cy.deleteDownloadsFolder();
  });*/

  // it(`Test submitting two files and if the upload request contains the inserted files`, () => {
  //   cy.ensureLoggedIn();
  //   cy.visitAndCheckAppMount("/requests");
  //   //setReloadOnClicksToAvoidPageLoadBug() // TODO what is this bug?
  //
  //   const removeFilename = "remove_file.xlsx";
  //   uploadDummyExcelFile(removeFilename);
  //   uploadBoxEntryShouldBe(removeFilename);
  //   // TODO try and fail to add another file
  //   // TODO remove the inserted file / click X
  //   expect(getUploadBoxFiles().length).to.equal(0);
  //   const keepFilename = "keep_file.xlsx";
  //   uploadDummyExcelFile(keepFilename);
  //   uploadBoxEntryShouldBe(keepFilename); // this is done to validate, that the removal worked
  //
  //   submitAndValidateSuccess((interception) => {
  //     expect(interception.request.body).to.contain(keepFilename);
  //     expect(interception.request.body).to.not.contain(removeFilename);
  //   });
  // });

  // it(`Test that the upload box is empty after a successful submission`, () => {
  //   cy.ensureLoggedIn();
  //   cy.visitAndCheckAppMount("/requests");
  //   uploadDummyExcelFile("accept_test.xlsx", Cypress.Blob.arrayBufferToBlob(new ArrayBuffer(UPLOAD_MAX_FILE_SIZE)));
  //   submitAndValidateSuccess();
  //   expect(getUploadBoxFiles().length).to.equal(0);
  // });

  it(`Test that a too large file gets rejected`, () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/requests");
    const rejectFilename = "reject_test.xlsx";

    uploadDummyExcelFile(rejectFilename, Cypress.Blob.arrayBufferToBlob(new ArrayBuffer(UPLOAD_MAX_FILE_SIZE + 1)));
    expect(getUploadBoxFiles().length).to.equal(0);
    validateErrorMessageContaining([rejectFilename, "Invalid file size"]);
    validateSubmitButtonDisabled();
  });

  it(`Test that a wrong file type gets rejected`, () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/requests");
    const rejectFilename = "reject_test.png";

    uploadDummyExcelFile(rejectFilename, Cypress.Blob.arrayBufferToBlob(new ArrayBuffer(UPLOAD_MAX_FILE_SIZE + 1)));
    expect(getUploadBoxFiles().length).to.equal(0);
    validateErrorMessageContaining([rejectFilename, "Invalid file type"]);
    validateSubmitButtonDisabled();
  });

  // TODO merge this test into a different test
  it(`Test that the submit button is disabled when there is no file to be submitted`, () => {
    cy.ensureLoggedIn();
    cy.visitAndCheckAppMount("/requests");
    validateSubmitButtonDisabled();
  });

  // TODO test reset button, checkbox (how?)
});
