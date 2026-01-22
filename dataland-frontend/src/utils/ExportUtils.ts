import {
  type DataExportControllerApi,
  type DataTypeEnum,
  type ExportFileType,
  ExportJobProgressState,
} from '@clients/backend';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { getDateStringForDataExport } from '@/utils/DataFormatUtils.ts';
const EXPORT_POLL_INTERVAL_MS = 500;
const EXPORT_MAX_POLL_ATTEMPTS = 240;

/**
 * Polls the export job status until completion or timeout
 * @param exportJobId the ID of the export job to poll
 * @param dataExportControllerApi
 * @throws Error if job fails or times out
 */
export async function pollExportJobStatus(
  exportJobId: string,
  dataExportControllerApi: DataExportControllerApi
): Promise<void> {
  let state: ExportJobProgressState = ExportJobProgressState.Pending;

  for (let attempt = 0; attempt < EXPORT_MAX_POLL_ATTEMPTS; attempt++) {
    const stateResponse = await dataExportControllerApi.getExportJobState(exportJobId);
    state = stateResponse.data;

    if (state === ExportJobProgressState.Success) return;
    if (state === ExportJobProgressState.Failure) {
      throw new Error('Export job failed on server');
    }

    await new Promise((resolve) => setTimeout(resolve, EXPORT_POLL_INTERVAL_MS));
  }

  if (state === ExportJobProgressState.Pending) {
    throw new Error('Export timeout - please try again with fewer companies or reporting periods');
  }
}

/**
 * Prepares the downloaded file by building filename and formatting content
 * @param exportFileType the file type for export
 * @param selectedFramework the framework being exported
 * @param responseData the raw response data from the API
 * @returns object containing filename and formatted content
 */
export function prepareDownloadFile(
  exportFileType: ExportFileType,
  selectedFramework: DataTypeEnum,
  responseData: string | ArrayBuffer | object
): { filename: string; content: string | ArrayBuffer } {
  const fileExtension = ExportFileTypeInformation[exportFileType].fileExtension;
  const label = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.find((f) => f === humanizeStringOrNumber(selectedFramework));
  const filename = `data-export-${label ?? humanizeStringOrNumber(selectedFramework)}-${getDateStringForDataExport(new Date())}.${fileExtension}`;
  const content = exportFileType === 'JSON' ? JSON.stringify(responseData) : (responseData as string | ArrayBuffer);

  return { filename, content };
}
