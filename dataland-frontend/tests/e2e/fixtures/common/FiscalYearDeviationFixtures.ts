import { faker } from "@faker-js/faker";
import { FiscalYearDeviation } from "@clients/backend";

/**
 * Generates a random fiscal year deviation value
 * @returns a random fiscal year deviation value
 */
export function generateFiscalYearDeviation(): FiscalYearDeviation {
  return faker.helpers.arrayElement(Object.values(FiscalYearDeviation));
}
