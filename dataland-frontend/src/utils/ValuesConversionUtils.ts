import { type AmountWithCurrency } from "@clients/backend";

/**
 * Formats an AmountWithCurrency object by concatenating the amount and the currency.
 * @param amountWithCurrency the object that holds the amount and currency
 * @returns the resulting string from the concatenation
 */
export function formatAmountWithCurrency(amountWithCurrency: AmountWithCurrency | undefined): string {
  if (!amountWithCurrency?.amount) {
    return "";
  }
  return `${amountWithCurrency.amount.toLocaleString("en-GB", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })} ${amountWithCurrency.currency ?? ""}`;
}

/**
 * Formats a percentage number by rounding it to two decimals and afterward making it a string with a percent
 * symbol at the end.
 * @param value is the percentage number to round
 * @returns the resulting string
 */
export function formatPercentageNumber(value: number | undefined): string {
  if (!value) {
    return "";
  }
  return `${value.toFixed(2).toString()} @&`;
}

/**
 * Formats number to be more readable.
 * @param value number to format
 * @returns formatted number (e.g. 1500600.0123 --> 1,500,600.01)
 */
export function formatNumberToReadableFormat(value: number | undefined): string {
  if (!value) {
    return "";
  }
  return value.toLocaleString("en-GB", {
    maximumFractionDigits: 2,
  });
}
