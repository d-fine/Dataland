const msPerDay = 86400000;
export const dateFormatOptions = {
  weekday: "short",
  year: "numeric",
  month: "short",
  day: "numeric",
  hour: "2-digit",
  minute: "2-digit",
} as Intl.DateTimeFormatOptions;

/**
 * Given a unix time in milliseconds calculates how many days the timestamp is in the future (rounding up)
 *
 * @param endDateInMilliseconds the unix time in milliseconds to
 * @returns the number of days until endDateInMilliseconds (rounding up)
 */
export function calculateDaysFromNow(endDateInMilliseconds: number): number {
  const currentUtcDateInMilliseconds = new Date().getTime();
  const daysFromNow = (endDateInMilliseconds - currentUtcDateInMilliseconds) / msPerDay;
  return Math.ceil(daysFromNow);
}

export function convertUnixTimeInMsToDateString(unixTimeInMs: number): string {
  return new Date(unixTimeInMs).toLocaleDateString("en-gb", dateFormatOptions);
}

/**
 * Calculates an expiry date in the future based on the number of valid days from now
 *
 * @param expiryTimeDays the time in days to move into the future
 * @returns the resulting expiry date in the future in the format of "Wed, 25 Jan 2023, 10:38"
 */
export function calculateExpiryDateAsDateString(expiryTimeDays: number): string {
  const currentUtcDateInMilliseconds = new Date().getTime();
  const expiryUtcDateInMilliseconds = currentUtcDateInMilliseconds + expiryTimeDays * msPerDay;
  return convertUnixTimeInMsToDateString(expiryUtcDateInMilliseconds);
}
