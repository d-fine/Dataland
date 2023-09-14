import { faker } from "@faker-js/faker";

/**
 * Generates a random list of Nace codes (unique and sorted)
 * @param min the minimal number of elements in the returned array (defaults to 0 if not provided)
 * @returns random list of Nace codes
 */
export function generateListOfNaceCodes(min = 0): string[] {
  const values = Array.from({ length: faker.number.int({ min: min, max: 5 }) }, () => {
    return faker.helpers.arrayElement(["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"]);
  }).sort((a, b) => a.localeCompare(b));
  return [...new Set(values)];
}
