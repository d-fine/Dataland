/**
 * Randomly returns the specified value or undefined
 * @param value the value to return
 * @param undefinedProbability the probability (as number between 0 and 1) that the returned value is undefined
 * @returns the value or undefined
 */
export function valueOrUndefined<T>(value: T, undefinedProbability = 0.5): T | undefined {
  return Math.random() > undefinedProbability ? value : undefined;
}
