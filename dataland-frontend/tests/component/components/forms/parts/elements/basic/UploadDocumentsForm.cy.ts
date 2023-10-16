import UploadDocumentsForm from "@/components/forms/parts/elements/basic/UploadDocumentsForm.vue";
import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";

describe("Component tests for the UploadDocumentsForm", () => {
  it("Check that error messages are displayed only under the right circumstances", () => {
    cy.mountWithPlugins(UploadDocumentsForm, {
      data() {
        return {
          name: "UploadReports",
        };
      },
    }).then(() => {
      const maximumAcceptedFileSize = 100 * 1024 * 1024;
      uploadDocuments.errorMessage().should("not.exist");
      uploadDocuments.selectDummyFile("justRight", maximumAcceptedFileSize);
      uploadDocuments.errorMessage().should("not.exist");
      uploadDocuments.selectDummyFile("tooLarge", maximumAcceptedFileSize + 1);
      uploadDocuments
        .errorMessage()
        .parent()
        .should("not.have.css", "display", "none")
        .should("contain.text", "tooLarge.pdf: Invalid file size, file size should be smaller than 100 MB.");
      uploadDocuments.dismissErrorMessage();
      uploadDocuments.errorMessage().parent().should("have.css", "display", "none");
      uploadDocuments.selectDummyFileOfType("invalidType", "xlsx", maximumAcceptedFileSize + 1);
      uploadDocuments
        .errorMessage()
        .parent()
        .should("not.have.css", "display", "none")
        .should("contain.text", "invalidType.xlsx: Invalid file type, allowed file types: .pdf.");
    });
  });
});
