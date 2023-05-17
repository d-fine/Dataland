import { CompanyReport } from "@clients/backend";
import { DocumentControllerApi } from "@clients/documentmanager";

export interface DocumentToUpload {
  file: File;
  fileNameWithoutSuffix: string;

  reference: string;
}

export interface StoredReport extends CompanyReport {
  reportName: string;
}

export interface ReportToUpload extends CompanyReport {
  file: File;
  fileNameWithoutSuffix: string;
}

/**
 * uploads Files through the frontend
 * @param files the list of files to upload
 * @param documentControllerApi the api with uploader permissions that was created in the component using this function
 */
export async function uploadFiles(
  files: ReportToUpload[] | DocumentToUpload[],
  documentControllerApi: DocumentControllerApi
): Promise<void> {
  for (const fileToUpload of files) {
    const fileIsAlreadyInStorage = (await documentControllerApi.checkDocument(fileToUpload.reference)).data
      .documentExists;
    if (!fileIsAlreadyInStorage) {
      const backendComputedHash = (await documentControllerApi.postDocument(fileToUpload.file)).data.documentId;
      if (fileToUpload.reference !== backendComputedHash) {
        throw Error("Locally computed document hash does not concede with the one received by the upload request!");
      }
    }
  }
}

/**
 * Checks if there was actually a file added by the user that was not filtered
 * out by the FileUpload component.
 * @param filesCurrentlySelectedByUser the files currently selected by the user
 * @param previouslySelectedCertificates the reports that have already been selected before the last change
 * @returns true if there is actually a file added by the user
 */
export function isThereActuallyANewFileSelected(
  filesCurrentlySelectedByUser: File[],
  previouslySelectedCertificates: DocumentToUpload[]
): boolean {
  return filesCurrentlySelectedByUser.length != previouslySelectedCertificates.length;
}

/**
 *  calculates the hash from a file
 * @param [file] the file to calculate the hash for
 * @returns a promise of the hash as string
 */
export async function calculateSha256HashFromFile(file: File): Promise<string> {
  const buffer = await file.arrayBuffer();
  const hashBuffer = await crypto.subtle.digest("SHA-256", buffer);
  return toHex(hashBuffer);
}

/**
 *  helper to encode a hash of type buffer in hex
 * @param [buffer] the buffer to encode in hex
 * @returns  the array as string, hex encoded
 */
function toHex(buffer: ArrayBuffer): string {
  const array = Array.from(new Uint8Array(buffer)); // convert buffer to byte array
  return array.map((b) => b.toString(16).padStart(2, "0")).join(""); // convert bytes to hex string
}

/**
 * Removes the file extension after the last dot of the filename.
 * E.g. someFileName.with.dots.pdf will be converted to someFileName.with.dots
 * @param fileName the file name
 * @returns the file name without the file extension after the last dot
 */
export function removeFileTypeExtension(fileName: string): string {
  return fileName.split(".").slice(0, -1).join(".");
}
