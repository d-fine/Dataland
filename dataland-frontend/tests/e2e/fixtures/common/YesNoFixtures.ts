import { YesNo, YesNoNa } from '@clients/backend';
import { pickOneElement } from '@e2e/fixtures/FixtureUtils';

/**
 * Randomly returns Yes or No
 * @returns Yes or No
 */
export function generateYesNo(): YesNo {
  return pickOneElement(Object.values(YesNo));
}

/**
 * Randomly returns Yes, No or Na
 * @returns Yes, No or Na
 */
export function generateYesNoNa(): YesNoNa {
  return pickOneElement(Object.values(YesNoNa));
}
