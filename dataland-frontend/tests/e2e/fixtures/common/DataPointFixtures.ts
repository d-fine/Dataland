import { faker } from "@faker-js/faker";
import { readFileSync } from "fs";
import { createHash } from "crypto";
import { CompanyReportReference, DataPointBigDecimal, DataPointYesNo, QualityOptions } from "@clients/backend";
import { generateDataSource, getCsvDataSourceMapping } from "./DataSourceFixtures";
import { DataPoint, ReferencedReports } from "@e2e/fixtures/FixtureUtils";
import { randomYesNoNaUndefined, randomYesNoUndefined } from "./YesNoFixtures";
import { humanizeOrUndefined } from "@e2e/fixtures/CsvUtils";
import { randomPastDateOrUndefined } from "./DateFixtures";

const possibleReports = ["AnnualReport", "SustainabilityReport", "IntegratedReport", "ESEFReport"];
const nullRatio = 0.1;
const undefinedRatio = 0.25;

/**
 * Randomly returns the specified value or null
 * @param value the value to return
 * @returns the value or null
 */
export function valueOrNull<T>(value: T): T | null {
  return Math.random() > nullRatio ? value : null;
}

/**
 * Randomly returns the specified value or undefined
 * @param value the value to return
 * @returns the value or undefined
 */
export function valueOrUndefined<T>(value: T): T | undefined {
  return Math.random() > undefinedRatio ? value : undefined;
}

/**
 * Generates a random link to a pdf document
 * @returns random link to a pdf document
 */
export function generateLinkToPdf(): string {
  return new URL(`${faker.internet.domainWord()}.pdf`, faker.internet.url()).href;
}

/**
 * Generates hash to fixture pdf that is used for all fake fixture references
 * @returns documentId ID of a pdf that is stored in internal storage and can be referenced
 */
export function getReferencedDocumentId(): string {
  const testDocumentPath = "../testing/data/documents/StandardWordExport.pdf";
  const fileContent: Buffer = readFileSync(testDocumentPath);
  return createHash("sha256").update(fileContent).digest("hex");
}

/**
 * Generates a random non-empty set of reports that can be referenced
 * @returns a random non-empty set of reports
 */
export function generateReferencedReports(): ReferencedReports {
  const availableReports = faker.helpers.arrayElements(possibleReports);
  if (availableReports.length == 0) availableReports.push(possibleReports[0]);

  const referencedReports: ReferencedReports = {};
  for (const reportName of availableReports) {
    referencedReports[reportName] = {
      reference: getReferencedDocumentId(),
      isGroupLevel: randomYesNoNaUndefined(),
      reportDate: randomPastDateOrUndefined(),
      currency: faker.finance.currencyCode(),
    };
  }
  return referencedReports;
}

/**
 * Randomly returns a datapoint with the specified value (chosen at random between 0 and 99999 if not specified) or
 * undefined
 * @param reports the reports that can be referenced as data sources
 * @param value the value of the datapoint to generate (chosen at random between 0 and 99999 if not specified)
 * @returns the generated datapoint or undefined
 */
export function generateNumericOrEmptyDatapoint(
  reports: ReferencedReports,
  value: number | null = valueOrNull(faker.datatype.number())
): DataPointBigDecimal | undefined {
  if (Math.random() < undefinedRatio) return undefined;
  return generateDatapoint(value, reports);
}

/**
 * Randomly generates a Yes / No / Na / undefined datapoint
 * @param reports the reports that can be referenced as data sources
 * @returns the generated datapoint or undefined
 */
export function generateYesNoOrEmptyDatapoint(reports: ReferencedReports): DataPointYesNo | undefined {
  const value = valueOrNull(randomYesNoUndefined());
  if (value === undefined) return undefined;
  return generateDatapoint(value, reports);
}

/**
 * Generates a datapoint with the given value or a datapoint with no value reported at random
 * @param value the decimal value of the datapoint to generate (is ignored at random)
 * @param reports the reports that can be referenced as data sources
 * @returns the generated datapoint or undefined
 */
export function generateDatapointOrNotReportedAtRandom(
  value: number | undefined,
  reports: ReferencedReports
): DataPointBigDecimal | undefined {
  if (value === undefined) return undefined;
  return generateDatapoint(valueOrNull(value), reports);
}

/**
 * Generates a datapoint with the given value, choosing a random quality bucket and report (might be empty/NA)
 * @param value the decimal value of the datapoint to generate
 * @param reports the reports that can be referenced as data sources
 * @returns the generated datapoint
 */
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

/**
 * Generates the CSV mapping for a single (decimal) datapoint
 * @param dataPointName the name of the datapoint
 * @param dataPointGetter a function that can be used to access the datapoint given the current fixture element
 * @param valueConverter a conversion function for formatting the number (i.e. that converts the decimal number to a percentage string)
 * @returns the CSV mapping for the datapoint
 */
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
