import {
  generateDatapoint,
  generateReferencedReports,
  type GenericDataPoint,
  type GenericBaseDataPoint,
} from "@e2e/fixtures/common/DataPointFixtures";
import { type ReferencedDocuments, generateArray } from "@e2e/fixtures/FixtureUtils";
import { randomYesNo, randomYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { type YesNo, type YesNoNa } from "@clients/backend";
import { randomNumber, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { generateReferencedDocuments } from "@e2e/utils/DocumentReference";
import { faker } from "@faker-js/faker";

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
    this.reports = generateReferencedReports();
    this.documents = generateReferencedDocuments();
  }

  valueOrUndefined<T>(value: T): T | undefined {
    return valueOrUndefined(value, this.undefinedProbability);
  }
  getReports(): ReferencedDocuments {
    return this.reports;
  }
  setReports(reports: ReferencedDocuments): void {
    this.reports = reports;
  }
  randomYesNo(): YesNo | undefined {
    return valueOrUndefined(randomYesNo(), this.undefinedProbability);
  }
  randomYesNoNa(): YesNoNa | undefined {
    return valueOrUndefined(randomYesNoNa(), this.undefinedProbability);
  }
  randomPercentageValue(): number | undefined {
    return valueOrUndefined(randomPercentageValue(), this.undefinedProbability);
  }
  randomNumber(max = 10000): number | undefined {
    return valueOrUndefined(randomNumber(max), this.undefinedProbability);
  }
  randomBaseDataPoint<T>(input: T): GenericBaseDataPoint<T> | undefined {
    const document = valueOrUndefined(
      faker.helpers.arrayElement(Object.values(this.documents)),
      this.undefinedProbability,
    );
    return valueOrUndefined({
      value: input,
      dataSource: document,
    } as GenericBaseDataPoint<T>);
  }
  randomDataPoint<T>(input: T): GenericDataPoint<T> | undefined {
    return valueOrUndefined(
      generateDatapoint(valueOrUndefined(input, this.undefinedProbability), this.reports),
      this.undefinedProbability,
    );
  }
  generateArray<T>(generator: () => T): T[] | undefined {
    return valueOrUndefined(generateArray(generator), this.undefinedProbability);
  }
}
