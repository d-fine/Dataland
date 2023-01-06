const msPerDay = 86400000;
const today = new Date().getTime();
export const dateFormatOptions = {
  weekday: "short",
  year: "numeric",
  month: "short",
  day: "numeric",
  hour: "2-digit",
  minute: "2-digit",
} as Intl.DateTimeFormatOptions;

export function calculateDaysFromNow(endDateInMilliseconds: number): number {
  const daysFromNow = (endDateInMilliseconds - today) / msPerDay;
  return Math.ceil(daysFromNow);
}

export function formatExpiryDate(expireTimeDays: number): string {
  console.log("expire time days", expireTimeDays);
  return new Date(today + expireTimeDays * msPerDay).toLocaleDateString(undefined, dateFormatOptions);
}
