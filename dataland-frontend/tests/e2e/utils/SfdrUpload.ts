import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";
import { TEST_PDF_FILE_NAME } from "@sharedUtils/ConstantsForPdfs";

/**
 * Adds reports to the dataset via the Sfdr upload form for the given dataset
 */
export function selectsReportsForUploadInSfdrForm(): void {
  uploadDocuments.selectFile(TEST_PDF_FILE_NAME, "referencedReports");
  uploadDocuments.validateReportToUploadHasContainerInTheFileSelector(TEST_PDF_FILE_NAME);
  uploadDocuments.fillAllFormsOfReportsSelectedForUpload(1);
}
