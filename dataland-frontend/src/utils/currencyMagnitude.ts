/**
 * Format a number using the symbol and the value as break points.
 * adopted from https://stackoverflow.com/questions/9461621/format-a-number-as-2-5k-if-a-thousand-or-more-otherwise-900
 *
 * @param  {number} number      is the number to be converted such as an amount
 * @param  {number} digits      are the decimal places to be displayed
 */

export function numberFormatter(number: number, digits?: number) {
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
    .find(function (part) {
      return number >= part.value;
    });
  return item ? (number / item.value).toFixed(digits).replace(regex, "$1") + " " + item.symbol : "0";
}
