import { faker } from "@faker-js/faker";

/**
 * Returns either the input string or undefined with a 50/50 chance
 *
 * @param input the string to be returned
 * @returns either the input or undefined
 */
export function randomStringOrUndefined(input: string): string | undefined {
  return faker.helpers.arrayElement([undefined, input]);
}
