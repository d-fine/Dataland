/**
 * Format Date
 */
// Calculate milliseconds in a year
const minute = 1000 * 60;
const hour = minute * 60;
const day = hour * 24;
// const year = day * 365;

export function calculateDaysFromNow(dateInMilliseconds: number, startDate = new Date().getTime()): number {
  const d = new Date(dateInMilliseconds).getTime();
  return Math.ceil((d - startDate) / day);
}
// Convert days to full date format
export function formatExpiryDate(expireTimeDays: number, startDate = Date.now()): string {
  // One day is 24h60min60s*1000ms = 86400000 ms
  const options = { weekday: "short", year: "numeric", month: "short", day: "numeric" } as Intl.DateTimeFormatOptions;
  return new Date(startDate + expireTimeDays * 86400000).toLocaleDateString(undefined, options);
}
