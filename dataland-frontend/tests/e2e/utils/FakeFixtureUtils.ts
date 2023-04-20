/**
 * Returns either the input string or undefined with a 50/50 chance
 *
 * @param input the string to be returned
 * @param undefinedPercentage the probability that this function returns undefined. Defaults to 50%
 * @returns either the input or undefined
 */
export function randomStringOrUndefined(input: string, undefinedPercentage = 0.5): string | undefined {
  return Math.random() < undefinedPercentage ? undefined : input;
}

/**
 * Returns either a list consisting of only input string or undefined with a 50/50 chance
 *
 * @param input the string to be returned as single element in a list
 * @param undefinedPercentage the probability that this function returns undefined. Defaults to 50%
 * @returns either the list with input or undefined
 */
export function randomListOfStringOrUndefined(input: string, undefinedPercentage = 0.5): string[] | undefined {
  return Math.random() < undefinedPercentage ? undefined : [input];
}
