import { faker } from "@faker-js/faker";

const percentagePrecision = 0.0001;

export function randomNumber() {
  return faker.datatype.number(1000);
}

export function randomNumberOrUndefined(): number | undefined {
  return faker.datatype.boolean() ? randomNumber() : undefined;
}

export function randomPercentageValue(): number {
  return faker.datatype.float({
    min: 0,
    max: 1,
    precision: percentagePrecision,
  });
}
