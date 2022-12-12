const msPerDay = 86400000;

export function calculateDaysFromNow(dateInMilliseconds: number, startDate = new Date().getTime()): number {
  const d = new Date(dateInMilliseconds).getTime();
  return Math.ceil((d - startDate) / msPerDay);
}

export function formatExpiryDate(expireTimeDays: number, startDate = Date.now()): string {
  const options = { weekday: "short", year: "numeric", month: "short", day: "numeric" } as Intl.DateTimeFormatOptions;
  return new Date(startDate + expireTimeDays * msPerDay).toLocaleDateString(undefined, options);
}
