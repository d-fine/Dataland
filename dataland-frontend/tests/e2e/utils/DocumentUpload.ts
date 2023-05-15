import { Configuration } from "@clients/backend";
import { DocumentControllerApi } from "@clients/documentmanager";
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
          cy.task("logMessage", ["Done with conversion."]);
          api.postDocument(file).catch((error) => {
            cy.task("logMessage", [`Error uploading document: ${name} Error: ${String(error)}`]);
          });
          cy.task("logMessage", ["Done uploading document: " + name]);
          delete bufferObject.data;
        });
      });
    });
  });
}
