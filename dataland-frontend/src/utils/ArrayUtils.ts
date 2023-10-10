/**
 * Checks if two arrays have the same unique values
 * @param a the first array
 * @param b the second array
 * @returns true iff the two arrays contain the same unique values
 */
export function arraySetEquals<T>(a: Array<T>, b: Array<T>): boolean {
  const aSet = new Set([...a]);
  const bSet = new Set([...b]);
  if (aSet.size !== bSet.size) return false;
  return [...aSet].every((element) => bSet.has(element));
}

/**
 * Generates an array of numbers from 0 to [numElements]-1
 * @param numElements the number of elements the array should hold
 * @returns the generated array
 */
export function range(numElements: number): number[] {
  return Array.from(Array(numElements).keys());
}
