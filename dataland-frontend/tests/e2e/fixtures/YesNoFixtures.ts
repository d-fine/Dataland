import { faker } from "@faker-js/faker";
import { YesNo } from "../../../build/clients/backend";

export function randomYesNo(): YesNo {
  return faker.datatype.boolean() ? YesNo.Yes : YesNo.No;
}
