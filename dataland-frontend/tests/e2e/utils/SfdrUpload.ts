import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";
import { TEST_PDF_FILE_NAME } from "@e2e/utils/Constants";

/**
 * Adds reports to the dataset via the Sfdr upload form for the given dataset
 */
export function selectsReportsForUploadInSfdrForm(): void {
  uploadDocuments.selectFile(TEST_PDF_FILE_NAME, "referencedReports");
  uploadDocuments.validateReportToUploadIsListed(TEST_PDF_FILE_NAME);
  uploadDocuments.fillAllReportsToUploadForms(1);
}
