import { Configuration } from "@clients/backend";
import { DocumentControllerApi, DocumentUploadResponse } from "@clients/documentmanager";
import { uploader_name, uploader_pw } from "@e2e/utils/Cypress";

/**
 * Uploads all documents provided in the documentDirectory folder
 */
export function uploadAllDocuments(): void {
  const documentDirectory = "../testing/data/documents/";
  cy.task("readdir", documentDirectory).then((fileNames) => {
    (fileNames as string[]).forEach((name: string) => {
      cy.getKeycloakToken(uploader_name, uploader_pw).then((token) => {
        const api = new DocumentControllerApi(new Configuration({ accessToken: token }));
        cy.task("logMessage", ["Uploading document: " + name]);
        cy.task<{ [type: string]: ArrayBuffer }>("readFile", documentDirectory + name).then((bufferObject) => {
          const arr = new Uint8Array(bufferObject.data);
          const file = new File([arr], name, { type: "application/pdf" });
          api.postDocument(file).catch((error) => console.log(error));
          delete bufferObject.data;
          cy.wait(2000);
        });
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
