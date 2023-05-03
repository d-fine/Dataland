import { faker } from "@faker-js/faker";

/**
 * Generates a random date in the past of the format YYYY-MM-DD
 *
 * @returns a random date in the past of the format YYYY-MM-DD
 */
export function randomPastDate(): string {
  return faker.date.past().toISOString().split("T")[0];
}
/**
 * Generates a random date in the past of the format YYYY-MM-DD or undefined
 *
 * @returns a random date in the past of the format YYYY-MM-DD or undefined
 */
export function randomPastDateOrUndefined(): string | undefined {
  return faker.datatype.boolean() ? randomPastDate() : undefined;
}

/**
 * Generates a random date in the future of the format YYYY-MM-DD
 *
 * @returns a random date in the future of the format YYYY-MM-DD
 */
export function randomFutureDate(): string {
  return faker.date.future().toISOString().split("T")[0];
}
