import { faker } from "@faker-js/faker";
import { type ExtendedDocumentReference, type BaseDocumentReference, QualityOptions } from "@clients/backend";
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
      fileReference: getReferencedDocumentId(),
      isGroupLevel: valueOrUndefined(generateYesNoNa(), undefinedProbability),
      reportDate: valueOrUndefined(generatePastDate(), undefinedProbability),
      currency: valueOrUndefined(generateCurrencyCode(), undefinedProbability),
    };
  }
  return referencedReports;
}

/**
 * Generates a datapoint with the given value, choosing a random quality bucket and report (might be empty/NA)
 * @param value the decimal value of the datapoint to generate
 * @param reports the reports that can be referenced as data sources
 * @param currency the currency of the datapoint to generate
 * @returns the generated datapoint
 */
export function generateDatapoint<T>(
  value: T | undefined,
  reports: ReferencedDocuments,
  currency?: string,
): GenericDataPoint<T> {
  const qualityBucket =
    value === undefined
      ? QualityOptions.Na
      : pickOneElement(Object.values(QualityOptions).filter((it) => it !== QualityOptions.Na));

  let dataSource: ExtendedDocumentReference | undefined = undefined;
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
    value: value ?? undefined,
    dataSource: dataSource,
    quality: qualityBucket,
    comment: comment,
    currency: currency ?? undefined,
  } as GenericDataPoint<T>;
}

export interface GenericDataPoint<T> {
  value: T | undefined;
  dataSource: ExtendedDocumentReference | undefined;
  quality: QualityOptions;
  comment: string | undefined;
  currency: string | undefined;
}

export interface GenericBaseDataPoint<T> {
  value: T;
  dataSource: BaseDocumentReference | undefined;
}
