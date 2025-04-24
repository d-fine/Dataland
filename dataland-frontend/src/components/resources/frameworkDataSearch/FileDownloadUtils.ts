import { type Ref, ref } from 'vue';
import {
  getAllPrivateFrameworkIdentifiers,
  getBasePrivateFrameworkDefinition,
} from '@/frameworks/BasePrivateFrameworkRegistry.ts';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type { PrivateFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import { getFileExtensionFromHeaders, getMimeTypeFromHeaders } from '@/utils/Axios.ts';
import type Keycloak from 'keycloak-js';

export interface DocumentDownloadInfo {
  fileReference: string;
  page: number | undefined;
  dataId: string | undefined;
  dataType: string | undefined;
  downloadName: string;
}

/**
 * Creates a new ref to a percentCompleted.
 */
export function createNewPercentCompletedRef(): Ref<number | undefined, number | undefined> {
  return ref<number | undefined>(undefined);
}

/**
 * Checks whether documentDownloadInfo refers to the download of a private framework.
 * @param documentDownloadInfo the download info to check
 */
function isForDownloadOfPrivateFrameworkDocument(documentDownloadInfo: DocumentDownloadInfo): boolean {
  return !!documentDownloadInfo.dataType && getAllPrivateFrameworkIdentifiers().includes(documentDownloadInfo.dataType);
}

/**
 * Handles the download of a private document
 * @param documentDownloadInfo the relevant download info
 * @param docUrl the URL of the document
 * @param getKeycloakPromise function returning the relevant keycloak promise
 * @param percentCompleted the percentCompleted value of the download
 */
async function handlePrivateDocumentDownload(
  documentDownloadInfo: DocumentDownloadInfo,
  docUrl: HTMLAnchorElement,
  getKeycloakPromise: (() => Promise<Keycloak>) | undefined,
  percentCompleted: Ref<number | undefined, number | undefined>
): Promise<void> {
  if (!documentDownloadInfo.dataId) throw new Error('Data id is required for private framework document download');
  if (!documentDownloadInfo.dataType) throw new Error('Data type is required for private framework document download');

  const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
  let privateDataControllerApi: PrivateFrameworkDataApi<unknown>;
  const frameworkDefinition = getBasePrivateFrameworkDefinition(documentDownloadInfo.dataType);
  if (frameworkDefinition) {
    privateDataControllerApi = frameworkDefinition.getPrivateFrameworkApiClient(
      undefined,
      apiClientProvider.axiosInstance
    );
    let downloadCompleted = false;
    await privateDataControllerApi
      .getPrivateDocument(documentDownloadInfo.dataId, documentDownloadInfo.fileReference, {
        responseType: 'arraybuffer',
        onDownloadProgress: (progressEvent) => {
          if (!downloadCompleted && progressEvent.total != null)
            percentCompleted.value = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        },
      })
      .then((getDocumentsFromStorageResponse) => {
        downloadCompleted = true;
        percentCompleted.value = 100;
        const fileExtension = getFileExtensionFromHeaders(getDocumentsFromStorageResponse.headers);
        const mimeType = getMimeTypeFromHeaders(getDocumentsFromStorageResponse.headers);
        const newBlob = new Blob([getDocumentsFromStorageResponse.data], { type: mimeType });
        docUrl.href = URL.createObjectURL(newBlob);
        docUrl.setAttribute('download', `${documentDownloadInfo.downloadName}.${fileExtension}`);
        document.body.appendChild(docUrl);
        docUrl.click();
      });
  }
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
    if (isForDownloadOfPrivateFrameworkDocument(documentDownloadInfo)) {
      await handlePrivateDocumentDownload(documentDownloadInfo, docUrl, getKeycloakPromise, percentCompleted);
    } else {
      await handlePublicDocumentDownload(documentDownloadInfo, docUrl, getKeycloakPromise, percentCompleted);
    }
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
