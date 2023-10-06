import { faker } from "@faker-js/faker";
import { type ExtendedDocumentReference, type BaseDocumentReference, QualityOptions } from "@clients/backend";
import { generateDataSource } from "./DataSourceFixtures";
import { pickSubsetOfElements, pickOneElement, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { generateYesNoNa } from "./YesNoFixtures";
import { DEFAULT_PROBABILITY, valueOrNull } from "@e2e/utils/FakeFixtureUtils";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";
import { getReferencedDocumentId } from "@e2e/utils/DocumentReference";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";

const possibleReports = ["AnnualReport", "SustainabilityReport", "IntegratedReport", "ESEFReport"];

/**
 * Generates a random non-empty set of reports that can be referenced
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @param requiredReportNames reports with names that must occur
 * @returns a random non-empty set of reports
 */
export function generateReferencedReports(
  nullProbability = DEFAULT_PROBABILITY,
  requiredReportNames?: string[],
): ReferencedDocuments {
  const availableReportNames = pickSubsetOfElements(possibleReports);
  requiredReportNames?.forEach((reportName) => {
    if (!availableReportNames.includes(reportName)) {
      availableReportNames.push(reportName);
    }
  });

  const referencedReports: ReferencedDocuments = {};
  for (const reportName of availableReportNames) {
    referencedReports[reportName] = {
      fileReference: getReferencedDocumentId(),
      isGroupLevel: valueOrNull(generateYesNoNa(), nullProbability),
      reportDate: valueOrNull(generatePastDate(), nullProbability),
      currency: generateCurrencyCode(),
    };
  }
  return referencedReports;
}

/**
 * Generates a datapoint with the given value, choosing a random quality bucket and report (might be empty/NA)
 * @param value the value of the datapoint to generate
 * @param reports the reports that can be referenced as data sources
 * @param currency the currency of the datapoint to generate
 * @returns the generated datapoint
 */
export function generateDataPoint<T>(
  value: T | null,
  reports: ReferencedDocuments,
  currency?: string | null,
): GenericDataPoint<T> {
  const qualityBucket =
    value === null
      ? QualityOptions.Na
      : pickOneElement(Object.values(QualityOptions).filter((it) => it !== QualityOptions.Na));

  let dataSource: ExtendedDocumentReference | null = null;
  let comment: string | null = null;

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
    value: value,
    dataSource: dataSource,
    quality: qualityBucket,
    comment: comment,
    currency: currency,
  } as GenericDataPoint<T>;
}

export interface GenericDataPoint<T> {
  value: T | null;
  dataSource: ExtendedDocumentReference | null;
  quality: QualityOptions;
  comment: string | null;
  currency?: string | null;
}

export interface GenericBaseDataPoint<T> {
  value: T;
  dataSource: BaseDocumentReference | null;
}
