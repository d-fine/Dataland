import {
  generateDatapoint,
  generateReferencedReports,
  type GenericDataPoint,
  type GenericBaseDataPoint,
} from "@e2e/fixtures/common/DataPointFixtures";
import { type ReferencedDocuments, generateArray, pickOneElement } from "@e2e/fixtures/FixtureUtils";
import { generateYesNo, generateYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { type DocumentReference, type YesNo, type YesNoNa } from "@clients/backend";
import { generateCurrencyValue, generateInt, generatePercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { generateReferencedDocuments } from "@e2e/utils/DocumentReference";

export const DEFAULT_PROBABILITY = 0.2;

/**
 * Randomly returns the specified value or undefined
 * @param value the value to return
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in optional fields
 * @returns the value or undefined
 */
export function valueOrUndefined<T>(value: T, undefinedProbability = DEFAULT_PROBABILITY): T | undefined {
  return Math.random() > undefinedProbability ? value : undefined;
}

/**
 * Randomly returns the specified value or null
 * @param value the value to return
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns the value or null
 */
export function valueOrNull<T>(value: T, nullProbability = DEFAULT_PROBABILITY): T | null {
  return Math.random() > nullProbability ? value : null;
}

/**
 * Randomly returns the specified value or a missing value, which can be represented by "null" or "undefined".
 * @param value the value to (potentially) return
 * @param missingValueProbability the probability (as number between 0 and 1) for a missing value in optional fields
 * @param setMissingValuesToNull sets missing values to "null" if set to "true", else to "undefined"
 * @returns the value or null or undefined
 */
export function valueOrMissing<T>(
  value: T,
  missingValueProbability: number,
  setMissingValuesToNull = false,
): T | undefined | null {
  return setMissingValuesToNull
    ? valueOrNull(value, missingValueProbability)
    : valueOrUndefined(value, missingValueProbability);
}

export class Generator {
  missingValueProbability: number;
  setMissingValuesToNull: boolean;
  reports: ReferencedDocuments;
  documents: ReferencedDocuments;

  constructor(undefinedProbability = DEFAULT_PROBABILITY, setMissingValuesToNull = false) {
    this.missingValueProbability = undefinedProbability;
    this.setMissingValuesToNull = setMissingValuesToNull;
    this.reports = generateReferencedReports(undefinedProbability, setMissingValuesToNull);
    this.documents = generateReferencedDocuments();
  }

  valueOrMissing<T>(value: T): T | undefined | null {
    return this.setMissingValuesToNull
      ? valueOrNull(value, this.missingValueProbability)
      : valueOrUndefined(value, this.missingValueProbability);
  }

  missingValue(): undefined | null {
    return this.setMissingValuesToNull ? null : undefined;
  }

  randomYesNo(): YesNo | undefined | null {
    return this.valueOrMissing(generateYesNo());
  }

  randomYesNoNa(): YesNoNa | undefined | null {
    return this.valueOrMissing(generateYesNoNa());
  }

  randomPercentageValue(): number | undefined | null {
    return this.valueOrMissing(generatePercentageValue());
  }

  randomInt(max = 10000): number | undefined | null {
    return this.valueOrMissing(generateInt(max));
  }

  randomCurrencyValue(): number | undefined | null {
    return this.valueOrMissing(generateCurrencyValue());
  }

  randomBaseDataPoint<T>(input: T): GenericBaseDataPoint<T> | undefined | null {
    const randomDocument = this.valueOrMissing(
      pickOneElement(
        Object.values(this.documents)
          .filter((document) => "name" in document && "reference" in document)
          .map((document) => document as DocumentReference),
      ),
    );
    return this.valueOrMissing({ value: input, dataSource: randomDocument });
  }

  randomDataPoint<T>(input: T, unit?: string): GenericDataPoint<T> | undefined | null {
    const randomInput = this.valueOrMissing(input);
    return this.valueOrMissing(generateDatapoint(randomInput, this.reports, this.setMissingValuesToNull, unit));
  }

  randomArray<T>(generator: () => T, min = 0, max = 5): T[] | undefined | null {
    return this.valueOrMissing(generateArray(generator, min, max));
  }
}
