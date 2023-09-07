import { faker } from "@faker-js/faker";

/**
 * Generates a random list of Nace codes (unique and sorted)
 * @param minNumberOfNaceCodes minimum number of nace codes to generate
 * @param maxNumberOfNaceCodes maximum number of nace codes to generate
 * @returns random list of Nace codes
 */
export function generateListOfNaceCodes(minNumberOfNaceCodes = 0, maxNumberOfNaceCodes = 5): string[] {
  const values = Array.from(
    { length: faker.number.int({ min: minNumberOfNaceCodes, max: maxNumberOfNaceCodes }) },
    () => {
      return faker.helpers.arrayElement(["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"]);
    },
  ).sort((a, b) => a.localeCompare(b));
  return [...new Set(values)];
}
