const msPerDay = 86400000;

export function calculateDaysFromNow(dateInMilliseconds: number, startDate = new Date().getTime()): number {
  const d = new Date(dateInMilliseconds).getTime();
  return Math.ceil((d - startDate) / msPerDay);
}

export function formatExpiryDate(expireTimeDays: number, startDate = Date.now()): string {
  return new Date(startDate + expireTimeDays * msPerDay).toDateString();
}
