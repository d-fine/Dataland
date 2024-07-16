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
 * @param amountWithCurrency the object that holds the amount and currency
 * @returns the resulting string from the concatenation
 */
export function formatAmountWithCurrency(amountWithCurrency: AmountWithCurrency | null | undefined): string {
  if (amountWithCurrency?.amount == undefined) {
    return '';
  }
  if (amountWithCurrency?.amount === 0) {
    return `0 ${(amountWithCurrency?.currency ?? '').trim()}`;
  }
  return `${amountWithCurrency.amount.toLocaleString('en-GB', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}${amountWithCurrency.currency ? ' ' + amountWithCurrency.currency : ''}`;
}

/**
 * Formats number to be more readable.
 * @param value number to format
 * @returns formatted number (e.g. 1500600.0123 --> 1,500,600.01)
 */
export function formatNumberToReadableFormat(value: number | undefined | null): string {
  if (value == undefined) {
    return '';
  }
  if (value == 0) {
    return '0';
  }
  return value.toLocaleString('en-GB', {
    maximumFractionDigits: 2,
  });
}
