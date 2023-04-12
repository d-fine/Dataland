import { faker } from "@faker-js/faker";
import { SmeData } from "@clients/backend";

import {
  randomCompanyAgeBracketOrUndefined,
  randomEnergyEfficiencyBracketOrUndefined,
  randomEnergyProductionBracketOrUndefined,
  randomHeatSourceOrUndefined,
  randomIndustryOrUndefined,
} from "./SmeEnumFixtures";
import { randomYesNoNaUndefined } from "@e2e/fixtures/common/YesNoFixtures";
import { randomEuroValue, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";

/**
 * Generates a random SME dataset
 *
 * @returns a random SME dataset
 */
export function generateSmeData(): Promise<SmeData> {
  return new Promise((resolve) => {
    const returnBase: SmeData = {};
    returnBase.industry = randomIndustryOrUndefined();
    returnBase.financialYear = faker.datatype.number({ min: 2010, max: 2022 });
    returnBase.totalRevenue = randomEuroValue();
    returnBase.researchAndDevelopmentExpenses = randomEuroValue();
    returnBase.energyEfficiencyInvestments = randomEnergyEfficiencyBracketOrUndefined();
    returnBase.operatingCosts = randomEuroValue();
    returnBase.totalAssets = randomEuroValue();
    returnBase.yearsSinceFounded = randomCompanyAgeBracketOrUndefined();
    returnBase.productCategoryWithHighestSales = faker.company.bsNoun();
    returnBase.productCategoryWithHighestSalesShareOfSales = randomPercentageValue();
    returnBase.productCategoryWithSecondHighestSales = faker.company.bsNoun();
    returnBase.productCategoryWithSecondHighestSalesShareOfSales = randomPercentageValue();
    returnBase.totalAreaCompany = faker.datatype.number();
    returnBase.totalPowerConsumption = faker.datatype.number();
    returnBase.totalPowerCosts = randomEuroValue();
    returnBase.useOfGreenElectricity = randomYesNoNaUndefined();
    returnBase.heatingEnergyConsumption = faker.datatype.number();
    returnBase.totalHeatingCosts = randomEuroValue();
    returnBase.roomWaterHeating = randomHeatSourceOrUndefined();
    returnBase.shareOwnEnergyProduction = randomEnergyProductionBracketOrUndefined();
    returnBase.waterSewageCosts = randomEuroValue();
    returnBase.wasteDisposalCosts = randomEuroValue();
    returnBase.wasteRecyclingRate = randomPercentageValue();
    returnBase.numberOfEmployees = faker.datatype.number({ min: 1, max: 1000 });
    returnBase.numberOfTemporaryWorkers = faker.datatype.number({ min: 1, max: 1000 });
    returnBase.shareOfFullTimeEmployees = randomPercentageValue();
    returnBase.shareOfEmployeesSubjectToSocialSecurityContributions = randomPercentageValue();
    returnBase.employeeFluctuation = randomPercentageValue();
    returnBase.shareOfFemaleEmployees = randomPercentageValue();
    returnBase.proportionOfFemaleEmployeesInManagement = randomPercentageValue();
    returnBase.oshMeasures = randomYesNoNaUndefined();
    returnBase.healthAndOldAgeOffers = randomYesNoNaUndefined();
    returnBase.numberVacationDays = faker.datatype.number({ min: 1, max: 50 });
    returnBase.nonProfitProjects = randomYesNoNaUndefined();
    resolve(returnBase);
  });
}
