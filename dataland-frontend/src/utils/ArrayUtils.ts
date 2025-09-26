/**
 * Checks if two arrays have the same unique values
 * @param a the first array
 * @param b the second array
 * @returns true iff the two arrays contain the same unique values
 */
export function arraySetEquals<T>(a: Array<T>, b: Array<T>): boolean {
  const aSet = new Set(a);
  const bSet = new Set(b);
  if (aSet.size !== bSet.size) return false;
  return [...aSet].every((element) => bSet.has(element));
}

/**
 * Generates an array of numbers from 0 to [numElements]-1
 * @param numElements the number of elements the array should hold
 * @returns the generated array
 */
export function range(numElements: number): number[] {
  return Array.from(new Array(numElements).keys());
}

/**
 * Takes an iterable of items and groups them by a key specified in a callback.
 * Note: This is an implementation of the Object.groupBy() method available in ES2024.
 * Currently, we use an older TypeScript version and if we change to a newer one in the future, the native
 * implementation should be used instead.
 * @param items the iterable containing the original items
 * @param callbackFn the callback function that generates the key from an item
 * @returns a map of arrays of items grouped by the key
 */
export function groupBy<T, K>(items: Iterable<T>, callbackFn: (item: T) => K): Map<K, T[]> {
  const returnValue = new Map<K, T[]>();
  for (const item of Array.from(items)) {
    const key = callbackFn(item);
    if (returnValue.has(key)) {
      returnValue.get(key)!.push(item);
    } else {
      returnValue.set(key, [item]);
    }
  }
  return returnValue;
}
