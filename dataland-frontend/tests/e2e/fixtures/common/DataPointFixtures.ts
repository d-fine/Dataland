import { faker } from "@faker-js/faker";
import { type CompanyReportReference, type DocumentReference, QualityOptions } from "@clients/backend";
import { generateDataSource } from "./DataSourceFixtures";
import { pickSubsetOfElements, pickOneElement, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { generateYesNoNa } from "./YesNoFixtures";
import { DEFAULT_PROBABILITY, valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";
import { getReferencedDocumentId } from "@e2e/utils/DocumentReference";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";

const possibleReports = ["AnnualReport", "SustainabilityReport", "IntegratedReport", "ESEFReport"];

/**
 * Generates a random non-empty set of reports that can be referenced
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns a random non-empty set of reports
 */
export function generateReferencedReports(undefinedProbability = DEFAULT_PROBABILITY): ReferencedDocuments {
  const availableReportNames = pickSubsetOfElements(possibleReports);

  const referencedReports: ReferencedDocuments = {};
  for (const reportName of availableReportNames) {
    referencedReports[reportName] = {
      reference: getReferencedDocumentId(),
      isGroupLevel: valueOrUndefined(generateYesNoNa(), undefinedProbability),
      reportDate: valueOrUndefined(generatePastDate(), undefinedProbability),
      currency: generateCurrencyCode(),
    };
  }
  return referencedReports;
}

/**
 * Generates a datapoint with the given value, choosing a random quality bucket and report (might be empty/NA)
 * @param value the decimal value of the datapoint to generate
 * @param reports the reports that can be referenced as data sources
 * @param unit the unit of the datapoint to generate
 * @returns the generated datapoint
 */
export function generateDatapoint<T>(
  value: T | undefined,
  reports: ReferencedDocuments,
  unit?: string,
): GenericDataPoint<T> {
  const qualityBucket =
    value === undefined
      ? QualityOptions.Na
      : pickOneElement(Object.values(QualityOptions).filter((it) => it !== QualityOptions.Na));

  const { dataSource, comment } = generateQualityAndDataSourceAndComment(reports, qualityBucket);

  return {
    value: value ?? undefined,
    dataSource: dataSource,
    quality: qualityBucket,
    comment: comment,
    unit: unit ?? undefined,
  } as GenericDataPoint<T>;
}

export interface GenericDataPoint<T> {
  value: T | undefined;
  dataSource: CompanyReportReference | undefined;
  quality: QualityOptions;
  comment: string | undefined;
  unit: string | undefined;
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
function generateQualityAndDataSourceAndComment(
  reports: ReferencedDocuments,
  qualityBucket: QualityOptions,
): { dataSource: CompanyReportReference | undefined; comment: string | undefined } {
  let dataSource: CompanyReportReference | undefined;
  let comment: string | undefined;
  if (
    qualityBucket === QualityOptions.Audited ||
    qualityBucket === QualityOptions.Reported ||
    ((qualityBucket === QualityOptions.Estimated || qualityBucket === QualityOptions.Incomplete) &&
      faker.datatype.boolean())
  ) {
    dataSource = generateDataSource(reports);
    comment = faker.git.commitMessage();
  } else {
    dataSource = { report: "", page: undefined, tagName: undefined };
  }
  return { dataSource, comment };
}
