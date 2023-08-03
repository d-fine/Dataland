import {
  P2pAmmonia,
  P2pAutomotive,
  P2pCement,
  P2pElectricityGeneration,
  P2pFreightTransportByRoad,
  P2pGeneral,
  P2pHvcPlastics,
  P2pLivestockFarming,
  P2pRealEstate,
  P2pSector,
  P2pSteel,
  PathwaysToParisData,
} from "@clients/backend";
import { FixtureData } from "@sharedUtils/Fixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { randomFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { faker } from "@faker-js/faker";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { randomYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { randomNumber, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { generateBaseDataPointOrUndefined } from "@e2e/fixtures/common/BaseDataPointFixtures";

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
  toggleRandomSectors = true
): FixtureData<PathwaysToParisData>[] {
  return generateFixtureDataset<PathwaysToParisData>(
    () => generateP2pData(undefinedProbability, toggleRandomSectors),
    numFixtures,
    (dataSet: PathwaysToParisData) => dataSet.general.general.dataDate.substring(0, 4)
  );
}

/**
 * Generates random P2pAmmonia instance
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random P2pAmmonia
 */
export function getSectorAmmonia(undefinedProbability: number): P2pAmmonia {
  return {
    decarbonisation: {
      energyMix: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      ccsTechnologyAdoption: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      electrification: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
    defossilisation: {
      useOfRenewableFeedstocks: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
  };
}

/**
 * Generates random P2pAutomotive instance
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random P2pAutomotive
 */
export function getSectorAutomotive(undefinedProbability: number): P2pAutomotive {
  return {
    energy: {
      productionSiteEnergyConsumption: valueOrUndefined(randomNumber(10000), undefinedProbability),
      energyMix: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
    technologyValueCreation: {
      driveMix: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      icAndHybridEnginePhaseOutDate: valueOrUndefined(randomFutureDate(), undefinedProbability),
      futureValueCreationStrategy: valueOrUndefined(randomYesNo(), undefinedProbability),
    },
    materials: {
      materialUseManagement: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      useOfSecondaryMaterials: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
  };
}

/**
 * Generates random P2pHvcPlastics instance
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random P2pHvcPlastics
 */
export function getSectorHVCPlastics(undefinedProbability: number): P2pHvcPlastics {
  return {
    decarbonisation: {
      energyMix: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      electrification: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
    defossilisation: {
      useOfRenewableFeedstocks: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      useOfBioplastics: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      useOfCo2FromCarbonCaptureAndReUseTechnologies: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      carbonCaptureAndUseStorageTechnologies: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
    recycling: {
      contributionToCircularEconomy: valueOrUndefined(randomYesNo(), undefinedProbability),
      materialRecycling: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      chemicalRecycling: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
  };
}

/**
 * Generates random P2pRealEstate (commercial and residential) instance
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random P2pRealEstate
 */
export function getSectorRealEstate(undefinedProbability: number): P2pRealEstate {
  return {
    buildingEfficiency: {
      buildingSpecificReburbishmentRoadmap: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      zeroEmissionBuildingShare: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      buildingEnergyEfficiency: valueOrUndefined(randomNumber(10000), undefinedProbability),
    },
    energySource: {
      renewableHeating: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
    technology: {
      useOfDistrictHeatingNetworks: valueOrUndefined(randomYesNo(), undefinedProbability),
      heatPumpUsage: valueOrUndefined(randomYesNo(), undefinedProbability),
    },
  };
}

/**
 * Generates random P2pSteel instance
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random P2pSteel
 */
export function getSectorSteel(undefinedProbability: number): P2pSteel {
  return {
    energy: {
      emissionIntensityOfElectricity: valueOrUndefined(randomNumber(10000), undefinedProbability),
      greenHydrogenUsage: valueOrUndefined(randomYesNo(), undefinedProbability),
    },
    technology: {
      blastFurnacePhaseOut: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      lowCarbonSteelScaleUp: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
  };
}

/**
 * Generates random P2pFreightTransportByRoad instance
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random P2pFreightTransportByRoad
 */
export function getSectorFreightTransportByRoad(undefinedProbability: number): P2pFreightTransportByRoad {
  return {
    technology: {
      driveMix: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      icePhaseOut: valueOrUndefined(randomFutureDate(), undefinedProbability),
    },
    energy: {
      fuelMix: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
  };
}

/**
 * Generates random P2pElectricityGeneration instance
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random P2pElectricityGeneration
 */
export function getSectorElectricityGeneration(undefinedProbability: number): P2pElectricityGeneration {
  return {
    technology: {
      electricityMixEmissions: valueOrUndefined(randomNumber(10000), undefinedProbability),
      shareOfRenewableElectricity: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      naturalGasPhaseOut: valueOrUndefined(randomFutureDate(), undefinedProbability),
      coalPhaseOut: valueOrUndefined(randomFutureDate(), undefinedProbability),
      storageCapacityExpansion: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
  };
}

/**
 * Generates random P2pLivestockFarming instance
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random P2pLivestockFarming
 */
export function getSectorLivestockFarming(undefinedProbability: number): P2pLivestockFarming {
  return {
    emissionsFromManureAndFertiliserAndLivestock: {
      compostedFermentedManure: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      emissionProofFertiliserStorage: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
    animalWelfare: {
      mortalityRate: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
    animalFeed: {
      ownFeedPercentage: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      externalFeedCertification: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
      originOfExternalFeed: faker.company.buzzPhrase(),
      excessNitrogen: valueOrUndefined(randomNumber(10000), undefinedProbability),
      cropRotation: valueOrUndefined(randomNumber(10000), undefinedProbability),
      climateFriendlyProteinProduction: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      greenFodderPercentage: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
    energy: {
      renewableElectricityPercentage: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      renewableHeatingPercentage: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      electricGasPoweredMachineryVehiclePercentage: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
  };
}

/**
 * Generates random P2pCement instance
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random P2pCement
 */
export function getSectorCement(undefinedProbability: number): P2pCement {
  return {
    energy: {
      energyMix: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      fuelMix: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      thermalEnergyEfficiency: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      compositionOfThermalInput: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
    technology: {
      carbonCaptureAndUseTechnologyUsage: valueOrUndefined(randomYesNo(), undefinedProbability),
      electrificationOfProcessHeat: valueOrUndefined(randomPercentageValue(), undefinedProbability),
    },
    material: {
      clinkerFactorReduction: valueOrUndefined(randomNumber(10000), undefinedProbability),
      preCalcinedClayUsage: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      circularEconomyContribution: valueOrUndefined(randomYesNo(), undefinedProbability),
    },
  };
}

/**
 * Generates random general information for the P2P Framework
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @param sectors Sectors of the company
 * @returns a random P2pGeneral instance with the given sectors
 */
export function getSectorGeneral(undefinedProbability: number, sectors: Array<P2pSector>): P2pGeneral {
  return {
    general: {
      dataDate: randomFutureDate(),
      sectors: sectors,
    },
    governance: {
      organisationalResponsibilityForParisCompatibility: valueOrUndefined(randomYesNo(), undefinedProbability),
      parisCompatibilityInExecutiveRemuneration: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      parisCompatibilityInAverageRemuneration: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      shareOfEmployeesTrainedOnParisCompatibility: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      qualificationRequirementsOnParisCompatibility: valueOrUndefined(randomYesNo(), undefinedProbability),
      mobilityAndTravelPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
      upstreamSupplierEngagementStrategy: valueOrUndefined(randomYesNo(), undefinedProbability),
      upstreamSupplierProcurementPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
      downstreamCustomerEngagement: valueOrUndefined(randomYesNo(), undefinedProbability),
      policymakerEngagement: valueOrUndefined(randomYesNo(), undefinedProbability),
    },
    climateTargets: {
      shortTermScienceBasedClimateTarget: valueOrUndefined(randomYesNo(), undefinedProbability),
      longTermScienceBasedClimateTarget: valueOrUndefined(randomYesNo(), undefinedProbability),
    },
    emissionsPlanning: {
      reductionOfAbsoluteEmissions: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      reductionOfRelativeEmissions: valueOrUndefined(randomNumber(10000), undefinedProbability),
      absoluteEmissions: valueOrUndefined(randomNumber(10000), undefinedProbability),
      relativeEmissions: valueOrUndefined(randomNumber(10000), undefinedProbability),
      climateActionPlan: valueOrUndefined(randomYesNo(), undefinedProbability),
      useOfInternalCarbonPrice: valueOrUndefined(randomYesNo(), undefinedProbability),
    },
    investmentPlanning: {
      investmentPlanForClimateTargets: valueOrUndefined(randomYesNo(), undefinedProbability),
      capexShareInNetZeroSolutions: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      capexShareInGhgIntensivePlants: valueOrUndefined(randomPercentageValue(), undefinedProbability),
      researchAndDevelopmentExpenditureForNetZeroSolutions: valueOrUndefined(
        randomPercentageValue(),
        undefinedProbability
      ),
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
  const inputSectors = generateSectors(toggleRandomSectors);
  return {
    general: getSectorGeneral(undefinedProbability, inputSectors),
    ammonia: inputSectors.indexOf("Ammonia") != -1 ? getSectorAmmonia(undefinedProbability) : undefined,
    automotive: inputSectors.indexOf("Automotive") != -1 ? getSectorAutomotive(undefinedProbability) : undefined,
    hvcPlastics: inputSectors.indexOf("HVCPlastics") != -1 ? getSectorHVCPlastics(undefinedProbability) : undefined,
    commercialRealEstate:
      inputSectors.indexOf("CommercialRealEstate") != -1 ? getSectorRealEstate(undefinedProbability) : undefined,
    residentialRealEstate:
      inputSectors.indexOf("ResidentialRealEstate") != -1 ? getSectorRealEstate(undefinedProbability) : undefined,
    steel: inputSectors.indexOf("Steel") != -1 ? getSectorSteel(undefinedProbability) : undefined,
    freightTransportByRoad:
      inputSectors.indexOf("FreightTransportByRoad") != -1
        ? getSectorFreightTransportByRoad(undefinedProbability)
        : undefined,
    electricityGeneration:
      inputSectors.indexOf("ElectricityGeneration") != -1
        ? getSectorElectricityGeneration(undefinedProbability)
        : undefined,
    livestockFarming:
      inputSectors.indexOf("LivestockFarming") != -1 ? getSectorLivestockFarming(undefinedProbability) : undefined,
    cement: inputSectors.indexOf("Cement") != -1 ? getSectorCement(undefinedProbability) : undefined,
  };
}
