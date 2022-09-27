import { faker } from "@faker-js/faker";

export function randomDate() {
  return faker.date.past().toISOString().split("T")[0];
}

export function randomDateOrUndefined(): string | undefined {
  return faker.datatype.boolean() ? randomDate() : undefined;
}
