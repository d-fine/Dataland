import { faker } from "@faker-js/faker";
import { CompanyReportReference, DataPointBigDecimal, DataPointYesNo, QualityOptions } from "@clients/backend";
import { generateDataSource } from "./DataSourceFixtures";
import { ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { randomYesNo, randomYesNoNa } from "./YesNoFixtures";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { randomPastDate } from "@e2e/fixtures/common/DateFixtures";
import { getReferencedDocumentId } from "@e2e/utils/DocumentReference";

const possibleReports = ["AnnualReport", "SustainabilityReport", "IntegratedReport", "ESEFReport"];
const nullRatio = 0.1;

/**
 * Randomly returns the specified value or null
 * @param value the value to return
 * @returns the value or null
 */
export function valueOrNull<T>(value: T): T | null {
  return Math.random() > nullRatio ? value : null;
}

/**
 * Generates a random link to a pdf document
 * @returns random link to a pdf document
 */
export function generateLinkToPdf(): string {
  return new URL(`${faker.internet.domainWord()}.pdf`, faker.internet.url()).href;
}

/**
 * Generates a random non-empty set of reports that can be referenced
 * @returns a random non-empty set of reports
 */
export function generateReferencedReports(): ReferencedDocuments {
  const availableReports = faker.helpers.arrayElements(possibleReports);
  if (availableReports.length == 0) availableReports.push(possibleReports[0]);

  const referencedReports: ReferencedDocuments = {};
  for (const reportName of availableReports) {
    referencedReports[reportName] = {
      reference: getReferencedDocumentId(),
      isGroupLevel: valueOrUndefined(randomYesNoNa()),
      reportDate: valueOrUndefined(randomPastDate()),
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
  reports: ReferencedDocuments,
  value: number | null = valueOrNull(faker.number.int()),
): DataPointBigDecimal | undefined {
  return valueOrUndefined(generateDatapoint(value, reports));
}

/**
 * Randomly returns a datapoint with the specified value (chosen at random between 0 and 99999 if not specified)
 * @param reports the reports that can be referenced as data sources
 * @param value the value of the datapoint to generate (chosen at random between 0 and 99999 if not specified)
 * @returns the generated datapoint
 */
export function generateNumericDatapoint(
  reports: ReferencedDocuments,
  value: number | null = valueOrNull(faker.number.int()),
): DataPointBigDecimal {
  return generateDatapoint(value, reports);
}

/**
 * Randomly generates a Yes / No / Na / undefined datapoint
 * @param reports the reports that can be referenced as data sources
 * @returns the generated datapoint or undefined
 */
export function generateYesNoOrEmptyDatapoint(reports: ReferencedDocuments): DataPointYesNo | undefined {
  return valueOrUndefined(generateDatapoint(randomYesNo(), reports));
}

/**
 * Generates a datapoint with the given value or a datapoint with no value reported at random
 * @param value the decimal value of the datapoint to generate (is ignored at random)
 * @param reports the reports that can be referenced as data sources
 * @returns the generated datapoint or undefined
 */
export function generateDatapointOrNotReportedAtRandom(
  value: number | undefined,
  reports: ReferencedDocuments,
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
export function generateDatapoint<T, Y>(value: T | null, reports: ReferencedDocuments): Y {
  const qualityBucket =
    value === null
      ? QualityOptions.Na
      : faker.helpers.arrayElement(Object.values(QualityOptions).filter((it) => it !== QualityOptions.Na));

  const { dataSource, comment } = createQualityAndDataSourceAndComment(reports, qualityBucket);

  return {
    value: value ?? undefined,
    dataSource: dataSource,
    quality: qualityBucket,
    comment: comment,
  } as Y;
}

/**
 * This method constructs the data source and the comment for the fake datapoint
 * @param reports the reports that can be referenced as data sources
 * @param qualityBucket the quality bucket of the datapoint
 * @returns the generated data source and comment of the datapoint
 */
function createQualityAndDataSourceAndComment(
  reports: ReferencedDocuments,
  qualityBucket: QualityOptions,
): { dataSource: CompanyReportReference | undefined; comment: string | undefined } {
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
  return { dataSource, comment };
}

/**
 * Generates a datapoint with the given value, choosing a random quality bucket and report (might be empty/NA)
 * @param valueAsAbsolute the decimal value of the datapoint to generate
 * @param valueAsPercentage the percentage of the datapoint to generate
 * @param reports the reports that can be referenced as data sources
 * @returns the generated datapoint
 */
export function generateDatapointAbsoluteAndPercentage<T, Y>(
  valueAsAbsolute: T | null,
  valueAsPercentage: T | null,
  reports: ReferencedDocuments,
): Y {
  const qualityBucket =
    valueAsAbsolute === null
      ? QualityOptions.Na
      : faker.helpers.arrayElement(Object.values(QualityOptions).filter((it) => it !== QualityOptions.Na));
  const { dataSource, comment } = createQualityAndDataSourceAndComment(reports, qualityBucket);

  return {
    valueAsPercentage: valueAsPercentage ?? undefined,
    dataSource: dataSource,
    quality: qualityBucket,
    comment: comment,
    valueAsAbsolute: valueAsAbsolute ?? undefined,
  } as Y;
}
