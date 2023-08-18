import { faker } from "@faker-js/faker";
import { type CompanyReportReference } from "@clients/backend";
import { type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a random data source referencing a random report from the provided referencedReports
 * @param referencedReports a list of reports that can be referenced
 * @returns a random data source referencing a random report from the provided referencedReports
 */
export function generateDataSource(referencedReports: ReferencedDocuments): CompanyReportReference {
  const chosenReport = faker.helpers.arrayElement(Object.keys(referencedReports));
  return {
    page: faker.number.int({ min: 1, max: 1200 }),
    report: chosenReport,
    tagName: faker.company.buzzNoun(),
  };
}
