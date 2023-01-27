/**
 * Format a number using the symbol and the value as break points.
 * adopted from https://stackoverflow.com/questions/9461621/format-a-number-as-2-5k-if-a-thousand-or-more-otherwise-900
 *
 * @param  {number} numberToConvert      is the number to be converted such as an amount
 * @param  {number} maxNumberOfDigitsAfterDecimalPoint      maximum number of digits to appear after the decimal point
 * @returns the formatted number
 */
export function convertCurrencyNumbersToNotationWithLetters(
  numberToConvert: number,
  maxNumberOfDigitsAfterDecimalPoint?: number
): string {
  const lookup = [
    { value: 1, symbol: "" },
    { value: 1e3, symbol: "K" },
    { value: 1e6, symbol: "M" },
    { value: 1e9, symbol: "B" },
    { value: 1e12, symbol: "T" },
    { value: 1e15, symbol: "QA" },
    { value: 1e18, symbol: "QI" },
  ];
  const regex = /\.0+$|(\.d*[1-9])0+$/;
  const item = lookup
    .slice()
    .reverse()
    .find((part): boolean => numberToConvert >= part.value);
  return item
    ? (numberToConvert / item.value).toFixed(maxNumberOfDigitsAfterDecimalPoint).replace(regex, "$1") +
        " " +
        item.symbol
    : "0";
}
