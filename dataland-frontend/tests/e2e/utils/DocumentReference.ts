import { pickOneElement, type ReferencedDocuments } from '@e2e/fixtures/FixtureUtils';
import { computeFakeFixtureDocumentIds } from '@e2e/support/node/fixtureDocuments.ts';

const possibleDocuments = ['Certification', 'Policy'];

let cachedIds: string[] | null = null;

/**
 * Returns the SHA-256 IDs of all fake PDF document fixtures used in E2E tests.
 *
 * In the Cypress browser context, the IDs are read from
 * `Cypress.env("fakeFixtureDocumentIds")` (set in `cypress.config.ts`).
 * In Node.js, the IDs are computed from the PDF files on disk.
 *
 * Results are cached after the first call.
 */
export function getAllFakeFixtureDocumentIds(): string[] {
  if (cachedIds) return cachedIds;

  // Browser (Cypress runner): take from env
  if (typeof window !== "undefined") {
    const ids = (Cypress.env("fakeFixtureDocumentIds") as string[]) ?? [];
    if (!ids.length) {
      throw new Error(
        "fakeFixtureDocumentIds missing. Ensure cypress.config.ts sets config.env.fakeFixtureDocumentIds in setupNodeEvents()."
      );
    }
    cachedIds = ids;
    return ids;
  }

  cachedIds = computeFakeFixtureDocumentIds(process.cwd())
  return cachedIds;
}

/**
 * Generates a set of referenced documents for test data.
 *
 * Each document is assigned a random file reference taken from the
 * available fake fixture document IDs.
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
