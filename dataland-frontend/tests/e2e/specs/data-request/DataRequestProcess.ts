import { exportFixturesEuTaxonomyFinancial } from "../../fixtures/eutaxonomy/financials";
import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "../../../../src/utils/Constants";
import { Interception } from "cypress/types/net-stubbing";
import { describeIf } from "@e2e/support/TestUtility";

describe("As a user I expect a data request page where I can download an excel template, fill it, and submit it", (): void => {
  describeIf("Do not execute these tests in the CD pipeline to prevent sending emails", {
    executionEnvironments: ["developmentLocal", "ci"],
    dataEnvironments: ["fakeFixtures"],
    }, (): void => {
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
        moreValidation(interception);
      });
    }

    function getUploadBoxFiles(): string[] {
      const filenames: string[] = [];
      let uploadContainerTag = "div[class=p-fileupload-content]"
      let fileTag = "span[class=font-semibold]";
      cy.get(uploadContainerTag).then(currentSubject =>
        cy.log(currentSubject.length.toString())
      );
      expect(filenames.length).to.be.at.most(1);
      return filenames;
    }

    function uploadBoxEntryShouldBe(filename: string) {
      let filenames = getUploadBoxFiles();
      expect(filenames.length).to.equal(1);
      expect(filenames[0]).to.equal(filename);
    }

    function validateThatErrorMessageContains(messages: string[]) {
      messages.forEach((it) => {
        cy.get("div[class=p-message-text]").should("contain.text", it);
      });
    }

    function validateThatSubmitButtonIsDisabled() {
      cy.get("button[name=submit_request_button]").should("be.disabled");
    }

    function setHideUsernameCheckbox(hideUsername: boolean) {
      if(hideUsername) {
        cy.get("input[type=checkbox]").check({force: true});
      } else {
        cy.get("input[type=checkbox]").uncheck({force: true});
      }
    }

    function validateHideUsernameCheckboxIs(hideUsername: boolean) {
      cy.get("input[type=checkbox]").should((hideUsername ? "" : "not.") + "be.checked")
    }

    function reset(areYouSure: boolean) {
      // TODO answer are you sure dialog
      cy.get("button[name=reset_request_button]")
    }

    beforeEach(() => {
      cy.ensureLoggedIn();
      cy.visitAndCheckAppMount("/requests");
    });

    /*it(`Test if Excel template for data request is downloadable and assert that it equals the expected Excel file`, () => {
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
    //   uploadDummyExcelFile("accept_test.xlsx", Cypress.Blob.arrayBufferToBlob(new ArrayBuffer(UPLOAD_MAX_FILE_SIZE)));
    //   submitAndValidateSuccess();
    //   expect(getUploadBoxFiles().length).to.equal(0);
    // });

    // it(`Test that a too large file gets rejected`, () => {
    //   const rejectFilename = "reject_test.xlsx";
    //   uploadDummyExcelFile(rejectFilename, Cypress.Blob.arrayBufferToBlob(
    //       new ArrayBuffer(UPLOAD_MAX_FILE_SIZE_IN_BYTES + 1)
    //   ));
    //   expect(getUploadBoxFiles().length).to.equal(0);
    //   validateThatErrorMessageContains([rejectFilename, "Invalid file size"]);
    //   validateThatSubmitButtonIsDisabled();
    // });
    //
    // it(`Test that a wrong file type gets rejected`, () => {
    //   const rejectFilename = "reject_test.png";
    //
    //   uploadDummyExcelFile(rejectFilename, Cypress.Blob.arrayBufferToBlob(
    //       new ArrayBuffer(UPLOAD_MAX_FILE_SIZE_IN_BYTES + 1)
    //   ));
    //   expect(getUploadBoxFiles().length).to.equal(0);
    //   validateThatErrorMessageContains([rejectFilename, "Invalid file type"]);
    //   validateThatSubmitButtonIsDisabled();
    // });
    //
    // // TODO merge this test into a different test
    // it(`Test that the submit button is disabled when there is no file to be submitted`, () => {
    //   validateThatSubmitButtonIsDisabled();
    // });

    // it(`Test that the reset button works as expected`, () => {
    //   const removeFilename = "remove_test.xlsx";
    //   uploadDummyExcelFile(removeFilename);
    //   setHideUsernameCheckbox(true);
    //
    //   reset(false);
    //   validateHideUsernameCheckboxIs(true);
    //   uploadBoxEntryShouldBe(removeFilename);
    //
    //   reset(true);
    //   validateHideUsernameCheckboxIs(false);
    //   expect(getUploadBoxFiles().length).to.equal(0);
    //
    //   reset(false);
    //   validateHideUsernameCheckboxIs(false);
    // });

    it(`Test that the checkbox state is correctly  in the request`, () => {
      // uploadDummyExcelFile("test.xlsx");
      // submitAndValidateSuccess((interception) => {
      //   expect(interception.request.body)
      // });
      //
      // uploadDummyExcelFile("test.xlsx");
      // setHideUsernameCheckbox(true);
      //cy.log(getUploadBoxFiles().length.toString());
      uploadDummyExcelFile("test.xlsx");
      cy.log(getUploadBoxFiles().length.toString());
      uploadDummyExcelFile("test2.xlsx");
      cy.log(getUploadBoxFiles().length.toString());
    });
  });
});