import { faker } from "@faker-js/faker";
import { CompanyReportReference } from "../../../build/clients/backend";
import { ReferencedReports } from "./Utils";
import { humaniseOrUndefined } from "./CsvUtils";

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
      value: (row: T) => humaniseOrUndefined(companyReportGetter(row)?.report),
    },
    {
      label: `${dataPointName} Page`,
      value: (row: T) => companyReportGetter(row)?.page,
    },
  ];
}
