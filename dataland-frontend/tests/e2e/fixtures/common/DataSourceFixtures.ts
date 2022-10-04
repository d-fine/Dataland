import { faker } from "@faker-js/faker";
import { CompanyReportReference } from "@clients/backend";
import { ReferencedReports } from "@e2e/fixtures/FixtureUtils";
import { humanizeOrUndefined } from "@e2e/fixtures/CsvUtils";

export function generateDataSource(referencedReports: ReferencedReports): CompanyReportReference {
  const chosenReport = faker.helpers.arrayElement(Object.keys(referencedReports));
  return {
    page: faker.mersenne.rand(1200, 1),
    report: chosenReport,
  };
}

export function getCsvDataSourceMapping<T>(
  dataPointName: string,
  companyReportGetter: (x: T) => CompanyReportReference | undefined
) {
  return [
    {
      label: `${dataPointName} Report`,
      value: (row: T) => humanizeOrUndefined(companyReportGetter(row)?.report),
    },
    {
      label: `${dataPointName} Page`,
      value: (row: T) => companyReportGetter(row)?.page,
    },
  ];
}
