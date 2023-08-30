import { type AmountWithCurrency } from "@clients/backend";

/**
 * Formats a percentage number by rounding it to two decimals and afterward making it a string with a percent
 * symbol at the end.
 * @param relativeShareInPercent is the percentage number to round
 * @returns the resulting string
 */
export function formatPercentageNumber(relativeShareInPercent?: number): string {
  if (relativeShareInPercent == undefined) {
    return "";
  }
  return `${(relativeShareInPercent * 100).toFixed(2)} %`;
}

/**
 * Formats an AmountWithCurrency object by concatenating the amount and the currency.
 * @param amountWithCurrency the object that holds the amount and currency
 * @returns the resulting string from the concatenation
 */
export function formatAmountWithCurrency(amountWithCurrency: AmountWithCurrency): string {
  if (amountWithCurrency && amountWithCurrency.amount) {
    return `${Math.round(amountWithCurrency.amount).toString()} ${amountWithCurrency.currency ?? ""}`;
  }
  return "";
}
