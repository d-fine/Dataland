import { faker } from "@faker-js/faker";
import { DataPointBigDecimal, QualityOptions, CompanyReportReference } from "../../../build/clients/backend";
import { generateDataSource, getCsvDataSourceMapping } from "./DataSourceFixtures";
import { ReferencedReports } from "./Utils";
import { randomYesNoNaUndefined } from "./YesNoFixtures";

const possibleReports = ["AnnualReport", "SustainabilityReport", "IntegratedReport"];

export function generateReferencedReports(): ReferencedReports {
  const availableReports = faker.helpers.arrayElements(possibleReports);
  if (availableReports.length == 0) availableReports.push(possibleReports[0]);

  const ret: ReferencedReports = {};
  availableReports.forEach((reportName) => {
    ret[reportName] = {
      reference: new URL(`${faker.internet.domainWord()}.pdf`, faker.internet.url()).href,
      isGroupLevel: randomYesNoNaUndefined(),
    };
  });

  return ret;
}

export function generateDatapointOrNotReportedAtRandom(
  value: number | undefined,
  reports: ReferencedReports
): DataPointBigDecimal | undefined {
  if (value === undefined) return undefined;
  return generateDatapoint(Math.random() > 0.1 ? value : null, reports);
}

export function generateDatapoint(value: number | null, reports: ReferencedReports): DataPointBigDecimal {
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
    dataSource = generateDataSource(reports);
  }

  return {
    value: value || undefined,
    dataSource: dataSource,
    quality: qualityBucket,
  };
}

export function getCsvDataPointMapping<T>(
  dataPointName: string,
  dataPointGetter: (row: T) => DataPointBigDecimal | undefined,
  valueConverter: (input: number | undefined) => string = (x) => x?.toString() || ""
) {
  return [
    {
      label: dataPointName,
      value: (row: T) => valueConverter(dataPointGetter(row)?.value),
    },
    {
      label: `${dataPointName} Quality`,
      value: (row: T) => dataPointGetter(row)?.quality,
    },
    ...getCsvDataSourceMapping<T>(dataPointName, (row: T) => dataPointGetter(row)?.dataSource),
  ];
}
