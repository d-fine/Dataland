import { faker } from "@faker-js/faker";
import { SmeData } from "@clients/backend";

import {
  randomIndustryOrUndefined,
  randomCompanyAgeBracketOrUndefined,
  randomEnergyEfficiencyBracketOrUndefined,
  randomEnergyProductionBracketOrUndefined,
  randomHeatSourceOrUndefined,
} from "./SmeEnumFixtures";
import { randomYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a set number of SME fixtures
 * @param numFixtures the number of SME fixtures to generate
 * @returns a set number of P2P fixtures
 */
export function generateSmeFixtures(numFixtures: number): FixtureData<SmeData>[] {
  return generateFixtureDataset<SmeData>(
    () => generateSmeData(),
    numFixtures,
    (dataSet: SmeData) => String(dataSet.financialYear)
  );
}

/**
 * Generates a random SME dataset
 * @returns a random SME dataset
 */
export function generateSmeData(): SmeData {
  const returnBase: SmeData = {};

  returnBase.industry = randomIndustryOrUndefined();
  returnBase.financialYear = faker.number.int({ min: 2010, max: 2022 });
  returnBase.totalRevenue = randomEuroValue();
  returnBase.researchAndDevelopmentExpenses = randomEuroValue();
  returnBase.energyEfficiencyInvestments = randomEnergyEfficiencyBracketOrUndefined();
  returnBase.operatingCosts = randomEuroValue();
  returnBase.totalAssets = randomEuroValue();
  returnBase.yearsSinceFounded = randomCompanyAgeBracketOrUndefined();
  returnBase.productCategoryWithHighestSales = faker.company.buzzNoun();
  returnBase.productCategoryWithHighestSalesShareOfSales = randomPercentageValue();
  returnBase.productCategoryWithSecondHighestSales = faker.company.buzzNoun();
  returnBase.productCategoryWithSecondHighestSalesShareOfSales = randomPercentageValue();
  returnBase.totalAreaCompany = faker.number.int();
  returnBase.totalPowerConsumption = faker.number.int();
  returnBase.totalPowerCosts = randomEuroValue();
  returnBase.useOfGreenElectricity = valueOrUndefined(randomYesNoNa());
  returnBase.heatingEnergyConsumption = faker.number.int();
  returnBase.totalHeatingCosts = randomEuroValue();
  returnBase.roomWaterHeating = randomHeatSourceOrUndefined();
  returnBase.shareOwnEnergyProduction = randomEnergyProductionBracketOrUndefined();
  returnBase.waterSewageCosts = randomEuroValue();
  returnBase.wasteDisposalCosts = randomEuroValue();
  returnBase.wasteRecyclingRate = randomPercentageValue();
  returnBase.numberOfEmployees = faker.number.int({ min: 1, max: 1000 });
  returnBase.numberOfTemporaryWorkers = faker.number.int({ min: 1, max: 1000 });
  returnBase.shareOfFullTimeEmployees = randomPercentageValue();
  returnBase.shareOfEmployeesSubjectToSocialSecurityContributions = randomPercentageValue();
  returnBase.employeeFluctuation = randomPercentageValue();
  returnBase.shareOfFemaleEmployees = randomPercentageValue();
  returnBase.proportionOfFemaleEmployeesInManagement = randomPercentageValue();
  returnBase.oshMeasures = valueOrUndefined(randomYesNoNa());
  returnBase.healthAndOldAgeOffers = valueOrUndefined(randomYesNoNa());
  returnBase.numberVacationDays = faker.number.int({ min: 1, max: 50 });
  returnBase.nonProfitProjects = valueOrUndefined(randomYesNoNa());
  return returnBase;
}
