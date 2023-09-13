import { faker } from "@faker-js/faker";
import { type CompanyReportReference, type DocumentReference, QualityOptions } from "@clients/backend";
import { generateDataSource } from "./DataSourceFixtures";
import { type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { randomYesNoNa } from "./YesNoFixtures";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { randomPastDate } from "@e2e/fixtures/common/DateFixtures";
import { getReferencedDocumentId } from "@e2e/utils/DocumentReference";

const possibleReports = ["AnnualReport", "SustainabilityReport", "IntegratedReport", "ESEFReport"];

/**
 * Generates a random non-empty set of reports that can be referenced
 * @returns a random non-empty set of reports
 */
export function generateReferencedReports(): ReferencedDocuments {
  const availableReportNames = faker.helpers.arrayElements(possibleReports, { min: 1, max: possibleReports.length });

  const referencedReports: ReferencedDocuments = {};
  for (const reportName of availableReportNames) {
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
 * Generates a datapoint with the given value, choosing a random quality bucket and report (might be empty/NA)
 * @param value the decimal value of the datapoint to generate
 * @param reports the reports that can be referenced as data sources
 * @returns the generated datapoint
 */
export function generateDatapoint<T>(value: T | null, reports: ReferencedDocuments): GenericDataPoint<T> {
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
  } as GenericDataPoint<T>;
}

export interface GenericDataPoint<T> {
  value: T | undefined;
  dataSource: CompanyReportReference | undefined;
  quality: QualityOptions;
  comment: string | undefined;
}

export interface GenericBaseDataPoint<T> {
  value: T;
  dataSource: DocumentReference | undefined;
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
