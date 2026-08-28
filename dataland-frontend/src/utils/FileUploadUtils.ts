import { type DocumentMetaInfo, type DocumentMetaInfoPatch } from '@clients/documentmanager';
import { type CompanyReport } from '@clients/backend';
import { ApiClientProvider } from '@/services/ApiClients';
import type Keycloak from 'keycloak-js';
import { AxiosError } from 'axios';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type ObjectType } from '@/utils/UpdateObjectUtils';

export interface DocumentToUpload {
  file: File;
  fileNameWithoutSuffix: string;
  fileReference: string;
}
export interface StoredReport extends CompanyReport {
  fileName: string;
}

/**
 * uploads Files through the frontend
 * @param files the list of files to upload
 * @param getKeycloakPromise getter for a keycloak promise
 * @param documentMetaInfoByReference document metadata to store with each newly uploaded document
 */
export async function uploadFiles(
  files: DocumentToUpload[],
  getKeycloakPromise: () => Promise<Keycloak>,
  documentMetaInfoByReference: Map<string, DocumentMetaInfo> = new Map()
): Promise<void> {
  const documentControllerApi = new ApiClientProvider(getKeycloakPromise()).apiClients.documentController;
  const alreadyUploadedFileReferences = new Set<string>();

  for (const fileToUpload of files) {
    if (alreadyUploadedFileReferences.has(fileToUpload.fileReference)) {
      continue;
    }

    const fileIsAlreadyInStorage = await isDocumentAlreadyInStorage(documentControllerApi, fileToUpload.fileReference);
    const documentMetaInfo = documentMetaInfoByReference.get(fileToUpload.fileReference);

    if (fileIsAlreadyInStorage) {
      await patchDocumentMetaInfoIfProvided(documentControllerApi, fileToUpload.fileReference, documentMetaInfo);
    } else {
      await uploadDocumentAndValidateHash(documentControllerApi, fileToUpload, documentMetaInfo);
    }

    alreadyUploadedFileReferences.add(fileToUpload.fileReference);
  }
}

function isNotFoundAxiosError(error: unknown): boolean {
  return error instanceof AxiosError && assertDefined(error.response).status === 404;
}

async function isDocumentAlreadyInStorage(
  documentControllerApi: ApiClientProvider['apiClients']['documentController'],
  fileReference: string
): Promise<boolean> {
  try {
    await documentControllerApi.checkDocument(fileReference);
    return true;
  } catch (error) {
    if (isNotFoundAxiosError(error)) {
      return false;
    }
    throw error;
  }
}

async function patchDocumentMetaInfoIfProvided(
  documentControllerApi: ApiClientProvider['apiClients']['documentController'],
  fileReference: string,
  documentMetaInfo: DocumentMetaInfo | undefined
): Promise<void> {
  if (!documentMetaInfo) {
    return;
  }

  const documentMetaInfoPatch: DocumentMetaInfoPatch = {
    documentName: documentMetaInfo.documentName,
    publicationDate: documentMetaInfo.publicationDate,
    reportingPeriod: documentMetaInfo.reportingPeriod,
  };
  await documentControllerApi.patchDocumentMetaInfo(fileReference, documentMetaInfoPatch);
}

async function uploadDocumentAndValidateHash(
  documentControllerApi: ApiClientProvider['apiClients']['documentController'],
  fileToUpload: DocumentToUpload,
  documentMetaInfo: DocumentMetaInfo | undefined
): Promise<void> {
  const backendComputedHash = (await documentControllerApi.postDocument(fileToUpload.file, documentMetaInfo)).data
    .documentId;
  if (fileToUpload.fileReference !== backendComputedHash) {
    throw new Error('Locally computed document hash does not concede with the one received by the upload request!');
  }
}

/**
 * Checks if there was actually a file added by the user that was not filtered out by the FileUpload component.
 * @param filesCurrentlySelectedByUser the files currently selected by the user
 * @param previouslySelectedDocuments the documents that have already been selected before the last change
 * @returns true if there is actually a file added by the user
 */
