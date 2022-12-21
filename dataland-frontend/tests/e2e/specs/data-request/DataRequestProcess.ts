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

      function submitAndValidateSuccess(moreValidation: (interception: Interception) => void): void {
        interceptInviteAndDisableEmail();
        cy.get(submitButtonSelector).click();
        validateSuccessResponse(moreValidation);
      }

      function interceptInviteAndDisableEmail(): void {
        cy.intercept("**/api/invite*", (req) => {
          req.headers["DATALAND-NO-EMAIL"] = "true";
        }).as(inviteInterceptionAlias);
      }

      function validateSuccessResponse(moreValidation: (interception: Interception) => void): void {
        cy.wait(`@${inviteInterceptionAlias}`).then((interception) => {
          expect(interception.response!.statusCode).to.be.within(200, 299);
          expect((interception.response!.body as InviteMetaInfoEntity).wasInviteSuccessful).to.equal(true);
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

      function visitRequestPage(): void {
        cy.visitAndCheckAppMount("/requests");
      }

      beforeEach(() => {
        cy.ensureLoggedIn();
        visitRequestPage();
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

      it(`Test overriding and removing files from the upload box`, () => {
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
        uploadDummyExcelFile(smallEnoughFilename);

        submitAndValidateSuccess((interception) => {
          expect(interception.request.body).to.contain(smallEnoughFilename);
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
        // TODO check if submitting empty file yields a fail response via error message
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

      it(`Test that the unchecked checkbox state is transferred correctly to the request`, () => {
        uploadDummyExcelFile("test.xlsx");
        submitAndValidateSuccess((interception: Interception) => {
          expect(interception.request.url.includes("isSubmitterNameHidden=false")).to.eq(true);
        });

        visitRequestPage(); // TODO replace this by the / a button from the submission success page

        uploadDummyExcelFile("test.xlsx");
        setHideUsernameCheckbox(true);
        submitAndValidateSuccess((interception: Interception) => {
          expect(interception.request.url.includes("isSubmitterNameHidden=true")).to.eq(true);
        });
      });

      // TODO merge this test into a different test
      it(`Test the submit button and the upload screen`, () => {
        validateThatSubmitButtonIsDisabled();
        // TODO test progressbar for correct color, depending on percentage.
        // TODO check existence of return to home
      });

      it(`Test the failure response screen`, () => {
        // TODO intercept to get a non 200 request
      });

      // TODO check if the request view got reset when you revisit it after a submission
      // TODO BURGER MENU!
    }
  );
});
