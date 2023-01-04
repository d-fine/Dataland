import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "../../../../src/utils/Constants";
import { Interception } from "cypress/types/net-stubbing";
import { describeIf } from "@e2e/support/TestUtility";
import { InviteMetaInfoEntity } from "@clients/backend";

describe("As a user I expect a data request page where I can download an excel template, fill it, and submit it", (): void => {
  describeIf(
    "Do not execute these tests in the CD pipeline to prevent sending emails",
    {
      executionEnvironments: ["developmentLocal", "ci"],
      dataEnvironments: ["fakeFixtures"],
    },
    (): void => {
      let inviteInterceptionAlias = "invite";

      const uploadBoxSelector = "div.p-fileupload-content";
      const uploadBoxEmptySelector = "div.p-fileupload-empty";
      const uploadFilenameSelector = "span.font-semibold.mr-2";
      const submitButtonSelector = "button[name=submit_request_button]";
      const resetButtonSelector = "button[name=reset_request_button]";
      const removeButtonSelector = 'img[alt="remove-file-button"]';

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

      function submit(): void {
        cy.get(submitButtonSelector).click();
      }

      function submitAndValidateSuccess(
        moreValidation: (interception: Interception) => void = (): void => undefined
      ): void {
        inviteInterceptionAlias = Math.random().toString();
        cy.intercept("**/api/invite*").as(inviteInterceptionAlias);
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

      function uploadBoxShouldBeEmpty(): void {
        cy.get(uploadBoxSelector).find(uploadBoxEmptySelector);
        cy.get(uploadBoxSelector).find(uploadFilenameSelector).should("not.exist");
      }

      function removeFileFromUploadBox(): void {
        cy.get(uploadBoxSelector).find(removeButtonSelector).click();
      }

      function validateThatErrorMessageContains(substrings: string[]): void {
        substrings.forEach((it) => {
          cy.get("div.p-message-text").should("contain.text", it);
        });
      }

      function validateThatSubmitButtonIsDisabled(): void {
        cy.get(submitButtonSelector).should("be.disabled");
      }

      function setHideUsernameCheckbox(hideUsername: boolean): void {
        if (hideUsername) {
          cy.get("input[type=checkbox]").check({ force: true });
        } else {
          cy.get("input[type=checkbox]").uncheck({ force: true });
        }
      }

      function validateHideUsernameCheckboxIs(hideUsername: boolean): void {
        cy.get("input[type=checkbox]").should((hideUsername ? "" : "not.") + "be.checked");
      }

      function reset(areYouSure: boolean): void {
        const resetDialogSelector = "div.p-dialog";
        cy.get(resetButtonSelector)
          .click()
          .then(() => {
            if (areYouSure) {
              cy.get(resetDialogSelector).find("button[aria-label=Yes]").click();
            } else {
              cy.get(resetDialogSelector).find("button[aria-label=No]").click();
            }
          });
      }

      beforeEach(() => {
        cy.ensureLoggedIn();
        cy.visitAndCheckAppMount("/requests");
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
        cy.readFile("./public/Dataland_Request_Template.xlsx", "binary", { timeout: 15000 }).then(
          (expectedExcelTemplateBinary) => {
            cy.readFile(expectedPathToDownloadedExcelTemplate, "binary", { timeout: 15000 }).should(
              "eq",
              expectedExcelTemplateBinary
            );
          }
        );
        cy.deleteDownloadsFolder();
      });

      it(`Test overriding and removing files from the upload box`, { scrollBehavior: false }, () => {
        const overrideFile = "override_file.xlsx";
        cy.wait(2000);
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
        });
      });

      it(`Test that the reset button works as expected`, () => {
        const removeFilename = "remove_test.xlsx";
        uploadDummyExcelFile(removeFilename);
        setHideUsernameCheckbox(true);

        reset(false);
        validateHideUsernameCheckboxIs(true);
        uploadBoxEntryShouldBe(removeFilename);

        reset(true);
        validateHideUsernameCheckboxIs(false);
        uploadBoxShouldBeEmpty();
      });

      it(`Test if the checkbox state is transferred correctly to the request`, () => {
        uploadDummyExcelFile("test.xlsx");
        submitAndValidateSuccess((interception: Interception) => {
          expect(interception.request.url.includes("isSubmitterNameHidden=false")).to.eq(true);
        });

        cy.get('button[name="back_to_home_button"]')
          .click()
          .get("img.d-triangle-down")
          .click()
          .get("a#profile-picture-dropdown-data-request-button")
          .click();

        uploadBoxShouldBeEmpty();
        uploadDummyExcelFile("test.xlsx");
        setHideUsernameCheckbox(true);
        submitAndValidateSuccess((interception: Interception) => {
          expect(interception.request.url.includes("isSubmitterNameHidden=true")).to.eq(true);
        });

        cy.get("#new-data-request div.p-card-content div.flex.align-items-center").click();
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
