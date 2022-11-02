import { faker } from "@faker-js/faker";
import { SmeData } from "@clients/backend";

import { randomBranchOrUndefined, randomCompanyAgeBracketOrUndefined } from "./SmeEnumFixtures";
import {randomYesNoUndefined} from "../common/YesNoFixtures";

export function generateSmeData(): SmeData {
  const returnBase: SmeData = {};

  returnBase.companyName = faker.company.name();
  returnBase.branch = randomBranchOrUndefined();
  returnBase.numberOfEmployees = faker.datatype.number();
  returnBase.businessYear = faker.datatype.number({ min: 2015, max: 2022 });
  returnBase.companyAge = randomCompanyAgeBracketOrUndefined();
  returnBase.electricityConsumption = faker.datatype.number();
  returnBase.revenue = faker.datatype.number();
  returnBase.workerProtectionMeasures = randomYesNoUndefined();

  return returnBase;
}
