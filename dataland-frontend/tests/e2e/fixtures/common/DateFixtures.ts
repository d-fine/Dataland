import { faker } from "@faker-js/faker";

export function randomPastDate(): string {
  return faker.date.past().toISOString().split("T")[0];
}

export function randomPastDateOrUndefined(): string | undefined {
  return faker.datatype.boolean() ? randomPastDate() : undefined;
}

export function randomFutureDate(): string {
  return faker.date.future().toISOString().split("T")[0];
}
