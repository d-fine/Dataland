import { faker } from "@faker-js/faker";

const percentagePrecision = 0.0001;
const maxEuro = 1000000;
const minEuro = 50000;

/**
 * Generates a random decimal value in [min, max]
 *
 * @param min the minimum allowed value (inclusive)
 * @param max the maximum allowed value (inclusive)
 * @returns a random number in [min, max]
 */
export function randomEuroValue(min: number = minEuro, max: number = maxEuro): number {
  return faker.datatype.float({ min: min, max: max });
}

/**
 * Generates a random number in [0, max]
 *
 * @param max the maximum allowed value (inclusive)
 * @returns a random number in [0, max]
 */
export function randomNumber(max: number): number {
  return faker.datatype.number(max);
}

/**
 * Generates a random decimal value between 0 and 1
 *
 * @returns a random float between 0 and 1
 */
export function randomPercentageValue(): number {
  return faker.datatype.float({
    min: 0,
    max: 1,
    precision: percentagePrecision,
  });
}
