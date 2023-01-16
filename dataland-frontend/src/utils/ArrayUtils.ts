/**
 * Checks if two arrays have the same unique values
 *
 * @param a the first array
 * @param b the second array
 * @returns true iff the two arrays contain the same unique values
 */
export function arraySetEquals<T>(a: Array<T>, b: Array<T>): boolean {
  const aSet = new Set([...a]);
  const bSet = new Set([...b]);
  if (aSet.size !== bSet.size) return false;
  return [...aSet].every((it) => bSet.has(it));
}
