import { faker } from "@faker-js/faker";
import { FiscalYearDeviation } from "@clients/backend";

const possibleFiscalYearDeviationValues = [...Object.values(FiscalYearDeviation)];
const possibleFiscalYearDeviationUndefinedValues = [undefined, ...Object.values(FiscalYearDeviation)];

/**
 * Generates a random fiscal year deviation value
 * @returns a random fiscal year deviation value
 */
export function randomFiscalYearDeviation(): FiscalYearDeviation {
  return faker.helpers.arrayElement(possibleFiscalYearDeviationValues);
}

/**
 * Generates a random fiscal year deviation or undefined
 * @returns a random fiscal year deviation or undefined
 */
export function randomFiscalYearDeviationOrUndefined(): FiscalYearDeviation | undefined {
  return faker.helpers.arrayElement(possibleFiscalYearDeviationUndefinedValues);
}
