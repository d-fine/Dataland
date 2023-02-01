import { faker } from "@faker-js/faker";
import { YesNo, YesNoNa } from "@clients/backend";

const possibleYesNoUndefinedValues = [undefined, ...Object.values(YesNo)];
/**
 * Randomly returns Yes, No or undefined
 *
 * @returns Yes, No or undefined
 */
export function randomYesNoUndefined(): YesNo | undefined {
  return faker.helpers.arrayElement(possibleYesNoUndefinedValues);
}

const possibleYesNoNaUndefinedValues = [undefined, ...Object.values(YesNoNa)];

/**
 * Randomly returns Yes, No, N/A or undefined
 *
 * @returns Yes, No, N/A or undefined
 */
export function randomYesNoNaUndefined(): YesNoNa | undefined {
  return faker.helpers.arrayElement(possibleYesNoNaUndefinedValues);
}
