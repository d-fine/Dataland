import { faker } from "@faker-js/faker";

const percentagePrecision = 0.0001;
const maxEuro = 1000000;
const minEuro = 50000;

export function randomEuroValue(min: number = minEuro, max: number = maxEuro): number {
  return faker.datatype.float({ min: min, max: max });
}

export function randomNumber(max: number): number {
  return faker.datatype.number(max);
}

export function randomNumberOrUndefined(max: number): number | undefined {
  return faker.datatype.boolean() ? randomNumber(max) : undefined;
}

export function randomPercentageValue(): number {
  return faker.datatype.float({
    min: 0,
    max: 1,
    precision: percentagePrecision,
  });
}
