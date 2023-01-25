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

export function convertUnixTimeInMsToDateString(unixTimeInMs: number): string {
  return new Date(unixTimeInMs).toLocaleDateString("en-gb", dateFormatOptions);
}

export function calculateExpiryDateAsDateString(expiryTimeDays: number): string {
  const currentUtcDateInMilliseconds = new Date().getTime();
  const expiryUtcDateInMilliseconds = currentUtcDateInMilliseconds + expiryTimeDays * msPerDay;
  return convertUnixTimeInMsToDateString(expiryUtcDateInMilliseconds);
}
