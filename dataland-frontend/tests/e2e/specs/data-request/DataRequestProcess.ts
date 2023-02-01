import { Interception } from "cypress/types/net-stubbing";
import { describeIf } from "@e2e/support/TestUtility";
import { InviteMetaInfoEntity } from "@clients/backend";
import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";

describe("As a user I expect a data request page where I can download an excel template, fill it, and submit it", (): void => {
  describeIf(
    "Do not execute these tests in the CD pipeline to prevent sending emails",
    {
      executionEnvironments: ["developmentLocal", "ci"],
      dataEnvironments: ["fakeFixtures"],
    },
    (): void => {
      const inviteInterceptionAlias = "invite";

      const uploadBoxSelector = "div.p-fileupload-content";
      const uploadBoxEmptySelector = "div.p-fileupload-empty";
      const uploadFilenameSelector = "span.font-semibold.mr-2";
      const submitButtonSelector = "button[name=submit_request_button]";
      const removeButtonSelector = 'img[alt="remove-file-button"]';

      /**
       * Selects an empty file with the given filename and contentsize
       *
       * @param filename the name of the dummy file to create
       * @param contentSize the size of the dummy file
       */
      function uploadDummyExcelFile(filename = "test.xlsx", contentSize = 1): void {
        cy.get(uploadBoxSelector).selectFile(
          {
            contents: new Cypress.Buffer(contentSize),
            fileName: filename,
            mimeType: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            lastModified: Date.now(),
          },
          { action: "drag-drop" }
        );
      }

      /**
       * submits the upload form
       */
      function submit(): void {
        cy.get(submitButtonSelector).click();
      }

      /**
       * Submits the upload form and intercepts the api request. Performs validation on the response status.
       * The moreValidation function may be supplied to perform further validation
       *
       * @param moreValidation a function that can be used to impose further restraints on the intercepted api message
       */
      function submitAndValidateSuccess(
        moreValidation: (interception: Interception) => void = (): void => undefined
      ): void {
        submit();
        cy.wait(`@${inviteInterceptionAlias}`).then((interception) => {
          if (interception.response === undefined) {
            expect(interception.response).not.to.equal(undefined);
            return;
          }
          expect(interception.response.statusCode).to.be.within(200, 399);
          if (interception.response.statusCode < 300) {
            expect((interception.response.body as InviteMetaInfoEntity).wasInviteSuccessful).to.equal(true);
          }
          moreValidation(interception);
        });
      }

      /**
       * Verifies that the upload box shows a file with the given filename
       *
       * @param filename the filename that is expected to be present in the upload box
       */
      function uploadBoxEntryShouldBe(filename: string): void {
        cy.get(uploadBoxSelector).find(uploadBoxEmptySelector).should("not.exist");
        cy.get(uploadBoxSelector)
          .find(uploadFilenameSelector)
          .then((elements) => {
            elements.each((index, element) => {
              expect(element.innerText).to.equal(filename);
            });
          })
          .its("length")
          .should("eq", 1);
      }

      /**
       * Asserts that the upload box is empty and shows the upload file div
       */
      function uploadBoxShouldBeEmpty(): void {
        cy.get(uploadBoxSelector).find(uploadBoxEmptySelector);
        cy.get(uploadBoxSelector).find(uploadFilenameSelector).should("not.exist");
      }

      /**
       * Clicks on the remove file button in the upload box
       */
      function removeFileFromUploadBox(): void {
        cy.get(uploadBoxSelector).find(removeButtonSelector).click();
      }

      /**
       * Assets that the error message contains all strings in the provided array
       *
       * @param substrings an array of strings that are required to be present in the error message
       */
      function validateThatErrorMessageContains(substrings: string[]): void {
        substrings.forEach((it) => {
          cy.get("div.p-message-text").should("contain.text", it);
        });
      }

      /**
       * Assets that the submit button is disabled
       */
      function validateThatSubmitButtonIsDisabled(): void {
        cy.get(submitButtonSelector).should("be.disabled");
      }

      /**
       * Checks/unchecks the hideUsername checkbox depending on the provided boolean
       *
       * @param hideUsername whether to check or to uncheck the hide username checkbox
       */
      function setHideUsernameCheckbox(hideUsername: boolean): void {
        if (hideUsername) {
          cy.get("input[type=checkbox]").check({ force: true });
        } else {
          cy.get("input[type=checkbox]").uncheck({ force: true });
        }
      }

      beforeEach(() => {
        cy.ensureLoggedIn();
        cy.visitAndCheckAppMount("/requests");
        cy.intercept("**/api/invite*").as(inviteInterceptionAlias);
      });

      it(`Test if Excel template for data request is downloadable and assert that it equals the expected Excel file`, () => {
        const expectedPathToDownloadedExcelTemplate =
          Cypress.config("downloadsFolder") + "/Dataland_Request_Template.xlsx";

        cy.readFile(expectedPathToDownloadedExcelTemplate).should("not.exist");
        const downloadLinkSelector = "a[id=download-data-request-excel-template]";
        const downloadAlias = "download";
        cy.intercept("**/Dataland_Request_Template.xlsx").as(downloadAlias);
        cy.get(downloadLinkSelector).click();
        cy.wait(`@${downloadAlias}`);
        cy.readFile("./public/Dataland_Request_Template.xlsx", "binary", {
          timeout: Cypress.env("medium_timeout_in_ms") as number,
        }).then((expectedExcelTemplateBinary) => {
          cy.readFile(expectedPathToDownloadedExcelTemplate, "binary", {
            timeout: Cypress.env("medium_timeout_in_ms") as number,
          }).should("eq", expectedExcelTemplateBinary);
        });
        cy.task("deleteFolder", Cypress.config("downloadsFolder"));
      });

      it(`Test overriding and removing files from the upload box`, { scrollBehavior: false }, () => {
        const overrideFile = "override_file.xlsx";
        cy.wait(2000); // TODO rather wait for specific stuff than random timespans
        uploadDummyExcelFile(overrideFile);
        uploadBoxEntryShouldBe(overrideFile);

        const removeFilename = "remove_file.xlsx";
        uploadDummyExcelFile(removeFilename);
        uploadBoxEntryShouldBe(removeFilename);
        removeFileFromUploadBox();
        uploadBoxShouldBeEmpty();

        const sufficientlySmallFilename = "sufficiently_small_file.xlsx";
        uploadDummyExcelFile(sufficientlySmallFilename);

        submitAndValidateSuccess((interception) => {
          expect(interception.request.body).to.contain(sufficientlySmallFilename);
          expect(interception.request.body).to.not.contain(removeFilename);
          expect(interception.request.body).to.not.contain(overrideFile);
        });
      });

      it(`Test that the right error messages are displayed at the right time`, () => {
        const tooLargeFilename = "slightly_too_large.xlsx";
        const wrongTypeFilename = "wrong_type.png";
        [
          {
            filename: tooLargeFilename,
            fileSize: UPLOAD_MAX_FILE_SIZE_IN_BYTES + 1,
            errorMessage: "Invalid file size",
          },
          {
            filename: wrongTypeFilename,
            fileSize: 1,
            errorMessage: "Invalid file type",
          },
        ].forEach((it) => {
          uploadDummyExcelFile(it.filename, it.fileSize);
          uploadBoxShouldBeEmpty();
          validateThatErrorMessageContains([it.filename, it.errorMessage]);
          validateThatSubmitButtonIsDisabled();
          cy.get("button.p-message-close").click();
        });
      });

      it(`Test if the checkbox state is transferred correctly to the request`, () => {
        uploadDummyExcelFile("test.xlsx");
        submitAndValidateSuccess((interception: Interception) => {
          expect(interception.request.url.includes("isSubmitterNameHidden=false")).to.eq(true);
        });

        cy.get('button[name="back_to_home_button"]').click();
        cy.wait(3000); // TODO rather wait for specific stuff than random timespans
        cy.get("img.d-triangle-down").click().get("a#profile-picture-dropdown-data-request-button").click();
        cy.wait(2000); // TODO rather wait for specific stuff than random timespans
        uploadBoxShouldBeEmpty();
        uploadDummyExcelFile("test.xlsx");
        setHideUsernameCheckbox(true);
        submitAndValidateSuccess((interception: Interception) => {
          expect(interception.request.url.includes("isSubmitterNameHidden=true")).to.eq(true);
        });

        cy.get("#new-data-request div.p-card-content div.flex.align-items-center img").should("exist");
        cy.get("#new-data-request div.p-card-content div.flex.align-items-center a").click();
        uploadBoxShouldBeEmpty();
      });

      it(`Test the submit button and the upload success screen`, () => {
        validateThatSubmitButtonIsDisabled();
        uploadDummyExcelFile();
        submitAndValidateSuccess();
        const finishedTextSelector = "p.progressbar-finished";
        cy.get(finishedTextSelector).then((element: JQuery<HTMLElement>) => {
          expect(element.text()).to.equal("100%");
        });
        cy.get("div.p-message-bordered-success").should("contain.text", "submit");
      });

      it(`Test the failure response screen`, () => {
        const errorMessageSelector = "div#result-message-container";
        const titleSelector = "h1#current-progress-title";
        uploadDummyExcelFile("test.xlsx", 0);
        submit();
        cy.get(errorMessageSelector).find("div").should("contain.text", "Excel file is empty.");
        cy.get(titleSelector).should("contain.text", "Submission failed");
      });
    }
  );
});
