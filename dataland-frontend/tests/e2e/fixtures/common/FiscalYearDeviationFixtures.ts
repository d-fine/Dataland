import { FiscalYearDeviation } from '@clients/backend';
import { pickOneElement } from '@e2e/fixtures/FixtureUtils';

/**
 * Generates a random fiscal year deviation value
 * @returns a random fiscal year deviation value
 */
export function generateFiscalYearDeviation(): FiscalYearDeviation {
  return pickOneElement(Object.values(FiscalYearDeviation));
}
