/**
 * Module to convert numbers for different use cases
 */

/**
 * round a number with a defined precision
 * @param  {number} rawNumber is the number that needs to be rounded
 * @param  {number} precision is the number that defines how many digits should be considered after the decimal point
 * @returns rawNumber rounded to precision digits
 */
export function roundNumber(rawNumber: number, precision: number): number {
  const multiplier = Math.pow(10, precision || 0);
  return Math.round(rawNumber * multiplier) / multiplier;
}

/**
 * convert the number of bytes for a file to a human-readable text
 * @param  {number} numberOfBytes is the number of bytes
 * @param  {number} precision is the number that defines how many digits should be considered after the decimal point
 * @returns a human-readable string of the number of bytes
 */
export function formatBytesUserFriendly(numberOfBytes: number, precision: number): string {
  if (!+numberOfBytes) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB'];
  const i = Math.floor(Math.log(numberOfBytes) / Math.log(k));

  return `${parseFloat((numberOfBytes / Math.pow(k, i)).toFixed(precision))} ${sizes[i]}`;
}

/**
 * Converts a number to millions with max two decimal places and adds "MM" at the end of the number.
 * @param inputNumber The number to convert
 * @returns a string with the converted number and "MM" at the end
 */
export function convertToMillions(inputNumber: number): string {
  return `${(inputNumber / 1000000).toLocaleString('en-GB', { maximumFractionDigits: 2 })} MM`;
}
