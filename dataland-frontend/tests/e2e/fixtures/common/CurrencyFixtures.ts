/**
 * Randomly returns a currency from "USD", "EUR", "CHF", "CAD" and "AUD"
 *
 * @returns the randomly chosen currency code
 */
export function generateIso4217CurrencyCode(): string {
  const someCommonIso4217CurrencyCodes = ["USD", "EUR", "CHF", "CAD", "AUD"];
  return someCommonIso4217CurrencyCodes[Math.floor(Math.random() * someCommonIso4217CurrencyCodes.length)];
}
