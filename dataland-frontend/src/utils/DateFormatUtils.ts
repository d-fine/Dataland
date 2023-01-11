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
  return new Date(currentUtcDateInMilliseconds + expiryTimeDays * msPerDay).toLocaleDateString(
    "en-gb",
    dateFormatOptions
  );
}
