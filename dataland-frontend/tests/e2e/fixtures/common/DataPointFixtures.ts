import { faker } from "@faker-js/faker";
import { type CurrencyDataPoint, type ExtendedDocumentReference, QualityOptions } from "@clients/backend";
import { generateDataSource } from "./DataSourceFixtures";
import { pickSubsetOfElements, pickOneElement, type ReferencedDocuments } from "@e2e/fixtures/FixtureUtils";
import { generateYesNoNa } from "./YesNoFixtures";
import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";
import { getReferencedDocumentId } from "@e2e/utils/DocumentReference";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { type ExtendedDataPoint } from "@/utils/DataPoint";
import { generateFloat } from "@e2e/fixtures/common/NumberFixtures";
const possibleReports = ["AnnualReport", "SustainabilityReport", "IntegratedReport", "ESEFReport"];

export class DataPointGenerator extends Generator {
  constructor(nullProbability = DEFAULT_PROBABILITY) {
    super(nullProbability);
  }
  /**
   * Generates a random non-empty set of reports that can be referenced
   * @param requiredReportNames reports with names that must occur
   * @returns a random non-empty set of reports
   */
  generateReferencedReports(requiredReportNames?: string[]): ReferencedDocuments {
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
        fileName: reportName,
        isGroupLevel: this.valueOrNull(generateYesNoNa()),
        reportDate: this.valueOrNull(generatePastDate()),
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
  generateDataPoint<T>(
    value: T | null,
    reports: ReferencedDocuments,
    currency?: string | null,
  ): ExtendedDataPoint<T> | CurrencyDataPoint {
    if (currency != undefined && value != null && typeof value != "number") {
      throw TypeError("Parameter `currency` is defined but parameter `value` is not of type number");
    }
    const qualityBucket =
      value === null
        ? QualityOptions.Na
        : pickOneElement(Object.values(QualityOptions).filter((it) => it !== QualityOptions.Na));
    const { dataSource, comment } = this.generateQualityAndDataSourceAndComment(reports, qualityBucket);
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
  generateQualityAndDataSourceAndComment(
    reports: ReferencedDocuments,
    qualityBucket: QualityOptions,
  ): { dataSource: ExtendedDocumentReference | null; comment: string | null } {
    let dataSource: ExtendedDocumentReference | null = null;
    let comment: string | null = null;
    if (
      qualityBucket === QualityOptions.Audited ||
      qualityBucket === QualityOptions.Reported ||
      ((qualityBucket === QualityOptions.Estimated || qualityBucket === QualityOptions.Incomplete) &&
        generateFloat(0, 1) > this.nullProbability)
    ) {
      dataSource = generateDataSource(reports);
      comment = faker.git.commitMessage();
    }
    return { dataSource, comment };
  }
}
