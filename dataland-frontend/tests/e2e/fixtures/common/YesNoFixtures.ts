import { faker } from "@faker-js/faker";
import { YesNo, YesNoNa } from "@clients/backend";

/**
 * Randomly returns Yes or No
 * @returns Yes or No
 */
export function randomYesNo(): YesNo {
  return faker.helpers.arrayElement(Object.values(YesNo));
}

/**
 * Randomly returns Yes, No or Na
 *
 * @returns Yes, No or Na
 */
export function randomYesNoNa(): YesNoNa {
  return faker.helpers.arrayElement(Object.values(YesNoNa));
}

const possibleYesNoNaUndefinedValues = [undefined, ...Object.values(YesNoNa)];

/**
 * Randomly returns Yes, No, N/A or undefined
 * @returns Yes, No, N/A or undefined
 */
export function randomYesNoNaUndefined(): YesNoNa | undefined {
  return faker.helpers.arrayElement(possibleYesNoNaUndefinedValues);
}
