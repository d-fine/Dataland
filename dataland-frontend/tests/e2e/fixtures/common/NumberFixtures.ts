import { faker } from '@faker-js/faker';

/**
 * Generates a random decimal value in [min, max]
 * @param min the minimum allowed value (inclusive)
 * @param max the maximum allowed value (inclusive)
 * @param precision the precision of the decimal value
 * @returns a random number in [min, max]
 */
export function generateFloat(min = 0, max = 1e5, precision = 1e-2): number {
  return faker.number.float({ min: min, max: max, multipleOf: precision });
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
 * Generates a random percentage value, by default in the standard range [0,100]
 * @param min is the minimum value
 * @param max is the maximum value
 * @returns a random percentage value generated according to the specifications
 */
export function generatePercentageValue(min = 0, max = 100): number {
  return generateFloat(min, max);
}

/**
 * Generates a random number in [0, max] or [-max, max] if desired
 * @param max the maximum allowed value (inclusive)
 * @param negative boolean that indicates whether negative outputs are also permitted
 * @returns a random number in [0, max], or between [-max, max] if desired
 */
export function generateInt(max = 10000, negative = false): number {
  const integer = faker.number.int(max);
  if (negative) {
    return faker.datatype.boolean() ? -integer : integer;
  } else {
    return integer;
  }
}

/**
 * Generates a random boolean
 * @returns a random boolean
 */
export function generateBoolean(): boolean {
  return faker.datatype.boolean();
}
