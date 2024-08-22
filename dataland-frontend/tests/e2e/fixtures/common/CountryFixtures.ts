import { getAllCountryCodes } from '@/utils/CountryCodeConverter';
import { pickOneElement } from '@e2e/fixtures/FixtureUtils';

/**
 * Randomly returns one Iso2 country code from all available Iso2 country codes
 * @returns the randomly chosen country code
 */
export function generateIso2CountryCode(): string {
  return pickOneElement(getAllCountryCodes());
}
