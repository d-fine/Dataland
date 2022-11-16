import { faker } from "@faker-js/faker";
import { FiscalYearDeviation } from "@clients/backend";

const possibleFiscalYearDeviationUndefinedValues = [undefined, ...Object.values(FiscalYearDeviation)];
export function randomFiscalYearDeviationOrUndefined(): FiscalYearDeviation | undefined {
  return faker.helpers.arrayElement(possibleFiscalYearDeviationUndefinedValues);
}
