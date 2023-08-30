import { faker } from "@faker-js/faker";

const percentagePrecision = 0.0001;
const maxEuro = 1000000;
const minEuro = 50000;

/**
 * Generates a random decimal value in [min, max]
 * @param min the minimum allowed value (inclusive)
 * @param max the maximum allowed value (inclusive)
 * @returns a random number in [min, max]
 */
export function randomEuroValue(min: number = minEuro, max: number = maxEuro): number {
  // TODO Emanuel not a good name, since this is not only used for euros
  return faker.number.float({ min: min, max: max, precision: 1 });
}

/**
 * Generates a random number in [0, max]
 * @param max the maximum allowed value (inclusive)
 * @returns a random number in [0, max]
 */
export function randomNumber(max: number): number {
  return faker.number.int(max);
}

/**
 * Generates a random decimal value between 0 and 1
 * @returns a random float between 0 and 1
 */
export function randomPercentageValue(): number {
  return faker.number.float({
    min: 0,
    max: 1,
    precision: percentagePrecision,
  });
}
