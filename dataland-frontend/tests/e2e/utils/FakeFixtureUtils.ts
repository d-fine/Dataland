import {
  generateCurrencyExtendedDataPoint,
  generateExtendedDataPoint,
  generateReferencedReports,
} from "@e2e/fixtures/common/DataPointFixtures";
import { type ReferencedDocuments, generateArray, pickOneElement } from "@e2e/fixtures/FixtureUtils";
import { generateYesNo, generateYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { type CurrencyDataPoint, type YesNo, type YesNoNa } from "@clients/backend";
import {
  generateCurrencyValue,
  generateFloat,
  generateInt,
  generatePercentageValue,
} from "@e2e/fixtures/common/NumberFixtures";
import { generateReferencedDocuments } from "@e2e/utils/DocumentReference";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { type BaseDataPoint, type ExtendedDataPoint } from "@/utils/DataPoint";
import { generateFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { faker } from "@faker-js/faker";

export const DEFAULT_PROBABILITY = 0.2;

/**
 * Randomly returns the specified value or null
 * @param value the value to return
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns the value or null
 */
export function valueOrNull<T>(value: T, nullProbability = DEFAULT_PROBABILITY): T | null {
  return Math.random() > nullProbability ? value : null;
}

export class Generator {
  nullProbability: number;
  reports: ReferencedDocuments;
  documents: ReferencedDocuments;

  constructor(nullProbability = DEFAULT_PROBABILITY) {
    this.nullProbability = nullProbability;
    this.reports = generateReferencedReports(nullProbability);
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

  randomInt(max = 10000): number | null {
    return this.valueOrNull(this.guaranteedInt(max));
  }

  guaranteedInt(max: number = 10000): number {
    return generateInt(max);
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
    return generateExtendedDataPoint(this.valueOrNull(input), this.reports);
  }

  randomCurrencyDataPoint(input = generateCurrencyValue()): CurrencyDataPoint | null {
    return this.valueOrNull(this.guaranteedCurrencyDataPoint(input));
  }

  guaranteedCurrencyDataPoint(input = generateCurrencyValue()): CurrencyDataPoint {
    const localCurrency = generateCurrencyCode();
    return generateCurrencyExtendedDataPoint(this.valueOrNull(input), this.reports, localCurrency);
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
}
