import { faker } from "@faker-js/faker";

/**
 * Generates a random list of Nace codes (unique and sorted)
 * @returns random list of Nace codes
 */
export function generateListOfNaceCodes(): string[] {
  const values = Array.from({ length: faker.number.int({ min: 0, max: 5 }) }, () => {
    return faker.helpers.arrayElement(["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"]);
  }).sort((a, b) => a.localeCompare(b));
  return [...new Set(values)];
}
