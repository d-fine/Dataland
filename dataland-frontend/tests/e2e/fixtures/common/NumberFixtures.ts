import { faker } from "@faker-js/faker";

export function randomNumber() {
  return faker.datatype.number(1000);
}

export function randomNumberOrUndefined(): number | undefined {
  return faker.datatype.boolean() ? randomNumber() : undefined;
}
