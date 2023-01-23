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
 * Given a unix time in milliseconds calculates how many days the day is in the future (rounding up)
 *
 * @param endDateInMilliseconds the unix time in milliseconds to
 * @returns the number of days until endDateInMilliseconds (rounding up)
 */
export function calculateDaysFromNow(endDateInMilliseconds: number): number {
  const currentUtcDateInMilliseconds = new Date().getTime();
  const daysFromNow = (endDateInMilliseconds - currentUtcDateInMilliseconds) / msPerDay;
  return Math.ceil(daysFromNow);
}

/**
 * Calculates a date expiryTimeDays in the future and returns it in the format like "Wed, 25 Jan 2023, 10:38"
 *
 * @param expiryTimeDays the time in days to move into the future
 * @returns the date expiryTimeDays in the future in the format of "Wed, 25 Jan 2023, 10:38"
 */
export function formatExpiryDate(expiryTimeDays: number): string {
  const currentUtcDateInMilliseconds = new Date().getTime();
  return new Date(currentUtcDateInMilliseconds + expiryTimeDays * msPerDay).toLocaleDateString(
    "en-gb",
    dateFormatOptions
  );
}
