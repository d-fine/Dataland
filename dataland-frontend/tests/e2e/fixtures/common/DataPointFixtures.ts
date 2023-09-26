import { faker } from "@faker-js/faker";
import {
  type CompanyReport,
  type CompanyReportReference,
  type DocumentReference,
  QualityOptions,
  type YesNoNa,
} from "@clients/backend";
import { generateDataSource } from "./DataSourceFixtures";
import { pickSubsetOfElements, pickOneElement, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { generateYesNoNa } from "./YesNoFixtures";
import { DEFAULT_PROBABILITY, valueOrMissing } from "@e2e/utils/FakeFixtureUtils";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";
import { getReferencedDocumentId } from "@e2e/utils/DocumentReference";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";

const possibleReports = ["AnnualReport", "SustainabilityReport", "IntegratedReport", "ESEFReport"];

/**
 * Generates a random non-empty set of reports that can be referenced
 * @param missingValueProbability the probability (as number between 0 and 1) for missing values in optional fields
 * @param setMissingValuesToNull decides whether missing values are represented by "undefined" or "null"
 * @returns a random non-empty set of reports
 */
export function generateReferencedReports(
  missingValueProbability = DEFAULT_PROBABILITY,
  setMissingValuesToNull: boolean,
): ReferencedDocuments {
  const availableReportNames = pickSubsetOfElements(possibleReports);

  const referencedReports: ReferencedDocuments = {};
  for (const reportName of availableReportNames) {
    referencedReports[reportName] = {
      reference: getReferencedDocumentId(),
      isGroupLevel: valueOrMissing(generateYesNoNa(), missingValueProbability, setMissingValuesToNull),
      reportDate: valueOrMissing(generatePastDate(), missingValueProbability, setMissingValuesToNull),
      currency: generateCurrencyCode(),
    };
  }
  return referencedReports;
}

/**
 * Generates a datapoint with the given value, choosing a random quality bucket and report (might be empty/NA)
 * @param value the value of the datapoint to generate
 * @param reports the reports that can be referenced as data sources
 * @param setMissingValuesToNull decides whether missing values are represented by "undefined" or "null"
 * @param unit the unit of the datapoint to generate
 * @returns the generated datapoint
 */
export function generateDatapoint<T>(
  value: T | undefined | null,
  reports: ReferencedDocuments,
  setMissingValuesToNull: boolean,
  unit?: string,
): GenericDataPoint<T> {
  const qualityBucket =
    value === undefined || value === null
      ? QualityOptions.Na
      : pickOneElement(Object.values(QualityOptions).filter((it) => it !== QualityOptions.Na));

  const { dataSource, comment } = generateQualityAndDataSourceAndComment(
    reports,
    qualityBucket,
    setMissingValuesToNull,
  );

  return {
    value: value,
    dataSource: dataSource,
    quality: qualityBucket,
    comment: comment,
    unit: setMissingValuesToNull ? unit ?? null : unit,
  };
}

export interface GenericDataPoint<T> {
  value: T | undefined | null;
  dataSource: CompanyReportReference;
  quality: QualityOptions;
  comment: string | undefined | null;
  unit: string | undefined | null;
}

export interface GenericBaseDataPoint<T> {
  value: T;
  dataSource: CompanyReport | DocumentReference | undefined | null;
}

/**
 * This method constructs the data source and the comment for the fake datapoint
 * @param reports the reports that can be referenced as data sources
 * @param qualityBucket the quality bucket of the datapoint
 * @param setMissingValuesToNull decides whether missing values are represented by "undefined" or "null"
 * @returns the generated data source and comment of the datapoint
 */
function generateQualityAndDataSourceAndComment(
  reports: ReferencedDocuments,
  qualityBucket: QualityOptions,
  setMissingValuesToNull: boolean,
): { dataSource: CompanyReportReference; comment: string | undefined | null } {
  let dataSource: CompanyReportReference;
  let comment: string | undefined | null;
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
    if (setMissingValuesToNull) {
      comment = null;
    }
  }
  return { dataSource, comment };
}
