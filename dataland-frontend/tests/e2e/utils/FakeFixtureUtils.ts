import {
  type ReferencedDocuments,
  generateArray,
  pickOneElement,
  pickSubsetOfElements,
} from '@e2e/fixtures/FixtureUtils';
import { generateYesNo, generateYesNoNa } from '@e2e/fixtures/common/YesNoFixtures';
import {
  type AmountWithCurrency,
  type CurrencyDataPoint,
  type ExtendedDocumentReference,
  QualityOptions,
  type YesNo,
  type YesNoNa,
} from '@clients/backend';
import { generateCurrencyValue, generateFloat, generatePercentageValue } from '@e2e/fixtures/common/NumberFixtures';
import { generateReferencedDocuments, getReferencedDocumentId } from '@e2e/utils/DocumentReference';
import { generateCurrencyCode } from '@e2e/fixtures/common/CurrencyFixtures';
import { type BaseDataPoint, type ExtendedDataPoint } from '@/utils/DataPoint';
import { generateFutureDate, generatePastDate } from '@e2e/fixtures/common/DateFixtures';
import { faker } from '@faker-js/faker';
import { generateDataSource } from '@e2e/fixtures/common/DataSourceFixtures';

export const DEFAULT_PROBABILITY = 0.2;
const possibleReports = ['AnnualReport', 'SustainabilityReport', 'IntegratedReport', 'ESEFReport'];

/**
 * Randomly returns the specified value or null
 * @param value the value to return
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns the value or null
 */
export function valueOrNull<T>(value: T, nullProbability = DEFAULT_PROBABILITY): T | null {
  return faker.datatype.boolean(nullProbability) ? null : value;
}

export class Generator {
  nullProbability: number;
  reports: ReferencedDocuments;
  documents: ReferencedDocuments;

  constructor(nullProbability = DEFAULT_PROBABILITY) {
    this.nullProbability = nullProbability;
    this.reports = this.generateReferencedReports();
    this.documents = generateReferencedDocuments();
  }

  valueOrNull<T>(value: T): T | null {
    return valueOrNull(value, this.nullProbability);
  }

  randomYesNo(): YesNo | null {
    return this.valueOrNull(this.guaranteedYesNo());
  }

  guaranteedYesNo(): YesNo {
    return generateYesNo();
  }

  randomYesNoNa(): YesNoNa | null {
    return this.valueOrNull(this.guaranteedYesNoNa());
  }

  guaranteedYesNoNa(): YesNoNa {
    return generateYesNoNa();
  }

  randomPercentageValue(): number | null {
    return this.valueOrNull(this.guaranteedPercentageValue());
  }

  guaranteedPercentageValue(): number {
    return generatePercentageValue();
  }

  randomInt(min: number = 0, max: number = 10000): number | null {
    return this.valueOrNull(this.guaranteedInt(min, max));
  }

  guaranteedInt(min: number = 0, max: number = 10000): number {
    return faker.number.int({ min: min, max: max });
  }

  randomFloat(min: number = 0, max: number = 1e5): number | null {
    return this.valueOrNull(this.guaranteedFloat(min, max));
  }

  guaranteedFloat(min: number = 0, max: number = 1e5): number {
    return generateFloat(min, max);
  }

  randomCurrencyValue(): number | null {
    return this.valueOrNull(this.guaranteedCurrencyValue());
  }

  guaranteedCurrencyValue(): number {
    return generateCurrencyValue();
  }

  randomBaseDataPoint<T>(input: T): BaseDataPoint<T> | null {
    return this.valueOrNull(this.guaranteedBaseDataPoint(input));
  }

  guaranteedBaseDataPoint<T>(input: T): BaseDataPoint<T> {
    const document = this.valueOrNull(pickOneElement(Object.values(this.documents)));
    return { value: input, dataSource: document };
  }

  randomExtendedDataPoint<T>(input: T | null): ExtendedDataPoint<T> | null {
    return this.valueOrNull(this.guaranteedExtendedDataPoint(input));
  }

