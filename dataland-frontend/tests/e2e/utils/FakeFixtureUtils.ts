import {
  generateDatapoint,
  generateReferencedReports,
  type GenericDataPoint,
  type GenericBaseDataPoint,
} from "@e2e/fixtures/common/DataPointFixtures";
import { type ReferencedDocuments, generateArray, pickOneElement } from "@e2e/fixtures/FixtureUtils";
import { generateYesNo, generateYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { type YesNo, type YesNoNa } from "@clients/backend";
import { generateCurrencyValue, generateInt, generatePercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { generateReferencedDocuments } from "@e2e/utils/DocumentReference";

export const DEFAULT_PROBABILITY = 0.2;

/**
 * Randomly returns the specified value or undefined
 * @param value the value to return
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns the value or undefined
 */
export function valueOrUndefined<T>(value: T, undefinedProbability = DEFAULT_PROBABILITY): T | undefined {
  return Math.random() > undefinedProbability ? value : undefined;
}

export class Generator {
  undefinedProbability: number;
  reports: ReferencedDocuments;
  documents: ReferencedDocuments;

  constructor(undefinedProbability = DEFAULT_PROBABILITY) {
    this.undefinedProbability = undefinedProbability;
    this.reports = generateReferencedReports(undefinedProbability);
    this.documents = generateReferencedDocuments();
  }

  valueOrUndefined<T>(value: T): T | undefined {
    return valueOrUndefined(value, this.undefinedProbability);
  }

  randomYesNo(): YesNo | undefined {
    return this.valueOrUndefined(generateYesNo());
  }

  randomYesNoNa(): YesNoNa | undefined {
    return this.valueOrUndefined(generateYesNoNa());
  }

  randomPercentageValue(): number | undefined {
    return this.valueOrUndefined(generatePercentageValue());
  }

  randomInt(max = 10000): number | undefined {
    return this.valueOrUndefined(generateInt(max));
  }

  randomCurrencyValue(): number | undefined {
    return this.valueOrUndefined(generateCurrencyValue());
  }

  randomBaseDataPoint<T>(input: T): GenericBaseDataPoint<T> | undefined {
    const document = this.valueOrUndefined(pickOneElement(Object.values(this.documents)));
    return this.valueOrUndefined({ value: input, dataSource: document } as GenericBaseDataPoint<T>);
  }

  randomDataPoint<T>(input: T, currency?: string): GenericDataPoint<T> | undefined {
    return this.valueOrUndefined(generateDatapoint(this.valueOrUndefined(input), this.reports, currency));
  }

  randomArray<T>(generator: () => T, min = 0, max = 5): T[] | undefined {
    return this.valueOrUndefined(generateArray(generator, min, max));
  }
}
