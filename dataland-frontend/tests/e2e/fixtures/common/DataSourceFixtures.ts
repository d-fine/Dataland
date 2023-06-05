import { faker } from "@faker-js/faker/locale/de";
import { CompanyReportReference } from "@clients/backend";
import { DataPoint, ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { humanizeOrUndefined } from "@e2e/fixtures/CsvUtils";

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

/**
 * Creates a CSV mapping for the data-source columns of a datapoint
 * @param dataPointName the datapoint to generate the apping for
 * @param companyReportGetter a function that can be used to get a report from the datapoint
 * @returns the generated CSV mapping
 */
export function getCsvDataSourceMapping<T>(
  dataPointName: string,
  companyReportGetter: (x: T) => CompanyReportReference | undefined
): Array<DataPoint<T, string | number>> {
  return [
    {
      label: `${dataPointName} Report`,
      value: (row: T): string | undefined => humanizeOrUndefined(companyReportGetter(row)?.report),
    },
    {
      label: `${dataPointName} Page`,
      value: (row: T): number | undefined => companyReportGetter(row)?.page,
    },
    {
      label: `${dataPointName} Tag`,
      value: (row: T): string | undefined => companyReportGetter(row)?.tagName as string,
    },
  ];
}
