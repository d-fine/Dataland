import { Configuration } from "@clients/backend";
import { DocumentControllerApi, DocumentUploadResponse } from "@clients/documentmanager";

/**
 * Uploads all documents provided in the documentDirectory folder
 * @param token the keycloak token for authentication
 */
export function uploadAllDocuments(token: string): void {
  const documentDirectory = "../testing/data/documents/";
  cy.task("readdir", documentDirectory).then((fileNames) => {
    (fileNames as string[]).forEach((name: string) => {
      cy.task<{ [type: string]: ArrayBuffer }>("readFile", documentDirectory + name).then((bufferObject) => {
        uploadDocumentViaApi(token, bufferObject.data, name).catch((error) => console.log(error));
      });
    });
  });
}

/**
 * Uses the Dataland API to upload a document
 * @param token the bearer token used to authorize the API requests
 * @param buffer the pdf document as an arrayBuffer to be uploaded as File
 * @param name the file name
 * @returns a promise on the upload response
 */
export async function uploadDocumentViaApi(
  token: string,
  buffer: ArrayBuffer,
  name: string
): Promise<DocumentUploadResponse> {
  const arr = new Uint8Array(buffer);
  const file = new File([arr], name, { type: "application/pdf" });
  return (
    await new DocumentControllerApi(
      new Configuration({
        accessToken: token,
      })
    ).postDocument(file)
  ).data;
}
