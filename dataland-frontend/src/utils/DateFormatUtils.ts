const msPerDay = 86400000;
export const dateFormatOptions = {
  weekday: "short",
  year: "numeric",
  month: "short",
  day: "numeric",
  hour: "2-digit",
  minute: "2-digit",
} as Intl.DateTimeFormatOptions;

export function calculateDaysFromNow(endDateInMilliseconds: number): number {
  const currentUtcDateInMilliseconds = new Date().getTime();
  const daysFromNow = (endDateInMilliseconds - currentUtcDateInMilliseconds) / msPerDay;
  return Math.ceil(daysFromNow);
}

export function formatExpiryDate(expiryTimeDays: number): string {
  const currentUtcDateInMilliseconds = new Date().getTime();
  return formatDate(currentUtcDateInMilliseconds + expiryTimeDays * msPerDay, dateFormatOptions);
}

export function formatDate(dateInMilliseconds: number, dateFormatOptions: Intl.DateTimeFormatOptions): string {
  return new Date(dateInMilliseconds).toLocaleDateString("en-gb", dateFormatOptions);
}
