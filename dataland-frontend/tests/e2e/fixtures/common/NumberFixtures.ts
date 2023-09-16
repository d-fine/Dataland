import { faker } from "@faker-js/faker";

/**
 * Generates a random decimal value in [min, max]
 * @param min the minimum allowed value (inclusive)
 * @param max the maximum allowed value (inclusive)
 * @param precision the precision of the decimal value
 * @returns a random number in [min, max]
 */
export function generateFloat(min = 0, max = 1e5, precision = 1e-2): number {
  return faker.number.float({ min: min, max: max, precision: precision });
}

/**
 * Generates a random currency value in [min, max]
 * @param min the minimum allowed value (inclusive)
 * @param max the maximum allowed value (inclusive)
 * @param precision the precision of the decimal value
 * @returns a random number in [min, max]
 */
export function generateCurrencyValue(min = 0, max = 1e10, precision = 1e-2): number {
  return generateFloat(min, max, precision);
}

/**
 * Generates a random decimal value between 0 and 1
 * @param precision the precision of the decimal value
 * @returns a random float between 0 and 1
 */
export function generatePercentageValue(precision = 1e-4): number {
  return generateFloat(0, 1, precision);
}

/**
 * Generates a random number in [0, max]
 * @param max the maximum allowed value (inclusive)
 * @returns a random number in [0, max]
 */
export function generateInt(max: number): number {
  return faker.number.int(max);
}
