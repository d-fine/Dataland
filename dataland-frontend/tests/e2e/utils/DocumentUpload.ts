import { Configuration } from '@clients/backend';
import {
  DocumentControllerApi,
  type DocumentMetaInfo,
  type DocumentMetaInfoPatch,
  type DocumentMetaInfoResponse,
} from '@clients/documentmanager';
import { createHash } from 'crypto';
import { type AxiosError } from 'axios';

/**
 * Uploads all documents provided in the documentDirectory folder
 * @param token the keycloak token for authentication
 */
export function uploadAllDocuments(token: string): void {
  uploadAllDocumentsFromFolder(token, '../testing/data/documents/');
  uploadAllDocumentsFromFolder(token, '../testing/data/documents/fake-fixtures/');
}

/**
 * Uploads all documents from a folder
 * @param token the keycloak token for authentication
 * @param documentDirectory the directory where the documents are stored
 */
function uploadAllDocumentsFromFolder(token: string, documentDirectory: string): void {
  cy.task('readdir', documentDirectory).then((fileNames) => {
    const allFileNames = fileNames as string[];
    const pdfFileNames = allFileNames.filter((name: string) => name.endsWith('.pdf'));
    pdfFileNames.forEach((name: string) => {
      cy.task<{ [type: string]: ArrayBuffer }>('readFile', documentDirectory + name).then((bufferObject) =>
        uploadDocumentViaApi(token, bufferObject.data, name)
      );
    });
  });
}

/**
 * Uses the Dataland API to upload a document
 * @param token the bearer token used to authorize the API requests
 * @param buffer the pdf document as an arrayBuffer to be uploaded as File
 * @param name the file name
 * @param documentMetaInfo meta Information about the file to be uploaded
 * @returns a promise on the upload response
 */
export async function uploadDocumentViaApi(
  token: string,
  buffer: ArrayBuffer,
  name: string,
  documentMetaInfo?: DocumentMetaInfo
): Promise<DocumentMetaInfoResponse> {
  const arr = new Uint8Array(buffer);
  const file = new File([arr], name, { type: 'application/pdf' });
  const documentControllerApi = new DocumentControllerApi(
    new Configuration({
      accessToken: token,
    })
  );
  const documentHash = createHash('sha256').update(arr).digest('hex');
  return await documentControllerApi
    .postDocument(file, documentMetaInfo)
    .then((response) => {
      return response.data;
    })
    .catch((error: AxiosError) => {
      if (error.status == 409) {
        console.log('Document already exists.');
        return { documentId: documentHash } as DocumentMetaInfoResponse;
      } else {
        throw error;
      }
    });
}

/**
 * Patch the company id list in the stored meta information of a given document by adding
 * a single new company id.
 * @param token the bearer token used to authorize the API requests
 * @param documentId the id of the document whose meta info shall be patched.
 * @param documentMetaInfoPatch meta data do patch
 */
export async function changeDocumentNameAndAddCompanyToDocumentMetaInfoViaApi(
  token: string,
  documentId: string,
  documentMetaInfoPatch: DocumentMetaInfoPatch
): Promise<DocumentMetaInfoResponse> {
  const documentControllerApi = new DocumentControllerApi(
    new Configuration({
      accessToken: token,
    })
  );
  const documentHash = createHash('sha256').digest('hex');
  return await documentControllerApi
    .patchDocumentMetaInfo(documentId, documentMetaInfoPatch)
    .then((response) => {
      return response.data;
    })
    .catch((error: AxiosError) => {
      if (error.status == 403) {
        console.log('You do not have the right to update the companyIds field.');
        return { documentId: documentHash } as DocumentMetaInfoResponse;
      } else if (error.status == 404) {
        console.log('Document Id does not match any stored document.');
        return { documentId: documentHash } as DocumentMetaInfoResponse;
      } else {
        throw error;
      }
    });
}
