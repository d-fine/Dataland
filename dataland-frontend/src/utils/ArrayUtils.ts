/**
 * Checks if two arrays have the same unique values
 */
export function arraySetEquals<T>(a: Array<T>, b: Array<T>): boolean {
  const aSet = new Set([...a]);
  const bSet = new Set([...b]);
  if (aSet.size !== bSet.size) return false;
  return [...aSet].every((element) => bSet.has(element));
}
