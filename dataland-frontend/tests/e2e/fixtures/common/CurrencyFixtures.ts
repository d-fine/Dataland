import { faker } from '@faker-js/faker';

/**
 * Randomly returns a currency from "USD", "EUR", "CHF", "CAD" and "AUD"
 * @returns the randomly chosen currency code
 */
export function generateCurrencyCode(): string {
  return faker.finance.currencyCode();
}
