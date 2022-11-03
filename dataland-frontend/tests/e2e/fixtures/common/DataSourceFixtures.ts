import { faker } from "@faker-js/faker";
import { CompanyReportReference } from "@clients/backend";
import { DataPoint, ReferencedReports } from "@e2e/fixtures/FixtureUtils";
import { humanizeOrUndefined } from "@e2e/fixtures/CsvUtils";

export function generateDataSource(referencedReports: ReferencedReports): CompanyReportReference {
  const chosenReport = faker.helpers.arrayElement(Object.keys(referencedReports));
  return {
    page: faker.datatype.number({ min: 1, max: 1200 }),
    report: chosenReport,
    tagName: faker.company.bsNoun(),
  };
}

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
