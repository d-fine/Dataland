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
 */
export async function uploadFiles(
  files: DocumentToUpload[],
  getKeycloakPromise: () => Promise<Keycloak>
): Promise<void> {
  const documentControllerApi = new ApiClientProvider(getKeycloakPromise()).apiClients.documentController;
  const alreadyUploadedFileReferences = new Set<string>();
  for (const fileToUpload of files) {
    if (alreadyUploadedFileReferences.has(fileToUpload.fileReference)) {
      continue;
    }
    let fileIsAlreadyInStorage: boolean;
    try {
      await documentControllerApi.checkDocument(fileToUpload.fileReference);
      alreadyUploadedFileReferences.add(fileToUpload.fileReference);
      fileIsAlreadyInStorage = true;
    } catch (error) {
      if (error instanceof AxiosError && assertDefined((error as AxiosError).response).status == 404) {
        fileIsAlreadyInStorage = false;
      } else {
        throw error;
      }
    }
    if (!fileIsAlreadyInStorage) {
      const backendComputedHash = (await documentControllerApi.postDocument(fileToUpload.file)).data.documentId;
      if (fileToUpload.fileReference !== backendComputedHash) {
        throw Error('Locally computed document hash does not concede with the one received by the upload request!');
      }
      alreadyUploadedFileReferences.add(fileToUpload.fileReference);
    }
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
  currentReportValue: string | null,
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
 * Retrieves the all fileNames of the map
 * @param injectReportsNameAndReferences map from which the fileNames should be retrieved
 * @returns fileNames of all entries in the map
 */
export function getFileName(injectReportsNameAndReferences: ObjectType): string[] {
  if (injectReportsNameAndReferences) {
    return Object.keys(injectReportsNameAndReferences);
  } else {
    return [];
  }
}
