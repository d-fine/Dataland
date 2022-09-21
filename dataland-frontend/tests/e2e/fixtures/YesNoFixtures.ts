import { faker } from "@faker-js/faker";
import { YesNo, YesNoNa } from "../../../build/clients/backend";

export function randomYesNo(): YesNo {
  return faker.datatype.boolean() ? YesNo.Yes : YesNo.No;
}

const possiblerYesNoUndefinedValues = [undefined, ...Object.values(YesNo)];
export function randomYesNoUndefined(): YesNo | undefined {
  return faker.helpers.arrayElement(possiblerYesNoUndefinedValues);
}

const possibleYesNoNaUndefinedValues = [undefined, ...Object.values(YesNoNa)];

export function randomYesNoNaUndefined(): YesNoNa | undefined {
  return faker.helpers.arrayElement(possibleYesNoNaUndefinedValues);
}
