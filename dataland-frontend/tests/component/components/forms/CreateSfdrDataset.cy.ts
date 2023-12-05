import CreateSfdrDataset from "@/components/forms/CreateSfdrDataset.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { FiscalYearDeviation } from "../../../../build/clients/backend";
import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";
import { TEST_PDF_FILE_NAME } from "@sharedUtils/ConstantsForPdfs";
describe("Component tests for the CreateSfdrDataset that test report uploading", () => {
  it("Open upload page and upload reports under yes no form fields", () => {
    cy.mountWithPlugins(CreateSfdrDataset, {
      keycloak: minimalKeycloakMock({}),
      data() {
        return {
          companyAssociatedSfdrData: {
            reportingPeriod: "2020",
            data: {
              general: {
                general: {
                  dataDate: "2023-04-30",
                  fiscalYearDeviation: FiscalYearDeviation.Deviation,
                  fiscalYearEnd: "2023-04-30",
                },
              },
            },
          },
        };
      },
    }).then(() => {
      cy.get('[data-test="sustainableAgriculturePolicy"] input[type="radio"][name="value"][value="Yes"]').check();
      uploadDocuments.selectFile(TEST_PDF_FILE_NAME, "sustainableAgriculturePolicy");
      cy.get('[data-test="sustainableOceansAndSeasPolicy"] input[type="radio"][name="value"][value="Yes"]').check();
      uploadDocuments.selectFile(TEST_PDF_FILE_NAME, "sustainableOceansAndSeasPolicy");

      submitButton.buttonAppearsEnabled();
      //cy.intercept
      //submitButton.clickButton();
    });
  });
});
