import { faker } from "@faker-js/faker";
import {CurrencyDataPoint, type ExtendedDocumentReference, QualityOptions} from "@clients/backend";
import { generateDataSource } from "./DataSourceFixtures";
import { pickSubsetOfElements, pickOneElement, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { generateYesNoNa } from "./YesNoFixtures";
import { DEFAULT_PROBABILITY, valueOrNull } from "@e2e/utils/FakeFixtureUtils";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";
import { getReferencedDocumentId } from "@e2e/utils/DocumentReference";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { type ExtendedDataPoint } from "@/utils/DataPoint";

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
): ExtendedDataPoint<T> | CurrencyDataPoint {
  if(currency != undefined && value != null && typeof value != "number") {
    throw TypeError("Parameter `currency` is defined but parameter `value` is not of type number")
  }

  const qualityBucket =
    value === null
      ? QualityOptions.Na
      : pickOneElement(Object.values(QualityOptions).filter((it) => it !== QualityOptions.Na));

  const { dataSource, comment } = generateQualityAndDataSourceAndComment(reports, qualityBucket);

  return {
    value: value,
    dataSource: dataSource,
    quality: qualityBucket,
    comment: comment,
    currency: currency,
  };
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
): { dataSource: ExtendedDocumentReference | null; comment: string | null } {
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
  return { dataSource, comment };
}
