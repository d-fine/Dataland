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
import { generateFixtureDataset, pickSubsetOfElements } from "@e2e/fixtures/FixtureUtils";
import { generateFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { faker } from "@faker-js/faker";
import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";

/**
 * Generates a set number of P2P fixtures
 * @param numFixtures the number of P2P fixtures to generate
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @param toggleRandomSectors determines if the sector list should include all possible sectors or a randomized selection
 * @returns a set number of P2P fixtures
 */
export function generateP2pFixtures(
  numFixtures: number,
  nullProbability = DEFAULT_PROBABILITY,
  toggleRandomSectors = true,
): FixtureData<PathwaysToParisData>[] {
  return generateFixtureDataset<PathwaysToParisData>(
    () => generateP2pData(nullProbability, toggleRandomSectors),
    numFixtures,
    (dataSet: PathwaysToParisData) => dataSet.general.general.dataDate.substring(0, 4),
  );
}

/**
 * Generates a random P2P dataset
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @param toggleRandomSectors determines if the sector list should include all possible sectors or a randomized selection
 * @returns a random P2P dataset
 */
function generateP2pData(nullProbability = DEFAULT_PROBABILITY, toggleRandomSectors = true): PathwaysToParisData {
  const dataGenerator = new P2pGenerator(nullProbability, toggleRandomSectors);
  return {
    general: dataGenerator.generateSectorGeneral(),
    ammonia: dataGenerator.getSectorAmmonia(),
    automotive: dataGenerator.getSectorAutomotive(),
    hvcPlastics: dataGenerator.getSectorHVCPlastics(),
    commercialRealEstate: dataGenerator.getSectorRealEstate("CommercialRealEstate"),
    residentialRealEstate: dataGenerator.getSectorRealEstate("ResidentialRealEstate"),
    steel: dataGenerator.getSectorSteel(),
    freightTransportByRoad: dataGenerator.getSectorFreightTransportByRoad(),
    electricityGeneration: dataGenerator.getSectorElectricityGeneration(),
    livestockFarming: dataGenerator.getSectorLivestockFarming(),
    cement: dataGenerator.getSectorCement(),
  };
}

class P2pGenerator extends Generator {
  sectors: Array<P2pSector>;

  constructor(nullProbability = DEFAULT_PROBABILITY, toggleRandomSectors = true) {
    super(nullProbability);
    this.sectors = this.generateSectors(toggleRandomSectors);
  }

  sectorPresent(sector: P2pSector): boolean {
    return this.sectors.indexOf(sector) != -1;
  }

  /**
   * Method to generate the sectors for a p2p dataset
   * @param toggleRandomSectors determines if the sector list should include all possible sectors or a randomized selection
   * @returns the sectors used for generating fixtures
   */
  generateSectors(toggleRandomSectors: boolean): P2pSector[] {
    if (toggleRandomSectors) {
      return pickSubsetOfElements(Object.values(P2pSector));
    } else {
      return Object.values(P2pSector);
    }
  }

  /**
   * Generates random general information for the P2P Framework
   * @returns a random P2pGeneral instance with the given sectors
   */
  generateSectorGeneral(): P2pGeneral {
    return {
      general: {
        dataDate: generateFutureDate(),
        sectors: this.sectors,
      },
      governance: {
        organisationalResponsibilityForParisCompatibility: this.randomYesNo(),
        parisCompatibilityInAverageRemunerationInPercent: this.randomPercentageValue(),
        parisCompatibilityInExecutiveRemunerationInPercent: this.randomPercentageValue(),
        shareOfEmployeesTrainedOnParisCompatibilityInPercent: this.randomPercentageValue(),
        qualificationRequirementsOnParisCompatibility: this.randomYesNo(),
        mobilityAndTravelPolicy: this.randomYesNo(),
        upstreamSupplierEngagementStrategy: this.randomYesNo(),
        upstreamSupplierProcurementPolicy: this.randomYesNo(),
        downstreamCustomerEngagement: this.randomYesNo(),
        policymakerEngagement: this.randomYesNo(),
      },
      climateTargets: {
        shortTermScienceBasedClimateTarget: this.randomYesNo(),
        longTermScienceBasedClimateTarget: this.randomYesNo(),
      },
      emissionsPlanning: {
        reductionOfAbsoluteEmissions: this.randomInt(),
        reductionOfRelativeEmissionsInPercent: this.randomPercentageValue(),
        relativeEmissionsInPercent: this.randomPercentageValue(),
        absoluteEmissions: this.randomInt(),
        climateActionPlan: this.randomYesNo(),
        useOfInternalCarbonPrice: this.randomYesNo(),
      },
      investmentPlanning: {
        investmentPlanForClimateTargets: this.randomYesNo(),
        capexShareInGhgIntensivePlantsInPercent: this.randomPercentageValue(),
        capexShareInNetZeroSolutionsInPercent: this.randomPercentageValue(),
        researchAndDevelopmentExpenditureForNetZeroSolutionsInPercent: this.randomPercentageValue(),
      },
    };
  }

  /**
   * Generates random P2pAmmonia instance
   * @returns a random P2pAmmonia or null
   */
  getSectorAmmonia(): P2pAmmonia | null {
    const data: P2pAmmonia = {
      decarbonisation: {
        energyMixInPercent: this.randomPercentageValue(),
        ccsTechnologyAdoptionInPercent: this.randomPercentageValue(),
        electrificationInPercent: this.randomPercentageValue(),
      },
      defossilisation: {
        useOfRenewableFeedstocksInPercent: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("Ammonia") ? data : null;
  }

  /**
   * Generates random P2pAutomotive instance
   * @returns a random P2pAutomotive or null
   */
  getSectorAutomotive(): P2pAutomotive | null {
    const data: P2pAutomotive = {
      energy: {
        productionSiteEnergyConsumption: this.randomInt(),
        energyMixInPercent: this.randomPercentageValue(),
      },
      technologyValueCreation: {
        driveMixInPercent: this.randomPercentageValue(),
        icAndHybridEnginePhaseOutDate: this.valueOrNull(generateFutureDate()),
        futureValueCreationStrategy: this.randomYesNo(),
      },
      materials: {
        materialUseManagementInPercent: this.randomPercentageValue(),
        useOfSecondaryMaterialsInPercent: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("Automotive") ? data : null;
  }

  /**
   * Generates random P2pHvcPlastics instance
   * @returns a random P2pHvcPlastics or null
   */
  getSectorHVCPlastics(): P2pHvcPlastics | null {
    const data: P2pHvcPlastics = {
      decarbonisation: {
        energyMixInPercent: this.randomPercentageValue(),
        electrificationInPercent: this.randomPercentageValue(),
      },
      defossilisation: {
        useOfRenewableFeedstocksInPercent: this.randomPercentageValue(),
        useOfBioplasticsInPercent: this.randomPercentageValue(),
        useOfCo2FromCarbonCaptureAndReUseTechnologiesInPercent: this.randomPercentageValue(),
        carbonCaptureAndUseStorageTechnologies: this.randomPercentageValue(),
      },
      recycling: {
        contributionToCircularEconomy: this.randomYesNo(),
        materialRecyclingInPercent: this.randomPercentageValue(),
        chemicalRecyclingInPercent: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("HVCPlastics") ? data : null;
  }

  /**
   * Generates random P2pRealEstate (commercial and residential) instance
   * @param sector the type of real estate
   * @returns a random P2pRealEstate or null
   */
  getSectorRealEstate(sector: P2pSector): P2pRealEstate | null {
    const data: P2pRealEstate = {
      buildingEfficiency: {
        buildingSpecificRefurbishmentRoadmapInPercent: this.randomPercentageValue(),
        zeroEmissionBuildingShareInPercent: this.randomPercentageValue(),
        buildingEnergyEfficiency: this.randomInt(),
      },
      energySource: {
        renewableHeatingInPercent: this.randomPercentageValue(),
      },
      technology: {
        useOfDistrictHeatingNetworks: this.randomYesNo(),
        heatPumpUsage: this.randomYesNo(),
      },
    };
    return this.sectorPresent(sector) ? data : null;
  }

  /**
   * Generates random P2pSteel instance
   * @returns a random P2pSteel or null
   */
  getSectorSteel(): P2pSteel | null {
    const data: P2pSteel = {
      energy: {
        emissionIntensityOfElectricity: this.randomInt(),
        greenHydrogenUsage: this.randomYesNo(),
      },
      technology: {
        blastFurnacePhaseOutInPercent: this.randomPercentageValue(),
        lowCarbonSteelScaleUpInPercent: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("Steel") ? data : null;
  }

  /**
   * Generates random P2pFreightTransportByRoad instance
   * @returns a random P2pFreightTransportByRoad or null
   */
  getSectorFreightTransportByRoad(): P2pFreightTransportByRoad | null {
    const data: P2pFreightTransportByRoad = {
      technology: {
        driveMixPerFleetSegment: this.randomPercentageValue(),
        icePhaseOut: this.valueOrNull(generateFutureDate()),
      },
      energy: {
        fuelMixInPercent: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("FreightTransportByRoad") ? data : null;
  }

  /**
   * Generates random P2pElectricityGeneration instance
   * @returns a random P2pElectricityGeneration or null
   */
  getSectorElectricityGeneration(): P2pElectricityGeneration | null {
    const data: P2pElectricityGeneration = {
      technology: {
        electricityMixEmissions: this.randomInt(),
        shareOfRenewableElectricityInPercent: this.randomPercentageValue(),
        naturalGasPhaseOut: this.valueOrNull(generateFutureDate()),
        coalPhaseOut: this.valueOrNull(generateFutureDate()),
        storageCapacityExpansionInPercent: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("ElectricityGeneration") ? data : null;
  }

  /**
   * Generates random P2pLivestockFarming instance
   * @returns a random P2pLivestockFarming or null
   */
  getSectorLivestockFarming(): P2pLivestockFarming | null {
    const data: P2pLivestockFarming = {
      emissionsFromManureAndFertiliserAndLivestock: {
        compostedFermentedManureInPercent: this.randomPercentageValue(),
        emissionProofFertiliserStorageInPercent: this.randomPercentageValue(),
      },
      animalWelfare: {
        mortalityRateInPercent: this.randomPercentageValue(),
      },
      animalFeed: {
        ownFeedInPercent: this.randomPercentageValue(),
        externalFeedCertification: this.randomBaseDataPoint(generateYesNo()),
        originOfExternalFeed: faker.company.buzzPhrase(),
        excessNitrogen: this.randomInt(),
        cropRotation: this.randomInt(),
        climateFriendlyProteinProductionInPercent: this.randomPercentageValue(),
        greenFodderInPercent: this.randomPercentageValue(),
      },
      energy: {
        renewableElectricityInPercent: this.randomPercentageValue(),
        renewableHeatingInPercent: this.randomPercentageValue(),
        electricGasPoweredMachineryVehicleInPercent: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("LivestockFarming") ? data : null;
  }

  /**
   * Generates random P2pCement instance
   * @returns a random P2pCement or null
   */
  getSectorCement(): P2pCement | null {
    const data: P2pCement = {
      energy: {
        energyMixInPercent: this.randomPercentageValue(),
        fuelMixInPercent: this.randomPercentageValue(),
        thermalEnergyEfficiencyInPercent: this.randomPercentageValue(),
        compositionOfThermalInputInPercent: this.randomPercentageValue(),
      },
      technology: {
        carbonCaptureAndUseTechnologyUsage: this.randomYesNo(),
        electrificationOfProcessHeatInPercent: this.randomPercentageValue(),
      },
      material: {
        clinkerFactorReduction: this.randomInt(),
        preCalcinedClayUsageInPercent: this.randomPercentageValue(),
        circularEconomyContribution: this.randomYesNo(),
      },
    };
    return this.sectorPresent("Cement") ? data : null;
  }
}
