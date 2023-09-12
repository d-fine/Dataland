import {
  type P2pAmmonia,
  type P2pAutomotive,
  type P2pCement,
  type P2pElectricityGeneration,
  type P2pFreightTransportByRoad,
  type P2pGeneral,
  type P2pHvcPlastics,
  type P2pLivestockFarming,
  type P2pRealEstate,
  P2pSector,
  type P2pSteel,
  type PathwaysToParisData,
} from "@clients/backend";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { randomFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { faker } from "@faker-js/faker";
import { Generator } from "@e2e/utils/FakeFixtureUtils";
import { randomYesNo } from "@e2e/fixtures/common/YesNoFixtures";

/**
 * Generates a set number of P2P fixtures
 * @param numFixtures the number of P2P fixtures to generate
 * @param undefinedProbability the probability of fields to be undefined (number between 0 and 1)
 * @param toggleRandomSectors determines if the sector list should include all possible sectors or a randomized selection
 * @returns a set number of P2P fixtures
 */
export function generateP2pFixtures(
  numFixtures: number,
  undefinedProbability = 0.5,
  toggleRandomSectors = true,
): FixtureData<PathwaysToParisData>[] {
  return generateFixtureDataset<PathwaysToParisData>(
    () => generateP2pData(undefinedProbability, toggleRandomSectors),
    numFixtures,
    (dataSet: PathwaysToParisData) => dataSet.general.general.dataDate.substring(0, 4),
  );
}

/**
 * Generates random P2pAmmonia instance
 * @param dataGenerator instance of the Generator class to create fake fixture data
 * @returns a random P2pAmmonia
 */
export function getSectorAmmonia(dataGenerator: Generator): P2pAmmonia {
  return {
    decarbonisation: {
      energyMix: dataGenerator.randomPercentageValue(),
      ccsTechnologyAdoption: dataGenerator.randomPercentageValue(),
      electrification: dataGenerator.randomPercentageValue(),
    },
    defossilisation: {
      useOfRenewableFeedstocks: dataGenerator.randomPercentageValue(),
    },
  };
}

/**
 * Generates random P2pAutomotive instance
 * @param dataGenerator instance of the Generator class to create fake fixture data
 * @returns a random P2pAutomotive
 */
export function getSectorAutomotive(dataGenerator: Generator): P2pAutomotive {
  return {
    energy: {
      productionSiteEnergyConsumption: dataGenerator.randomNumber(),
      energyMix: dataGenerator.randomPercentageValue(),
    },
    technologyValueCreation: {
      driveMix: dataGenerator.randomPercentageValue(),
      icAndHybridEnginePhaseOutDate: dataGenerator.valueOrUndefined(randomFutureDate()),
      futureValueCreationStrategy: dataGenerator.randomYesNo(),
    },
    materials: {
      materialUseManagement: dataGenerator.randomPercentageValue(),
      useOfSecondaryMaterials: dataGenerator.randomPercentageValue(),
    },
  };
}

/**
 * Generates random P2pHvcPlastics instance
 * @param dataGenerator instance of the Generator class to create fake fixture data
 * @returns a random P2pHvcPlastics
 */
export function getSectorHVCPlastics(dataGenerator: Generator): P2pHvcPlastics {
  return {
    decarbonisation: {
      energyMix: dataGenerator.randomPercentageValue(),
      electrification: dataGenerator.randomPercentageValue(),
    },
    defossilisation: {
      useOfRenewableFeedstocks: dataGenerator.randomPercentageValue(),
      useOfBioplastics: dataGenerator.randomPercentageValue(),
      useOfCo2FromCarbonCaptureAndReUseTechnologies: dataGenerator.randomPercentageValue(),
      carbonCaptureAndUseStorageTechnologies: dataGenerator.randomPercentageValue(),
    },
    recycling: {
      contributionToCircularEconomy: dataGenerator.randomYesNo(),
      materialRecycling: dataGenerator.randomPercentageValue(),
      chemicalRecycling: dataGenerator.randomPercentageValue(),
    },
  };
}

/**
 * Generates random P2pRealEstate (commercial and residential) instance
 * @param dataGenerator instance of the Generator class to create fake fixture data
 * @returns a random P2pRealEstate
 */
export function getSectorRealEstate(dataGenerator: Generator): P2pRealEstate {
  return {
    buildingEfficiency: {
      buildingSpecificReburbishmentRoadmap: dataGenerator.randomPercentageValue(),
      zeroEmissionBuildingShare: dataGenerator.randomPercentageValue(),
      buildingEnergyEfficiency: dataGenerator.randomNumber(),
    },
    energySource: {
      renewableHeating: dataGenerator.randomPercentageValue(),
    },
    technology: {
      useOfDistrictHeatingNetworks: dataGenerator.randomYesNo(),
      heatPumpUsage: dataGenerator.randomYesNo(),
    },
  };
}

/**
 * Generates random P2pSteel instance
 * @param dataGenerator instance of the Generator class to create fake fixture data
 * @returns a random P2pSteel
 */
export function getSectorSteel(dataGenerator: Generator): P2pSteel {
  return {
    energy: {
      emissionIntensityOfElectricity: dataGenerator.randomNumber(),
      greenHydrogenUsage: dataGenerator.randomYesNo(),
    },
    technology: {
      blastFurnacePhaseOut: dataGenerator.randomPercentageValue(),
      lowCarbonSteelScaleUp: dataGenerator.randomPercentageValue(),
    },
  };
}

/**
 * Generates random P2pFreightTransportByRoad instance
 * @param dataGenerator instance of the Generator class to create fake fixture data
 * @returns a random P2pFreightTransportByRoad
 */
export function getSectorFreightTransportByRoad(dataGenerator: Generator): P2pFreightTransportByRoad {
  return {
    technology: {
      driveMix: dataGenerator.randomPercentageValue(),
      icePhaseOut: dataGenerator.valueOrUndefined(randomFutureDate()),
    },
    energy: {
      fuelMix: dataGenerator.randomPercentageValue(),
    },
  };
}

/**
 * Generates random P2pElectricityGeneration instance
 * @param dataGenerator instance of the Generator class to create fake fixture data
 * @returns a random P2pElectricityGeneration
 */
export function getSectorElectricityGeneration(dataGenerator: Generator): P2pElectricityGeneration {
  return {
    technology: {
      electricityMixEmissions: dataGenerator.randomNumber(),
      shareOfRenewableElectricity: dataGenerator.randomPercentageValue(),
      naturalGasPhaseOut: dataGenerator.valueOrUndefined(randomFutureDate()),
      coalPhaseOut: dataGenerator.valueOrUndefined(randomFutureDate()),
      storageCapacityExpansion: dataGenerator.randomPercentageValue(),
    },
  };
}

/**
 * Generates random P2pLivestockFarming instance
 * @param dataGenerator instance of the Generator class to create fake fixture data
 * @returns a random P2pLivestockFarming
 */
export function getSectorLivestockFarming(dataGenerator: Generator): P2pLivestockFarming {
  return {
    emissionsFromManureAndFertiliserAndLivestock: {
      compostedFermentedManure: dataGenerator.randomPercentageValue(),
      emissionProofFertiliserStorage: dataGenerator.randomPercentageValue(),
    },
    animalWelfare: {
      mortalityRate: dataGenerator.randomPercentageValue(),
    },
    animalFeed: {
      ownFeedPercentage: dataGenerator.randomPercentageValue(),
      externalFeedCertification: dataGenerator.randomBaseDataPoint(randomYesNo()),
      originOfExternalFeed: faker.company.buzzPhrase(),
      excessNitrogen: dataGenerator.randomNumber(),
      cropRotation: dataGenerator.randomNumber(),
      climateFriendlyProteinProduction: dataGenerator.randomPercentageValue(),
      greenFodderPercentage: dataGenerator.randomPercentageValue(),
    },
    energy: {
      renewableElectricityPercentage: dataGenerator.randomPercentageValue(),
      renewableHeatingPercentage: dataGenerator.randomPercentageValue(),
      electricGasPoweredMachineryVehiclePercentage: dataGenerator.randomPercentageValue(),
    },
  };
}

/**
 * Generates random P2pCement instance
 * @param dataGenerator instance of the Generator class to create fake fixture data
 * @returns a random P2pCement
 */
export function getSectorCement(dataGenerator: Generator): P2pCement {
  return {
    energy: {
      energyMix: dataGenerator.randomPercentageValue(),
      fuelMix: dataGenerator.randomPercentageValue(),
      thermalEnergyEfficiency: dataGenerator.randomPercentageValue(),
      compositionOfThermalInput: dataGenerator.randomPercentageValue(),
    },
    technology: {
      carbonCaptureAndUseTechnologyUsage: dataGenerator.randomYesNo(),
      electrificationOfProcessHeat: dataGenerator.randomPercentageValue(),
    },
    material: {
      clinkerFactorReduction: dataGenerator.randomNumber(),
      preCalcinedClayUsage: dataGenerator.randomPercentageValue(),
      circularEconomyContribution: dataGenerator.randomYesNo(),
    },
  };
}

/**
 * Generates random general information for the P2P Framework
 * @param dataGenerator instance of the Generator class to create fake fixture data
 * @param sectors Sectors of the company
 * @returns a random P2pGeneral instance with the given sectors
 */
export function getSectorGeneral(dataGenerator: Generator, sectors: Array<P2pSector>): P2pGeneral {
  return {
    general: {
      dataDate: randomFutureDate(),
      sectors: sectors,
    },
    governance: {
      organisationalResponsibilityForParisCompatibility: dataGenerator.randomYesNo(),
      parisCompatibilityInExecutiveRemuneration: dataGenerator.randomPercentageValue(),
      parisCompatibilityInAverageRemuneration: dataGenerator.randomPercentageValue(),
      shareOfEmployeesTrainedOnParisCompatibility: dataGenerator.randomPercentageValue(),
      qualificationRequirementsOnParisCompatibility: dataGenerator.randomYesNo(),
      mobilityAndTravelPolicy: dataGenerator.randomYesNo(),
      upstreamSupplierEngagementStrategy: dataGenerator.randomYesNo(),
      upstreamSupplierProcurementPolicy: dataGenerator.randomYesNo(),
      downstreamCustomerEngagement: dataGenerator.randomYesNo(),
      policymakerEngagement: dataGenerator.randomYesNo(),
    },
    climateTargets: {
      shortTermScienceBasedClimateTarget: dataGenerator.randomYesNo(),
      longTermScienceBasedClimateTarget: dataGenerator.randomYesNo(),
    },
    emissionsPlanning: {
      reductionOfAbsoluteEmissions: dataGenerator.randomPercentageValue(),
      reductionOfRelativeEmissions: dataGenerator.randomNumber(),
      absoluteEmissions: dataGenerator.randomNumber(),
      relativeEmissions: dataGenerator.randomNumber(),
      climateActionPlan: dataGenerator.randomYesNo(),
      useOfInternalCarbonPrice: dataGenerator.randomYesNo(),
    },
    investmentPlanning: {
      investmentPlanForClimateTargets: dataGenerator.randomYesNo(),
      capexShareInNetZeroSolutions: dataGenerator.randomPercentageValue(),
      capexShareInGhgIntensivePlants: dataGenerator.randomPercentageValue(),
      researchAndDevelopmentExpenditureForNetZeroSolutions: dataGenerator.randomPercentageValue(),
    },
  };
}

/**
 * Method to generate the sectors for a p2p dataset
 * @param toggleRandomSectors determines if the sector list should include all possible sectors or a randomized selection
 * @returns the sectors used for generating fixtures
 */
function generateSectors(toggleRandomSectors: boolean): P2pSector[] {
  if (toggleRandomSectors) {
    return faker.helpers.arrayElements(Object.values(P2pSector));
  } else {
    return Object.values(P2pSector);
  }
}
/**
 * Generates a random P2P dataset
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @param toggleRandomSectors determines if the sector list should include all possible sectors or a randomized selection
 * @returns a random P2P dataset
 */
export function generateP2pData(undefinedProbability = 0.5, toggleRandomSectors = true): PathwaysToParisData {
  const dataGenerator = new Generator(undefinedProbability);
  const inputSectors = generateSectors(toggleRandomSectors);
  return {
    general: getSectorGeneral(dataGenerator, inputSectors),
    ammonia: inputSectors.indexOf("Ammonia") != -1 ? getSectorAmmonia(dataGenerator) : undefined,
    automotive: inputSectors.indexOf("Automotive") != -1 ? getSectorAutomotive(dataGenerator) : undefined,
    hvcPlastics: inputSectors.indexOf("HVCPlastics") != -1 ? getSectorHVCPlastics(dataGenerator) : undefined,
    commercialRealEstate:
      inputSectors.indexOf("CommercialRealEstate") != -1 ? getSectorRealEstate(dataGenerator) : undefined,
    residentialRealEstate:
      inputSectors.indexOf("ResidentialRealEstate") != -1 ? getSectorRealEstate(dataGenerator) : undefined,
    steel: inputSectors.indexOf("Steel") != -1 ? getSectorSteel(dataGenerator) : undefined,
    freightTransportByRoad:
      inputSectors.indexOf("FreightTransportByRoad") != -1 ? getSectorFreightTransportByRoad(dataGenerator) : undefined,
    electricityGeneration:
      inputSectors.indexOf("ElectricityGeneration") != -1 ? getSectorElectricityGeneration(dataGenerator) : undefined,
    livestockFarming:
      inputSectors.indexOf("LivestockFarming") != -1 ? getSectorLivestockFarming(dataGenerator) : undefined,
    cement: inputSectors.indexOf("Cement") != -1 ? getSectorCement(dataGenerator) : undefined,
  };
}
