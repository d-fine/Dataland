import {
  generateDatapoint,
  generateReferencedReports,
  type GenericDataPoint,
} from "@e2e/fixtures/common/DataPointFixtures";
import { type ReferencedDocuments, generateArray } from "@e2e/fixtures/FixtureUtils";
import { randomYesNo, randomYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { type YesNo, type YesNoNa } from "@clients/backend";
import { generateBaseDataPoint, type GenericBaseDataPoint } from "@e2e/fixtures/common/BaseDataPointFixtures";
import { randomNumber, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";

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

  constructor(undefinedProbability = DEFAULT_PROBABILITY) {
    this.undefinedProbability = undefinedProbability;
    this.reports = generateReferencedReports();
  }

  valueOrUndefined<T>(value: T): T | undefined {
    return Math.random() > this.undefinedProbability ? value : undefined;
  }
  getReports(): ReferencedDocuments {
    return this.reports;
  }
  setReports(reports: ReferencedDocuments): void {
    this.reports = reports;
  }
  randomYesNo(): YesNo | undefined {
    return this.valueOrUndefined(randomYesNo());
  }
  randomYesNoNa(): YesNoNa | undefined {
    return this.valueOrUndefined(randomYesNoNa());
  }
  randomPercentageValue(): number | undefined {
    return this.valueOrUndefined(randomPercentageValue());
  }
  randomNumber(max = 10000): number | undefined {
    return this.valueOrUndefined(randomNumber(max));
  }
  randomBaseDataPoint<T>(input: T): GenericBaseDataPoint<T> | undefined {
    return this.valueOrUndefined(generateBaseDataPoint(input, this.undefinedProbability));
  }
  randomDataPoint<T>(input: T): GenericDataPoint<T> | undefined {
    return this.valueOrUndefined(generateDatapoint(input, this.reports));
  }
  generateArray<T>(generator: () => T): T[] | undefined {
    return this.valueOrUndefined(generateArray(generator));
  }
}
