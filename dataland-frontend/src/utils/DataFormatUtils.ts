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
 * @param endDateInMilliseconds the unix time in milliseconds to
 * @returns the number of days until endDateInMilliseconds (rounding up)
 */
export function calculateDaysFromNow(endDateInMilliseconds: number): number {
  const currentUtcDateInMilliseconds = new Date().getTime();
  const daysFromNow = (endDateInMilliseconds - currentUtcDateInMilliseconds) / msPerDay;
  return Math.ceil(daysFromNow);
}

/**
 * Transforms the given unix time in milliseconds to a date string
 * @param unixTimeInMs the unix time in milliseconds
 * @returns a date string representing the given unix time
 */
export function convertUnixTimeInMsToDateString(unixTimeInMs: number): string {
  return new Date(unixTimeInMs).toLocaleDateString("en-gb", dateFormatOptions);
}

/**
 * Calculates an expiry date in the future based on the number of valid days from now
 * @param expiryTimeDays the time in days to move into the future
 * @returns the resulting expiry date in the future in the format of "Wed, 25 Jan 2023, 10:38"
 */
export function calculateExpiryDateAsDateString(expiryTimeDays: number): string {
  const currentUtcDateInMilliseconds = new Date().getTime();
  const expiryUtcDateInMilliseconds = currentUtcDateInMilliseconds + expiryTimeDays * msPerDay;
  return convertUnixTimeInMsToDateString(expiryUtcDateInMilliseconds);
}

/**
 * Computes a hyphenated string (yyyy-MM-dd) of a date.
 * Since the toISOString()-method takes the UTC-timeoffset of the user into account, that offset needs to be substracted
 * in the step before. This makes sure that the resulting Date object has the time 00:00 on the picked day again.
 * @param date the date to hyphenate
 * @returns the hyphenated date string
 */
export function getHyphenatedDate(date: Date): string {
  const timeZoneOffsetBetweenLocalAndUtcInMs = date.getTimezoneOffset() * 60 * 1000;
  const dateInEpochMsMinusTimezoneOffset = date.getTime() - timeZoneOffsetBetweenLocalAndUtcInMs;
  return new Date(dateInEpochMsMinusTimezoneOffset).toISOString().substring(0, 10);
}
