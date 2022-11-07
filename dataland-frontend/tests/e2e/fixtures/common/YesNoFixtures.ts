import { faker } from "@faker-js/faker";
import { YesNo, YesNoNa } from "@clients/backend";

const possibleYesNoUndefinedValues = [undefined, ...Object.values(YesNo)];
export function randomYesNoUndefined(): YesNo | undefined {
  return faker.helpers.arrayElement(possibleYesNoUndefinedValues);
}

const possibleYesNoNaUndefinedValues = [undefined, ...Object.values(YesNoNa)];
export function randomYesNoNaUndefined(): YesNoNa | undefined {
  return faker.helpers.arrayElement(possibleYesNoNaUndefinedValues);
}
