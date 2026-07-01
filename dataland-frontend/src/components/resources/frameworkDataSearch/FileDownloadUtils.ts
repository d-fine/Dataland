import { type Ref, ref } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { getMimeTypeFromHeaders } from '@/utils/Axios.ts';
import type Keycloak from 'keycloak-js';
import { type BaseDocumentReference, type ExtendedDocumentReference } from '@clients/backend';

export interface DocumentDownloadInfo {
  downloadName: string;
  fileReference: string;
  page?: number;
  dataId?: string;
  dataType?: string;
}

/**
 * Creates a new ref to a percentCompleted.
 */
export function createNewPercentCompletedRef(): Ref<number | undefined, number | undefined> {
  return ref<number | undefined>(undefined);
}

/**
 * Handles the download of a public document
 * @param documentDownloadInfo the relevant download info
 * @param docUrl the URL of the document
 * @param getKeycloakPromise function returning the relevant keycloak promise
 * @param percentCompleted the percentCompleted value of the download
 */
async function handlePublicDocumentDownload(
  documentDownloadInfo: DocumentDownloadInfo,
  docUrl: HTMLAnchorElement,
  getKeycloakPromise: (() => Promise<Keycloak>) | undefined,
  percentCompleted: Ref<number | undefined, number | undefined>
): Promise<void> {
  const documentControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
    .documentController;
  let downloadCompleted = false;
  await documentControllerApi
    .getDocument(documentDownloadInfo.fileReference, {
      responseType: 'arraybuffer',
      onDownloadProgress: (progressEvent) => {
        if (!downloadCompleted && progressEvent.total != null)
          percentCompleted.value = Math.round((progressEvent.loaded * 100) / progressEvent.total);
      },
    })
    .then((getDocumentsFromStorageResponse) => {
      downloadCompleted = true;
      percentCompleted.value = 100;
      const mimeType = getMimeTypeFromHeaders(getDocumentsFromStorageResponse.headers);
      const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: mimeType });
      docUrl.href = URL.createObjectURL(newBlob);
      docUrl.target = '_blank';
      docUrl.dataset.test = `report-${documentDownloadInfo.downloadName}-link`;
      if (documentDownloadInfo.page) docUrl.href += `#page=${documentDownloadInfo.page}`;
      document.body.appendChild(docUrl);
      docUrl.click();
    });
}

/**
 * Method for downloading a document from Dataland.
 * @param documentDownloadInfo the relevant info for the download
 * @param getKeycloakPromise function returning the relevant keycloak promise
 * @param percentCompleted the completed percentage of the download, undefined at first
 */
export async function downloadDocument(
  documentDownloadInfo: DocumentDownloadInfo,
  getKeycloakPromise: (() => Promise<Keycloak>) | undefined,
  percentCompleted: Ref<number | undefined, number | undefined>
): Promise<void> {
  percentCompleted.value = 0;
  try {
    const docUrl = document.createElement('a');
    await handlePublicDocumentDownload(documentDownloadInfo, docUrl, getKeycloakPromise, percentCompleted);
  } catch (error) {
    console.error(error);
  }
  percentCompleted.value = undefined;
}

/**
 * Checks, based on the value of percentCompleted, whether a download triggered from the component
 * is currently in progress.
 * @param percentCompleted the progress in percent of the download, if any (if no download is in
 * progress, its value is undefined)
 */
export function downloadIsInProgress(percentCompleted: number | undefined): boolean {
  return percentCompleted != undefined;
}

/**
 * Based on the given data source, returns the triple of info what the first document page for this
 * source is, what the entire page range is (can also be just a single page), and whether the source
 * corresponds to multiple pages.
 * @param dataSource the data source in question
 */
export function getPageInfo(dataSource: ExtendedDocumentReference | BaseDocumentReference | undefined): {
  firstPageInRange: number | undefined;
  pageRange: string;
  hasMultiplePages: boolean;
} {
  const pageInfo = { firstPageInRange: undefined as number | undefined, pageRange: '', hasMultiplePages: false };

  if (dataSource && 'page' in dataSource && dataSource.page != null) {
    const pageRange = dataSource.page;
    pageInfo.firstPageInRange = Number(pageRange.split('-')[0]) || undefined;
    pageInfo.pageRange = pageRange;
    pageInfo.hasMultiplePages = pageRange.includes('-');
  }

  return pageInfo;
}
