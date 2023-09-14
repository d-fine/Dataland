import { faker } from "@faker-js/faker";
import { type SmeData, type SmeProduct, type SmeProductionSite } from "@clients/backend";
import { randomNumber } from "@e2e/fixtures/common/NumberFixtures";
import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generateListOfNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateAddress } from "@e2e/fixtures/common/AddressFixtures";
import { randomFutureDate } from "@e2e/fixtures/common/DateFixtures";
import {
  getRandomHeatSource,
  getRandomSlectionOfNaturalHazards,
  getRandomPercentageRangeEnergyConsumption,
  getRandomPercentageRangeInvestmentEnergyEfficiency,
} from "@e2e/fixtures/sme/SmeEnumFixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";

/**
 * Generates a set number of SME fixtures
 * @param numFixtures the number of SME fixtures to generate
 * @returns a set number of SME fixtures
 */
export function generateSmeFixtures(numFixtures: number): FixtureData<SmeData>[] {
  return generateFixtureDataset<SmeData>(() => generateSmeData(), numFixtures);
}

/**
 * Generates a random SME dataset
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns a random SME dataset
 */
export function generateSmeData(undefinedProbability = DEFAULT_PROBABILITY): SmeData {
  const dataGenerator = new SmeGenerator(undefinedProbability);
  return {
    general: {
      basicInformation: {
        sector: generateListOfNaceCodes(1),
        addressOfHeadquarters: generateAddress(dataGenerator.undefinedProbability),
        numberOfEmployees: randomNumber(10000),
        fiscalYearStart: randomFutureDate(),
      },
      companyFinancials: {
        revenueInEur: dataGenerator.randomNumber(100000000),
        operatingCostInEur: dataGenerator.randomNumber(80000000),
        capitalAssetsInEur: dataGenerator.randomNumber(70000000),
      },
    },
    production: {
      sites: {
        listOfProductionSites: dataGenerator.generateProductionSite(),
      },
      products: {
        listOfProducts: dataGenerator.generateProduct(),
      },
    },
    power: {
      investments: {
        percentageOfInvestmentsInEnhancingEnergyEfficiency: dataGenerator.valueOrUndefined(
          getRandomPercentageRangeInvestmentEnergyEfficiency(),
        ),
      },
      consumption: {
        powerConsumptionInMwh: dataGenerator.valueOrUndefined(randomNumber(2000)),
        powerFromRenewableSources: dataGenerator.randomYesNo(),
        energyConsumptionHeatingAndHotWater: dataGenerator.randomNumber(1000),
        primaryEnergySourceForHeatingAndHotWater: dataGenerator.valueOrUndefined(getRandomHeatSource()),
        energyConsumptionCoveredByOwnRenewablePowerGeneration: dataGenerator.valueOrUndefined(
          getRandomPercentageRangeEnergyConsumption(),
        ),
      },
    },
    insurances: {
      naturalHazards: {
        insuranceAgainstNaturalHazards: dataGenerator.randomYesNo(),
        amountCoveredByInsuranceAgainstNaturalHazards: dataGenerator.randomNumber(50000000),
        naturalHazardsCovered: dataGenerator.valueOrUndefined(getRandomSlectionOfNaturalHazards()),
      },
    },
  };
}

class SmeGenerator extends Generator {
  /**
   * Generates a random product
   * @returns a random product
   */
  generateProduct(): SmeProduct[] | undefined {
    return this.randomArray(() => {
      return {
        name: faker.commerce.productName(),
        percentageOfTotalRevenue: this.randomPercentageValue(),
      } as SmeProduct;
    });
  }

  /**
   * Generates a random production site
   * @returns a random production site
   */
  generateProductionSite(): SmeProductionSite[] | undefined {
    return this.randomArray(() => {
      return {
        nameOfProductionSite: this.valueOrUndefined(faker.company.name()),
        addressOfProductionSite: generateAddress(this.undefinedProbability),
        percentageOfTotalRevenue: this.randomPercentageValue(),
      } as SmeProductionSite;
    });
  }
}
