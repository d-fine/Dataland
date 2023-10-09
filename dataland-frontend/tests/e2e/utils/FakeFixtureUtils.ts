import {
  generateDataPoint,
  generateReferencedReports,
  type GenericDataPoint,
  type GenericBaseDataPoint,
} from "@e2e/fixtures/common/DataPointFixtures";
import { type ReferencedDocuments, generateArray, pickOneElement } from "@e2e/fixtures/FixtureUtils";
import { generateYesNo, generateYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { type CurrencyDataPoint, type YesNo, type YesNoNa } from "@clients/backend";
import { generateCurrencyValue, generateInt, generatePercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { generateReferencedDocuments } from "@e2e/utils/DocumentReference";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";

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
    return this.valueOrNull(generateYesNo());
  }

  randomYesNoNa(): YesNoNa | null {
    return this.valueOrNull(generateYesNoNa());
  }

  randomPercentageValue(): number | null {
    return this.valueOrNull(generatePercentageValue());
  }

  randomInt(max = 10000): number | null {
    return this.valueOrNull(generateInt(max));
  }

  randomCurrencyValue(): number | null {
    return this.valueOrNull(generateCurrencyValue());
  }

  randomBaseDataPoint<T>(input: T): GenericBaseDataPoint<T> | null {
    const document = this.valueOrNull(pickOneElement(Object.values(this.documents)));
    return this.valueOrNull({ value: input, dataSource: document } as GenericBaseDataPoint<T>);
  }

  randomExtendedDataPoint<T>(input: T): GenericDataPoint<T> | null {
    return this.valueOrNull(generateDataPoint(this.valueOrNull(input), this.reports));
  }

  randomCurrencyDataPoint(input = generateCurrencyValue()): CurrencyDataPoint | null {
    const localCurrency = generateCurrencyCode();
    return this.valueOrNull(generateDataPoint(this.valueOrNull(input), this.reports, localCurrency));
  }

  randomArray<T>(generator: () => T, min = 0, max = 5): T[] | null {
    return this.valueOrNull(generateArray(generator, min, max));
  }
}