export function isThereActuallyANewFileSelected(
  filesCurrentlySelectedByUser: File[],
  previouslySelectedDocuments: DocumentToUpload[]
): boolean {
  return filesCurrentlySelectedByUser.length != previouslySelectedDocuments.length;
}

/**
 *  calculates the hash from a file
 * @param [file] the file to calculate the hash for
 * @returns a promise of the hash as string
 */
export async function calculateSha256HashFromFile(file: File): Promise<string> {
  const buffer = await file.arrayBuffer();
  const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
  return toHex(hashBuffer);
}

/**
 *  helper to encode a hash of type buffer in hex
 * @param [buffer] the buffer to encode in hex
 * @returns  the array as string, hex encoded
 */
function toHex(buffer: ArrayBuffer): string {
  const array = Array.from(new Uint8Array(buffer)); // convert buffer to byte array
  return array.map((b) => b.toString(16).padStart(2, '0')).join(''); // convert bytes to hex string
}

/**
 * Removes the file extension after the last dot of the filename.
 * E.g. someFileName.with.dots.pdf will be converted to someFileName.with.dots
 * @param fileName the file name
 * @returns the file name without the file extension after the last dot
 */
export function removeFileTypeExtension(fileName: string): string {
  return fileName.split('.').slice(0, -1).join('.');
}

/**
 * This functions returns the array of available reports
 * @param inputArray array of files which should be made referenceable
 * @returns the object of referenceable reports
 */
export function calculateReferenceableFiles(inputArray: DocumentToUpload[] | StoredReport[]): ObjectType {
  const referenceableReport = {} as ObjectType;
  let reportName: string;
  for (const element of inputArray) {
    if ((<DocumentToUpload>element).fileNameWithoutSuffix) {
      reportName = (<DocumentToUpload>element).fileNameWithoutSuffix;
    } else {
      reportName = (<StoredReport>element).fileName;
    }
    referenceableReport[reportName] = element.fileReference;
  }
  return referenceableReport;
}

/**
 * The method returns the fileReference for a given fileName
 * @param currentReportValue name of the report for which the fileReference should be retrieved
 * @param injectReportsNameAndReferences map containing fileNames and corresponding FileReferences
 * @returns fileReference of the given fileName
 */
export function getFileReferenceByFileName(
  currentReportValue: string | null | undefined,
  injectReportsNameAndReferences: ObjectType
): string {
  if (currentReportValue && currentReportValue in injectReportsNameAndReferences) {
    const value = injectReportsNameAndReferences[currentReportValue];
    if (typeof value === 'string') {
      return value;
    }
  }
  return '';
}

/**
 * The method returns the fileName for a given fileReference. This is used to resolve the currently referenced
 * report of a data point even if the backend does not (or no longer) provide the fileName directly (e.g. because
 * it is considered an inferable field that is derived from the document metadata behind the fileReference).
 * @param fileReference the fileReference for which the corresponding fileName should be retrieved
 * @param injectReportsNameAndReferences map containing fileNames and corresponding FileReferences
 * @returns fileName of the given fileReference, or undefined if it could not be resolved
 */
export function getFileNameByFileReference(
  fileReference: string | null | undefined,
  injectReportsNameAndReferences: ObjectType
): string | undefined {
  if (!fileReference || !injectReportsNameAndReferences) {
    return undefined;
  }
  const matchingEntry = Object.entries(injectReportsNameAndReferences).find(
    ([, reference]) => reference === fileReference
  );
  return matchingEntry?.[0];
}

/**
 * Retrieves all names of currently referencable files on the upload page.
 * @param injectReportsNameAndReferences map of file names to their respective file references (which are hashes)
 * @returns the file names as list
 */
export function getAvailableFileNames(injectReportsNameAndReferences: ObjectType): string[] {
  if (injectReportsNameAndReferences) {
    return Object.keys(injectReportsNameAndReferences);
  } else {
    return [];
  }
}

export const PAGE_NUMBER_DESCRIPTION =
  'The single page or the range of pages of the document from where the information ' +
  'was sourced. On Dataland, a single page is defined as the n-th page of the PDF, i.e., ' +
  'the page number when looking at the PDF in a browser.';
