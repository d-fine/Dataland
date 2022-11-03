import { faker } from "@faker-js/faker";
import { SmeData } from "@clients/backend";

import {
  randomBranchOrUndefined,
  randomCompanyAgeBracketOrUndefined,
  randomEnergyEfficiencyBracketOrUndefined,
  randomEnergyProductionBracketOrUndefined,
  randomHeatSourceOrUndefined,
  randomLegalFormOrUndefined,
} from "./SmeEnumFixtures";
import { randomYesNoUndefined } from "../common/YesNoFixtures";

export function generateSmeData(): SmeData {
  const returnBase: SmeData = {};

  returnBase.companyName = faker.company.name();
  returnBase.companyAge = randomCompanyAgeBracketOrUndefined();
  returnBase.companyLegalForm = randomLegalFormOrUndefined();
  returnBase.shareOfInvestmentsForEnergyEfficiency = randomEnergyEfficiencyBracketOrUndefined();
  returnBase.businessYear = faker.datatype.number({ min: 2015, max: 2022 });
  returnBase.branch = randomBranchOrUndefined();
  returnBase.renewableEnergy = randomYesNoUndefined();
  returnBase.heatSource = randomHeatSourceOrUndefined();
  returnBase.shareOfSelfProducedEnergy = randomEnergyProductionBracketOrUndefined();
  returnBase.numberOfEmployees = faker.datatype.number();
  returnBase.revenue = faker.datatype.number();
  returnBase.electricityConsumption = faker.datatype.number();
  returnBase.workerProtectionMeasures = randomYesNoUndefined();

  return returnBase;
}
