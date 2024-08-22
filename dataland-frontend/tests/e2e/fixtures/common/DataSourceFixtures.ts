import { faker } from '@faker-js/faker';
import { type ExtendedDocumentReference } from '@clients/backend';
import { pickOneElement, type ReferencedDocuments } from '@e2e/fixtures/FixtureUtils';

/**
 * Generates a random data source referencing a random report from the provided referencedReports
 * @param referencedReports a list of reports that can be referenced
 * @returns a random data source referencing a random report from the provided referencedReports
 */
export function generateDataSource(referencedReports: ReferencedDocuments): ExtendedDocumentReference {
  const chosenReport = pickOneElement(Object.keys(referencedReports));
  const chosenReportReference = referencedReports[chosenReport];
  return {
    page: faker.number.int({ min: 1, max: 1200 }),
    fileName: chosenReport,
    fileReference: chosenReportReference.fileReference,
    tagName: faker.company.buzzNoun(),
  };
}
