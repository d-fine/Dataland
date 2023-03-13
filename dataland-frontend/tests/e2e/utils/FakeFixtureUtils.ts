/**
 * Returns either the input string or undefined with a 50/50 chance
 *
 * @param input the string to be returned
 * @param undefinedPercentage the probability that this function returns undefined. Defaults to 50%
 * @returns either the input or undefined
 */
export function randomStringOrUndefined(input: string, undefinedPercentage = 0.5): string | undefined {
  if (Math.random() < undefinedPercentage) return undefined;
  else return input;
}
