import { Configuration } from "@clients/backend";
import { DocumentControllerApi, DocumentUploadResponse } from "@clients/documentmanager";

/**
 * Fills the company for a company with the specified name with dummy values.
 *
 * @param companyName the company name to fill into the form
 */
export function fillCompanyUploadFields(companyName: string): void {
  cy.get("input[name=companyName]").type(companyName, { force: true });
  cy.get("input[name=alternativeName]").type("Name to remove", { force: true });
  cy.get("button[name=addAlternativeName]").click({ force: true });
  cy.get(`span.form-list-item em`).click();
  cy.get("span.form-list-item").should("not.exist");
  cy.get("input[name=alternativeName]").type("Another Name", { force: true });
  cy.get("button[name=addAlternativeName]").click({ force: true });
  cy.get("input[name=headquarters]").type("Capitol City", { force: true });
  cy.get("select[name=countryCode]").select("DE", { force: true });
  cy.get("input[name=headquartersPostalCode]").type("123456", { force: true });
  cy.get("input[name=companyLegalForm]").type("Enterprise Ltd.", { force: true });
  cy.get("input[name=website]").type("www.company.com", { force: true });
  cy.get("input[name=lei]").type(`LeiValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("input[name=isin]").type(`IsinValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("input[name=ticker]").type(`TickerValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("input[name=permId]").type(`PermValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("input[name=duns]").type(`DunsValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("input[name=companyRegistrationNumber]").type(`RegValueId:${crypto.randomUUID()}`, { force: true });
  cy.get("select[name=sector]").select("Energy");
}

/**
 * Uploads all documents provided in the <> folder and saves the hashes in a file for later referencing in the datasets
 *
 * @param token the keycloak token for authentication
 * @returns a cypress chainable containing the company meta information of the newly created company
 */
export function uploadAllDocuments(token: string): string[] {
  const documentDirectory = "..\\testing\\data\\documents\\";
  const documentIds: string[] = [];
  cy.task("readdir", documentDirectory).then((fileNames) => {
    (fileNames as string[]).forEach((name) => {
      cy.task<Buffer>("readFile", documentDirectory + name).then((fileBuffer) => {
        uploadDocumentViaApi(token, fileBuffer, name)
          .then((documentId) => {
            documentIds.push(documentId.documentId);
          })
          .catch((error) => console.log(error));
      });
    });
  });
  return documentIds;
}

/**
 * Uses the Dataland API to upload a document
 *
 * @param token the bearer token used to authorize the API requests
 * @param buffer the pdf document as an arrayBuffer to be uploaded as File
 * @param name the file name
 */
export async function uploadDocumentViaApi(
  token: string,
  buffer: ArrayBuffer,
  name: string
): Promise<DocumentUploadResponse> {
  const arr = new Uint8Array(buffer);
  const file = new File([arr], name, { type: "application/pdf" });
  const response = await new DocumentControllerApi(
    new Configuration({
      accessToken: token,
    })
  ).postDocument(file);
  return response.data;
}
