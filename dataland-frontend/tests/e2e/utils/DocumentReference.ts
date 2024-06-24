import { readFileSync } from 'fs';
import { createHash } from 'crypto';
import { type ReferencedDocuments } from '@e2e/fixtures/FixtureUtils';

const possibleDocuments = ['Certification', 'Policy'];

/**
 * Generates hash to fixture pdf that is used for all fake fixture references
 * @returns documentId ID of a pdf that is stored in internal storage and can be referenced
 */
export function getReferencedDocumentId(): string {
  const testDocumentPath = '../testing/data/documents/StandardWordExport.pdf';
  const fileContent: Buffer = readFileSync(testDocumentPath);
  return createHash('sha256').update(fileContent).digest('hex');
}

/**
 * Generates a random non-empty set of reports that can be referenced
 * @returns a random non-empty set of reports
 */
export function generateReferencedDocuments(): ReferencedDocuments {
  const referencedDocuments: ReferencedDocuments = {};
  for (const documentName of possibleDocuments) {
    referencedDocuments[documentName] = {
      fileName: documentName,
      fileReference: getReferencedDocumentId(),
    };
  }
  return referencedDocuments;
}
