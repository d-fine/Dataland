import { Configuration } from "@clients/backend";
import { DocumentControllerApi } from "@clients/documentmanager";

/**
 * Uploads all documents provided in the documentDirectory folder
 * @param token the keycloak token for authentication
 */
export function uploadAllDocuments(token: string): void {
  const documentDirectory = "../testing/data/documents/";
  const api = new DocumentControllerApi(new Configuration({ accessToken: token }));
  cy.task("readdir", documentDirectory).then((fileNames) => {
    (fileNames as string[]).forEach((name: string) => {
      cy.task<{ [type: string]: ArrayBuffer }>("readFile", documentDirectory + name).then((bufferObject) => {
        const arr = new Uint8Array(bufferObject.data);
        const file = new File([arr], name, { type: "application/pdf" });
        api.postDocument(file).catch((error) => console.log(error));
        cy.wait(1000);
        delete bufferObject.data;
        cy.wait(1000);
      });
    });
  });
}
