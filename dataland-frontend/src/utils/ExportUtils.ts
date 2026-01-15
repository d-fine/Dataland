import { type DataExportControllerApi, ExportJobProgressState } from '@clients/backend';
const EXPORT_POLL_INTERVAL_MS = 500;
const EXPORT_MAX_POLL_ATTEMPTS = 180;

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