  guaranteedExtendedDataPoint<T>(input: T | null): ExtendedDataPoint<T> {
    return this.generateExtendedDataPoint(this.valueOrNull(input));
  }

  randomCurrencyDataPoint(min: number = 0, max: number = 1e10): CurrencyDataPoint | null {
    return this.valueOrNull(this.guaranteedCurrencyDataPoint(min, max));
  }

  guaranteedCurrencyDataPoint(min: number = 0, max: number = 1e10): CurrencyDataPoint {
    const localCurrency = generateCurrencyCode();
    const value = generateCurrencyValue(min, max);
    return this.generateCurrencyExtendedDataPoint(this.valueOrNull(value), localCurrency);
  }

  randomArray<T>(generator: () => T, min = 0, max = 5): T[] | null {
    return this.valueOrNull(this.guaranteedArray(generator, min, max));
  }

  guaranteedArray<T>(generator: () => T, min = 0, max = 5): T[] {
    return generateArray(generator, min, max);
  }

  randomFutureDate(): string | null {
    return this.valueOrNull(this.guaranteedFutureDate());
  }

  guaranteedFutureDate(): string {
    return generateFutureDate();
  }

  randomShortString(): string | null {
    return this.valueOrNull(this.guaranteedShortString());
  }

  guaranteedShortString(): string {
    return faker.company.buzzNoun();
  }

  randomParagraphs(): string | null {
    return this.valueOrNull(this.guaranteedParagraphs());
  }

  guaranteedParagraphs(): string {
    return faker.lorem.paragraphs({ min: 1, max: 1 });
  }

  /**
   * Generates a random non-empty set of reports that can be referenced
   * @param requiredReportNames reports with names that must occur
   * @returns a random non-empty set of reports
   */
  generateReferencedReports(requiredReportNames?: string[]): ReferencedDocuments {
    let availableReportNames = pickSubsetOfElements(possibleReports);
    if (requiredReportNames !== undefined) {
      availableReportNames = [...new Set(requiredReportNames.concat(availableReportNames))];
    }

    const referencedReports: ReferencedDocuments = {};
    for (const reportName of availableReportNames) {
      referencedReports[reportName] = {
        fileReference: getReferencedDocumentId(),
        fileName: this.valueOrNull(reportName),
        publicationDate: this.valueOrNull(generatePastDate()),
      };
    }
    return referencedReports;
  }

  /**
   * Generates a datapoint with the given value, choosing a random quality bucket and report (might be empty/NA)
   * @param value the value of the datapoint to generate
   * @returns the generated datapoint
   */
  generateExtendedDataPoint<T>(value: T | null): ExtendedDataPoint<T> {
    const qualityBucket = this.valueOrNull(pickOneElement(Object.values(QualityOptions)));

    let dataSource: ExtendedDocumentReference | null = this.valueOrNull(generateDataSource(this.reports));
    const comment: string | null = this.valueOrNull(faker.git.commitMessage());
    if (qualityBucket === QualityOptions.Audited || qualityBucket === QualityOptions.Reported) {
      dataSource = generateDataSource(this.reports);
    }
    return {
      value: value,
      dataSource: dataSource,
      comment: comment,
      quality: qualityBucket,
    };
  }

  /**
   * Generates a currency Datapoint with the given value, choosing a random quality bucket and report (might be empty/NA)
   * @param value the value of the datapoint to generate
   * @param currency the currency of the datapoint
   * @returns the generated datapoint
   */
  generateCurrencyExtendedDataPoint(value: number | null, currency: string | null): CurrencyDataPoint {
    const datapoint = this.generateExtendedDataPoint(value);
    return {
      ...datapoint,
      currency: currency,
    };
  }
  /**
   * Generates a random value and currency
   * @returns an AmountWithCurrency object
   */
  generateAmountWithCurrency(): AmountWithCurrency {
    return {
      amount: this.randomCurrencyValue(),
      currency: this.valueOrNull(generateCurrencyCode()),
    };
  }
}
