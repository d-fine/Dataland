import { faker } from "@faker-js/faker";
import { DataPointBigDecimal, QualityOptions, CompanyReportReference, DataPointYesNo } from "@clients/backend";
import { generateDataSource, getCsvDataSourceMapping } from "./DataSourceFixtures";
import { DataPoint, ReferencedReports } from "@e2e/fixtures/FixtureUtils";
import { randomYesNoNaUndefined, randomYesNoUndefined } from "./YesNoFixtures";
import { humanizeOrUndefined } from "@e2e/fixtures/CsvUtils";
import { randomPastDateOrUndefined } from "./DateFixtures";

const possibleReports = ["AnnualReport", "SustainabilityReport", "IntegratedReport", "ESEFReport"];
const nullRatio = 0.1;
const undefinedRatio = 0.25;

export function valueOrNull<T>(value: T): T | null {
  return Math.random() > nullRatio ? value : null;
}

export function valueOrUndefined<T>(value: T): T | undefined {
  return Math.random() > undefinedRatio ? value : undefined;
}

export function generateReferencedReports(): ReferencedReports {
  const availableReports = faker.helpers.arrayElements(possibleReports);
  if (availableReports.length == 0) availableReports.push(possibleReports[0]);

  const referencedReports: ReferencedReports = {};
  availableReports.forEach((reportName) => {
    referencedReports[reportName] = {
      reference: new URL(`${faker.internet.domainWord()}.pdf`, faker.internet.url()).href,
      isGroupLevel: randomYesNoNaUndefined(),
      reportDate: randomPastDateOrUndefined(),
      currency: faker.finance.currencyCode(),
    };
  });
  return referencedReports;
}

export function generateNumericOrEmptyDatapoint(
  reports: ReferencedReports,
  value: number | null = valueOrNull(faker.datatype.number())
): DataPointBigDecimal | undefined {
  if (Math.random() < undefinedRatio) return undefined;
  return generateDatapoint(value, reports);
}

export function generateYesNoOrEmptyDatapoint(reports: ReferencedReports): DataPointYesNo | undefined {
  const value = valueOrNull(randomYesNoUndefined());
  if (value === undefined) return undefined;
  return generateDatapoint(value, reports);
}

export function generateDatapointOrNotReportedAtRandom(
  value: number | undefined,
  reports: ReferencedReports
): DataPointBigDecimal | undefined {
  if (value === undefined) return undefined;
  return generateDatapoint(valueOrNull(value), reports);
}

export function generateDatapoint<T, Y>(value: T | null, reports: ReferencedReports): Y {
  const qualityBucket =
    value === null
      ? QualityOptions.Na
      : faker.helpers.arrayElement(Object.values(QualityOptions).filter((it) => it !== QualityOptions.Na));

  let dataSource: CompanyReportReference | undefined = undefined;
  let comment: string | undefined = undefined;
  if (
    qualityBucket === QualityOptions.Audited ||
    qualityBucket === QualityOptions.Reported ||
    ((qualityBucket === QualityOptions.Estimated || qualityBucket === QualityOptions.Incomplete) &&
      faker.datatype.boolean())
  ) {
    dataSource = generateDataSource(reports);
    comment = faker.git.commitMessage();
  }

  return {
    value: value || undefined,
    dataSource: dataSource,
    quality: qualityBucket,
    comment: comment,
  } as Y;
}

export function getCsvDataPointMapping<T>(
  dataPointName: string,
  dataPointGetter: (row: T) => DataPointBigDecimal | undefined,
  valueConverter: (input: number | undefined) => string = (x): string => x?.toString() || ""
): Array<DataPoint<T, string | number>> {
  return [
    {
      label: dataPointName,
      value: (row: T): string | undefined => valueConverter(dataPointGetter(row)?.value),
    },
    {
      label: `${dataPointName} Quality`,
      value: (row: T): string | undefined => humanizeOrUndefined(dataPointGetter(row)?.quality),
    },
    {
      label: `${dataPointName} Comment`,
      value: (row: T): string | undefined => dataPointGetter(row)?.comment,
    },
    ...getCsvDataSourceMapping<T>(dataPointName, (row: T) => dataPointGetter(row)?.dataSource),
  ];
}
