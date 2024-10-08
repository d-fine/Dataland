import { TEST_PDF_FILE_BASEPATH } from '@sharedUtils/ConstantsForPdfs';

export class UploadDocuments {
  private readonly name: string;
  private readonly uploadDocumentsSelector: string;
  private readonly addDocumentButtonSelector: string;
  constructor(name: string = 'UploadReports') {
    this.name = name;
    this.uploadDocumentsSelector = `div[data-test='upload-documents-${name}']`;
    this.addDocumentButtonSelector = `button[data-test='upload-files-button-${name}']`;
  }

  selectFile(filename: string): void {
    cy.get(this.addDocumentButtonSelector).click();
    cy.get(this.uploadDocumentsSelector)
      .find('input[type=file]')
      .selectFile(`../${TEST_PDF_FILE_BASEPATH}/${filename}.pdf`, { force: true });
  }

  selectMultipleFilesAtOnce(filenames: string[]): void {
    cy.get(this.addDocumentButtonSelector).click();
    const filenamePaths = filenames.map((filename) => `../testing/data/documents/${filename}.pdf`);
    cy.get(this.uploadDocumentsSelector).find('input[type=file]').selectFile(filenamePaths, { force: true });
  }
  selectDummyFile(filename: string, contentSize: number): void {
    cy.get(this.addDocumentButtonSelector).click();
    cy.get(this.uploadDocumentsSelector)
      .find('input[type=file]')
      .selectFile(
        {
          contents: new Cypress.Buffer(contentSize),
          fileName: `${filename}.pdf`,
          mimeType: 'application/pdf',
        },
        { force: true }
      );
  }
  selectDummyFileOfType(filename: string, fileType: string, contentSize: number): void {
    cy.get(this.addDocumentButtonSelector).click();
    cy.get(this.uploadDocumentsSelector)
      .find('input[type=file]')
      .selectFile(
        {
          contents: new Cypress.Buffer(contentSize),
          fileName: `${filename}.${fileType}`,
          mimeType: 'application/pdf',
        },
        { force: true }
      );
  }

  errorMessage(): Cypress.Chainable {
    return cy.get(`${this.uploadDocumentsSelector} .p-fileupload .p-message-error`);
  }
  dismissErrorMessage(): void {
    this.errorMessage().find('.p-message-close-icon').click();
  }
}
