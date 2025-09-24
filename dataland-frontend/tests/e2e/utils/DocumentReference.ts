import { readFileSync, readdirSync } from 'fs';
import { createHash } from 'crypto';
import { pickOneElement, type ReferencedDocuments } from '@e2e/fixtures/FixtureUtils';

const possibleDocuments = ['Certification', 'Policy'];

/**
 * Returns all document IDs of the fake fixtures
 * @returns all document IDs of the fake fixtures
 */
export function getAllFakeFixtureDocumentIds(): string[] {
  const baseDir = '../testing/data/documents/fake-fixtures';
  const files = readdirSync(baseDir);
  const pdfFiles = files.filter((file) => file.endsWith('.pdf'));
  return pdfFiles.map((file) =>
    createHash('sha256')
      .update(readFileSync(`${baseDir}/${file}`))
      .digest('hex')
  );
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
      fileReference: pickOneElement(getAllFakeFixtureDocumentIds()),
    };
  }
  return referencedDocuments;
}
