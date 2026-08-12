import { type AmountWithCurrency } from '@clients/backend';
import { roundNumber } from '@/utils/NumberConversionUtils';

/**
 * Formats a percentage number by rounding it and afterward making it a string with a
 * percent symbol at the end.
 * @param percentageNumber is the percentage number to format
 * @param precision is the precision for the rounding of the percentage number
 * @returns the resulting string
 */
export function formatPercentageNumberAsString(percentageNumber?: number | null, precision = 2): string {
  if (percentageNumber == undefined) {
    return '';
  }
  return `${roundNumber(percentageNumber, precision)} %`;
}

/**
 * Formats an AmountWithCurrency object by concatenating the amount and the currency.
 *
 * The amount is defensively coerced to a number before formatting: values coming from a
 * single stored data point's JSON (e.g. a QA report's correctedData or a reviewer's
 * customValue) may arrive as a numeric string at runtime even though they are typed as
 * `number`, and `String.prototype.toLocaleString` silently ignores formatting options,
 * which would otherwise cause the thousands-grouping to be dropped.
 * @param amountWithCurrency the object that holds the amount and currency
 * @returns the resulting string from the concatenation
 */
export function formatAmountWithCurrency(amountWithCurrency: AmountWithCurrency | null | undefined): string {
  if (amountWithCurrency?.amount == undefined) {
    return '';
  }
  const amount = Number(amountWithCurrency.amount);
  if (Number.isNaN(amount)) {
    return '';
  }
  if (amount === 0) {
    return `0 ${(amountWithCurrency?.currency ?? '').trim()}`;
  }
  return `${amount.toLocaleString('en-GB', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}${amountWithCurrency.currency ? ' ' + amountWithCurrency.currency : ''}`;
}

/**
 * Formats number to be more readable.
 *
 * The value is defensively coerced to a number before formatting: values coming from a
 * single stored data point's JSON (e.g. a QA report's correctedData or a reviewer's
 * customValue) may arrive as a numeric string at runtime even though they are typed as
 * `number`, and `String.prototype.toLocaleString` silently ignores formatting options,
 * which would otherwise cause the thousands-grouping to be dropped.
 * @param value number to format
 * @returns formatted number (e.g. 1500600.0123 --> 1,500,600.01)
 */
export function formatNumberToReadableFormat(value: number | undefined | null): string {
  if (value == undefined) {
    return '';
  }
  const numericValue = Number(value);
  if (Number.isNaN(numericValue)) {
    return '';
  }
  if (numericValue == 0) {
    return '0';
  }
  return numericValue.toLocaleString('en-GB', {
    maximumFractionDigits: 2,
  });
}
