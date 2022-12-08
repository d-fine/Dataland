/**
 * Format Date
 */
// Calculate milliseconds in a year
const minute = 1000 * 60;
const hour = minute * 60;
const day = hour * 24;
// const year = day * 365;

export function howManyDaysFromToday(dateInMilliseconds: number): number {
  const today = new Date().getTime();
  const d = new Date(dateInMilliseconds).getTime();
  return Math.ceil((d - today) / day);
}
// Converte days to full date format
export function expiryDateFormat(expireTimeDays: number): string {
  // One day is 24h60min60s*1000ms = 86400000 ms
  const options = { weekday: "short", year: "numeric", month: "short", day: "numeric" };
  return new Date(Date.now() + expireTimeDays * 86400000).toLocaleDateString(undefined, options);
}
