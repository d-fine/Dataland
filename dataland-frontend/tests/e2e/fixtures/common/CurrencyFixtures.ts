import { faker } from '@faker-js/faker';
import currencyCodes from '@/generated/currencyCodes.json';

/**
 * Randomly returns an ISO 4217 currency code from the shared currency code list
 * @returns the randomly chosen currency code
 */
export function generateCurrencyCode(): string {
  return faker.helpers.arrayElement(currencyCodes).code;
}
