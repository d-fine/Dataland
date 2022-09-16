import { faker } from "@faker-js/faker";
import { DataPoint, QualityOptions, CompanyReportReference } from "../../../build/clients/backend";
import { generateDataSource } from "./DataSourceFixtures";

export function generateDatapointOrNotReportedAtRandom(value: number | undefined): DataPoint | undefined {
  if (value === undefined) return undefined;
  return generateDatapoint(Math.random() > 0.1 ? value : null);
}

export function generateDatapoint(value: number | null): DataPoint {
  const qualityBucket =
    value === null
      ? QualityOptions.Na
      : faker.helpers.arrayElement(Object.values(QualityOptions).filter((it) => it !== QualityOptions.Na));

  let dataSource: CompanyReportReference | undefined = undefined;
  if (
    qualityBucket === QualityOptions.Audited ||
    qualityBucket === QualityOptions.Reported ||
    ((qualityBucket === QualityOptions.Estimated || qualityBucket === QualityOptions.Incomplete) &&
      faker.datatype.boolean())
  ) {
    dataSource = generateDataSource();
  }

  return {
    value: value || undefined,
    dataSource: dataSource,
    quality: qualityBucket,
  };
}
