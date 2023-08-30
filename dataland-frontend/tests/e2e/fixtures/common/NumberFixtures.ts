import { faker } from "@faker-js/faker";

/**
 * Generates a random decimal value in [min, max]
 * @param min the minimum allowed value (inclusive)
 * @param max the maximum allowed value (inclusive)
 * @param precision the precision of the decimal value
 * @returns a random number in [min, max]
 */
export function randomFloat(min: number, max: number, precision?: number): number {
  return faker.number.float({ min: min, max: max, precision: precision });
}

/**
 * Generates a random decimal value between 0 and 1
 * @param precision
 * @returns a random float between 0 and 1
 */
export function randomPercentageValue(precision = 0.0001): number {
  return randomFloat(0, 1, precision);
}

/**
 * Generates a random number in [0, max]
 * @param max the maximum allowed value (inclusive)
 * @returns a random number in [0, max]
 */
export function randomInt(max: number): number {
  return faker.number.int(max);
}
