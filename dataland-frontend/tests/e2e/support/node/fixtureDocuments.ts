import { readdirSync, readFileSync, existsSync } from 'fs';
import { createHash } from 'crypto';
import * as path from 'path';

/**
 * Computes SHA-256 document IDs for all fake PDF fixtures.
 *
 * Reads all `.pdf` files from the fake-fixtures directory (relative to the
 * Cypress project root) and returns their content hashes.
 */
export function computeFakeFixtureDocumentIds(projectRoot: string): string[] {
  const baseDir = path.resolve(projectRoot, '..', 'testing', 'data', 'documents', 'fake-fixtures');

  if (!existsSync(baseDir)) {
    throw new Error(`fake-fixtures folder not found: ${baseDir}`);
  }

  const pdfFiles = readdirSync(baseDir).filter((f) => f.endsWith('.pdf'));

  if (!pdfFiles.length) {
    throw new Error(`No PDF fixtures found in ${baseDir}`);
  }

  return pdfFiles.map((file) =>
    createHash('sha256')
      .update(readFileSync(path.join(baseDir, file)))
      .digest('hex')
  );
}
