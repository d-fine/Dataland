import { faker } from "@faker-js/faker";
import { SmeData, SmeProduct, SmeProductionSite } from "@clients/backend";
import { randomYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { randomNumber, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { generateListOfNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateAddress } from "@e2e/fixtures/common/AddressFixtures";
import { randomFutureDate } from "@e2e/fixtures/common/DateFixtures";
import {
  getRandomHeatSource,
  getRandomSlectionOfNaturalHazards,
  getRandomPercentageRangeEnergyConsumption,
  getRandomPercentageRangeInvestmentEnergyEfficiency,
} from "@e2e/fixtures/sme/SmeEnumFixtures";
import { generateArray } from "@e2e/fixtures/FixtureUtils";

/**
 * Generates a random product
 * @returns a random product
 */
function generateProduct(): SmeProduct {
  return {
    name: faker.commerce.productName(),
    percentageOfTotalRevenue: valueOrUndefined(randomPercentageValue()),
  };
}

/**
 * Generates a random production site
 * @param undefinedProbability the percentage of undefined values in the returned production site
 * @returns a random production site
 */
export function generateProductionSite(undefinedProbability = 0.5): SmeProductionSite {
  // TODO do undefined values for nullable things too
  console.log(undefinedProbability); // TODO remove at the end
  return {
    nameOfProductionSite: faker.company.name(),
    addressOfProductionSite: generateAddress(),
    percentageOfTotalRevenue: valueOrUndefined(randomPercentageValue()),
  };
}

/**
 * Generates a random SME dataset
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random SME dataset
 */
export function generateSmeData(undefinedProbability = 0.5): SmeData {
  return {
    general: {
      basicInformation: {
        sector: generateListOfNaceCodes(),
        addressOfHeadquarters: generateAddress(),
        numberOfEmployees: randomNumber(10000),
        fiscalYearStart: randomFutureDate(),
      },
      businessNumbers: {
        revenueInEur: valueOrUndefined(randomNumber(100000000), undefinedProbability),
        operatingCostInEur: valueOrUndefined(randomNumber(80000000), undefinedProbability),
        capitalAssetsInEur: valueOrUndefined(randomNumber(70000000), undefinedProbability),
      },
    },
    production: {
      sites: {
        listOfProductionSites: valueOrUndefined(generateArray(generateProductionSite), undefinedProbability),
      },
      products: {
        listOfProducts: valueOrUndefined(generateArray(generateProduct), undefinedProbability),
      },
    },
    power: {
      investments: {
        percentageOfInvestmentsInEnhancingEnergyEfficiency: valueOrUndefined(
          getRandomPercentageRangeInvestmentEnergyEfficiency()
        ),
      },
      consumption: {
        powerConsumptionInMwh: valueOrUndefined(randomNumber(2000), undefinedProbability),
        powerFromRenewableSources: valueOrUndefined(randomYesNo(), undefinedProbability),
        energyConsumptionHeatingAndHotWater: valueOrUndefined(randomNumber(1000), undefinedProbability),
        primaryEnergySourceForHeatingAndHotWater: valueOrUndefined(getRandomHeatSource()),
        energyConsumptionCoveredByOwnRenewablePowerGeneration: valueOrUndefined(
          getRandomPercentageRangeEnergyConsumption()
        ),
      },
    },
    insurances: {
      naturalHazards: {
        insuranceAgainstNaturalHazards: valueOrUndefined(randomYesNo(), undefinedProbability),
        amountCoveredByInsuranceAgainstNaturalHazards: valueOrUndefined(randomNumber(50000000), undefinedProbability),
        naturalHazardsCovered: valueOrUndefined(getRandomSlectionOfNaturalHazards(), undefinedProbability),
      },
    },
  };
}
