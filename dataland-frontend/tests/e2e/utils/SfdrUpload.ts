import { TEST_PDF_FILE_NAME } from "@sharedUtils/ConstantsForPdfs";
import { UploadReports } from "@sharedUtils/components/UploadReports";

/**
 * Adds reports to the dataset via the Sfdr upload form for the given dataset
 */
export function selectsReportsForUploadInSfdrForm(): void {
  const uploadReports = new UploadReports("referencedReports");
  uploadReports.selectFile(TEST_PDF_FILE_NAME);
  uploadReports.validateReportToUploadHasContainerInTheFileSelector(TEST_PDF_FILE_NAME);
  uploadReports.fillAllFormsOfReportsSelectedForUpload(1);
}
