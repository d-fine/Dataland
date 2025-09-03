import { TEST_PDF_FILE_NAME } from '@sharedUtils/ConstantsForPdfs';
import { UploadReports } from '@sharedUtils/components/UploadReports';

/**
 * Adds reports to the dataset via the Sfdr upload form for the given dataset
 * @param uploadReportsName the name of the vue component
 * @param filename the file to be uploaded
 */
export function selectSingleReportAndFillWithData(
  uploadReportsName: string = 'referencedReports',
  filename: string = TEST_PDF_FILE_NAME
): void {
  const uploadReports = new UploadReports(uploadReportsName);
  uploadReports.selectFile(filename);
  uploadReports.validateReportToUploadHasContainerInTheFileSelector(filename);
  uploadReports.fillAllFormsOfReportsSelectedForUpload();
}
