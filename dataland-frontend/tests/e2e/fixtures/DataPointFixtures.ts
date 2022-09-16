import { faker } from "@faker-js/faker";
import { DataPoint, QualityOptions, CompanyReportReference } from "../../../build/clients/backend";

function generateDataSource(): CompanyReportReference {
  return {
    page: faker.mersenne.rand(1200, 1),
    report: new URL(`${faker.internet.domainWord()}.pdf`, faker.internet.url()).href,
  };
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
