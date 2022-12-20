import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "../../../../src/utils/Constants";
import { Interception } from "cypress/types/net-stubbing";
import { describeIf } from "@e2e/support/TestUtility";

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
      const resetButtonSelector = "button[name=reset_request_button]";
      const removeButtonSelector = 'img[alt="remove-file-button"]';

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

      function uploadDummyExcelFile(filename: string, contentSize = 1): void {
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

      function submitAndValidateSuccess(moreValidation = (interception: Interception): void => undefined): void {
        interceptInviteAndDisableEmail();
        cy.get(submitButtonSelector).click();
        validateSuccessResponse(moreValidation);
      }

      function interceptInviteAndDisableEmail(): void {
        cy.intercept("**/api/invite*", (req) => {
          req.headers["DATALAND-NO-EMAIL"] = "true";
        }).as(inviteInterceptionAlias);
      }

      function validateSuccessResponse(moreValidation = (interception: Interception): void => undefined): void {
        cy.wait(`@${inviteInterceptionAlias}`).then((interception) => {
          expect(interception.response!.statusCode).to.be.within(200, 299);
          expect(interception.response!.body.wasInviteSuccessful!).to.equal(true);
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

      function visit(): void {
        cy.visitAndCheckAppMount("/requests");
      }

      beforeEach(() => {
        cy.ensureLoggedIn();
        visit();
      });

      it(`Test if Excel template for data request is downloadable and assert that it equals the expected Excel file`, () => {
        setReloadOnClicksToAvoidPageLoadBug();

        const expectedPathToDownloadedExcelTemplate =
          Cypress.config("downloadsFolder") + "/Dataland_Request_Template.xlsx";

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
      });

      it(`Test submitting two files and if the upload request contains the inserted files`, () => {
        const overrideFile = "override_file.xlsx";
        uploadDummyExcelFile(overrideFile);
        uploadBoxEntryShouldBe(overrideFile);

        const removeFilename = "remove_file.xlsx";
        uploadDummyExcelFile(removeFilename);
        uploadBoxEntryShouldBe(removeFilename);
        removeFileFromUploadBox();
        uploadBoxShouldBeEmpty();

        const smallEnoughFilename = "small_enough_file.xlsx";
        // also test that maximum filesize is really accepted here
        uploadDummyExcelFile(smallEnoughFilename, UPLOAD_MAX_FILE_SIZE_IN_BYTES);

        submitAndValidateSuccess((interception) => {
          expect(interception.request.body).to.contain(smallEnoughFilename);
          expect(interception.request.body).to.not.contain(overrideFile);
        });
      });

      it(`Test that a too large file gets rejected`, () => {
        const rejectFilename = "reject_test.xlsx";
        uploadDummyExcelFile(rejectFilename, UPLOAD_MAX_FILE_SIZE_IN_BYTES + 1);
        uploadBoxShouldBeEmpty();
        validateThatErrorMessageContains([rejectFilename, "Invalid file size"]);
        validateThatSubmitButtonIsDisabled();
      });

      it(`Test that a wrong file type gets rejected`, () => {
        const rejectFilename = "reject_test.png";

        uploadDummyExcelFile(rejectFilename);
        uploadBoxShouldBeEmpty();
        validateThatErrorMessageContains([rejectFilename, "Invalid file type"]);
        validateThatSubmitButtonIsDisabled();
      });

      // TODO merge this test into a different test
      it(`Test that the submit button is disabled when there is no file to be submittable`, () => {
        validateThatSubmitButtonIsDisabled();
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

        reset(false);
        validateHideUsernameCheckboxIs(false);
        uploadBoxShouldBeEmpty();
      });

      it(`Test that the unchecked checkbox state is transferred correctly to the request`, () => {
        uploadDummyExcelFile("test.xlsx");
        submitAndValidateSuccess((interception: Interception) => {
          expect(interception.request.url.includes("isSubmitterNameHidden=false")).to.eq(true);
        });
      });

      it(`Test that the checked checkbox state is transferred correctly to the request`, () => {
        uploadDummyExcelFile("test.xlsx");
        setHideUsernameCheckbox(true);
        submitAndValidateSuccess((interception: Interception) => {
          expect(interception.request.url.includes("isSubmitterNameHidden=true")).to.eq(true);
        });
      });

      // TODO check if the request view got reset when you revisit it after a submission
      // TODO check if submitting empty file yields a fail response
    }
  );
});
