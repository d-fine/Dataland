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
  const startPage = faker.number.int({ min: 1, max: 1200 });
  const endPage = faker.number.int({ min: startPage + 1, max: 1300 });
  const singlePageNumberScheme = (): ExtendedDocumentReference => ({
    page: `${startPage}`,
    fileName: chosenReport,
    fileReference: chosenReportReference.fileReference,
    tagName: faker.company.buzzNoun(),
  });
  const pageRangeScheme = (): ExtendedDocumentReference => ({
    page: `${startPage}-${endPage}`,
    fileName: chosenReport,
    fileReference: chosenReportReference.fileReference,
    tagName: faker.company.buzzNoun(),
  });
  const chosenPageNumberScheme = pickOneElement([singlePageNumberScheme, pageRangeScheme]);
  return chosenPageNumberScheme();
}
