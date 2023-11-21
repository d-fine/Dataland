import { generateArray } from "@e2e/fixtures/FixtureUtils";
import { generateYesNo, generateYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { type YesNo, type YesNoNa } from "@clients/backend";
import {
  generateCurrencyValue,
  generateFloat,
  generateInt,
  generatePercentageValue,
} from "@e2e/fixtures/common/NumberFixtures";
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
  constructor(nullProbability = DEFAULT_PROBABILITY) {
    this.nullProbability = nullProbability;
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
  randomFloat(min: number = 0, max: number = 1e5): number | null {
    return this.valueOrNull(generateFloat(min, max));
  }
  randomCurrencyValue(): number | null {
    return this.valueOrNull(generateCurrencyValue());
  }
  randomArray<T>(generator: () => T, min = 0, max = 5): T[] | null {
    return this.valueOrNull(generateArray(generator, min, max));
  }
}
