/**
 * Module to convert numbers for different use cases
 */

/**
 * round a number with a defined precision
 *
 * @param  {number} rawNumber is the number that needs to be rounded
 * @param  {number} precision is the number that defines how many digits should be considered after the decimal point
 */

export function roundNumber(rawNumber: number, precision: number): number {
  const multiplier = Math.pow(10, precision || 0);
  return Math.round(rawNumber * multiplier) / multiplier;
}

/**
 * convert bytes to a human-readable text
 *
 * @param  {number} bytes is the number of bytes
 * @param  {number} decimals is the max number of digits that should be displayed after the decimal point
 */
export function formatBytesUserFriendly(bytes: number, decimals: number): string {
  if (!+bytes) return "0 Bytes";
  const k = 1024;
  const sizes = ["Bytes", "KB", "MB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));

  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(decimals))} ${sizes[i]}`;
}
