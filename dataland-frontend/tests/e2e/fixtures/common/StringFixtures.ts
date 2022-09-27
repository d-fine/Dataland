import { faker } from "@faker-js/faker";

export function randomString(length: number) {
  return faker.datatype.string(length);
}

export function randomStringOrUndefined(length: number): string | undefined {
  return faker.datatype.boolean() ? randomString(length) : undefined;
}
