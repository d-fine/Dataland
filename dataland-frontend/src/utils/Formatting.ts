/**
 * Formats a percentage number by rounding it to two decimals and afterward making it a string with a percent
 * symbol at the end.
 * @param relativeShareInPercent is the percentage number to round
 * @returns the resulting string
 */
export function formatPercentageNumber(relativeShareInPercent?: number) {
  if(relativeShareInPercent == undefined) {
    return "";
  }
  return `${(relativeShareInPercent * 100).toFixed(2)} %`;
}
