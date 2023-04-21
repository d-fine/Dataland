import { faker } from "@faker-js/faker";
import { FiscalYearDeviation } from "@clients/backend";

const possibleFiscalYearDeviationUndefinedValues = [undefined, ...Object.values(FiscalYearDeviation)];

/**
 * Generates a random fiscal year deviation or undefined
 * @returns a random fiscal year deviation or undefined
 */
export function randomFiscalYearDeviationOrUndefined(): FiscalYearDeviation | undefined {
  return faker.helpers.arrayElement(possibleFiscalYearDeviationUndefinedValues);
}
