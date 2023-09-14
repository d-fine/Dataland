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
import { generateFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { faker } from "@faker-js/faker";
import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";

/**
 * Generates a set number of P2P fixtures
 * @param numFixtures the number of P2P fixtures to generate
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @param toggleRandomSectors determines if the sector list should include all possible sectors or a randomized selection
 * @returns a set number of P2P fixtures
 */
export function generateP2pFixtures(
  numFixtures: number,
  undefinedProbability = DEFAULT_PROBABILITY,
  toggleRandomSectors = true,
): FixtureData<PathwaysToParisData>[] {
  return generateFixtureDataset<PathwaysToParisData>(
    () => generateP2pData(undefinedProbability, toggleRandomSectors),
    numFixtures,
    (dataSet: PathwaysToParisData) => dataSet.general.general.dataDate.substring(0, 4),
  );
}

/**
 * Generates a random P2P dataset
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @param toggleRandomSectors determines if the sector list should include all possible sectors or a randomized selection
 * @returns a random P2P dataset
 */
function generateP2pData(undefinedProbability = DEFAULT_PROBABILITY, toggleRandomSectors = true): PathwaysToParisData {
  const dataGenerator = new P2pGenerator(undefinedProbability, toggleRandomSectors);
  return {
    general: dataGenerator.getSectorGeneral(),
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

  constructor(undefinedProbability = DEFAULT_PROBABILITY, toggleRandomSectors = true) {
    super(undefinedProbability);
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
      return faker.helpers.arrayElements(Object.values(P2pSector));
    } else {
      return Object.values(P2pSector);
    }
  }

  /**
   * Generates random general information for the P2P Framework
   * @returns a random P2pGeneral instance with the given sectors
   */
  getSectorGeneral(): P2pGeneral {
    return {
      general: {
        dataDate: generateFutureDate(),
        sectors: this.sectors,
      },
      governance: {
        organisationalResponsibilityForParisCompatibility: this.randomYesNo(),
        parisCompatibilityInExecutiveRemuneration: this.randomPercentageValue(),
        parisCompatibilityInAverageRemuneration: this.randomPercentageValue(),
        shareOfEmployeesTrainedOnParisCompatibility: this.randomPercentageValue(),
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
        reductionOfAbsoluteEmissions: this.randomPercentageValue(),
        reductionOfRelativeEmissions: this.randomNumber(),
        absoluteEmissions: this.randomNumber(),
        relativeEmissions: this.randomNumber(),
        climateActionPlan: this.randomYesNo(),
        useOfInternalCarbonPrice: this.randomYesNo(),
      },
      investmentPlanning: {
        investmentPlanForClimateTargets: this.randomYesNo(),
        capexShareInNetZeroSolutions: this.randomPercentageValue(),
        capexShareInGhgIntensivePlants: this.randomPercentageValue(),
        researchAndDevelopmentExpenditureForNetZeroSolutions: this.randomPercentageValue(),
      },
    };
  }

  /**
   * Generates random P2pAmmonia instance
   * @returns a random P2pAmmonia or undefined
   */
  getSectorAmmonia(): P2pAmmonia | undefined {
    const data: P2pAmmonia = {
      decarbonisation: {
        energyMix: this.randomPercentageValue(),
        ccsTechnologyAdoption: this.randomPercentageValue(),
        electrification: this.randomPercentageValue(),
      },
      defossilisation: {
        useOfRenewableFeedstocks: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("Ammonia") ? data : undefined;
  }

  /**
   * Generates random P2pAutomotive instance
   * @returns a random P2pAutomotive or undefined
   */
  getSectorAutomotive(): P2pAutomotive | undefined {
    const data: P2pAutomotive = {
      energy: {
        productionSiteEnergyConsumption: this.randomNumber(),
        energyMix: this.randomPercentageValue(),
      },
      technologyValueCreation: {
        driveMix: this.randomPercentageValue(),
        icAndHybridEnginePhaseOutDate: this.valueOrUndefined(generateFutureDate()),
        futureValueCreationStrategy: this.randomYesNo(),
      },
      materials: {
        materialUseManagement: this.randomPercentageValue(),
        useOfSecondaryMaterials: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("Automotive") ? data : undefined;
  }

  /**
   * Generates random P2pHvcPlastics instance
   * @returns a random P2pHvcPlastics or undefined
   */
  getSectorHVCPlastics(): P2pHvcPlastics | undefined {
    const data: P2pHvcPlastics = {
      decarbonisation: {
        energyMix: this.randomPercentageValue(),
        electrification: this.randomPercentageValue(),
      },
      defossilisation: {
        useOfRenewableFeedstocks: this.randomPercentageValue(),
        useOfBioplastics: this.randomPercentageValue(),
        useOfCo2FromCarbonCaptureAndReUseTechnologies: this.randomPercentageValue(),
        carbonCaptureAndUseStorageTechnologies: this.randomPercentageValue(),
      },
      recycling: {
        contributionToCircularEconomy: this.randomYesNo(),
        materialRecycling: this.randomPercentageValue(),
        chemicalRecycling: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("HVCPlastics") ? data : undefined;
  }

  /**
   * Generates random P2pRealEstate (commercial and residential) instance
   * @param sector the type of real estate
   * @returns a random P2pRealEstate or undefined
   */
  getSectorRealEstate(sector: P2pSector): P2pRealEstate | undefined {
    const data: P2pRealEstate = {
      buildingEfficiency: {
        buildingSpecificReburbishmentRoadmap: this.randomPercentageValue(),
        zeroEmissionBuildingShare: this.randomPercentageValue(),
        buildingEnergyEfficiency: this.randomNumber(),
      },
      energySource: {
        renewableHeating: this.randomPercentageValue(),
      },
      technology: {
        useOfDistrictHeatingNetworks: this.randomYesNo(),
        heatPumpUsage: this.randomYesNo(),
      },
    };
    return this.sectorPresent(sector) ? data : undefined;
  }

  /**
   * Generates random P2pSteel instance
   * @returns a random P2pSteel or undefined
   */
  getSectorSteel(): P2pSteel | undefined {
    const data: P2pSteel = {
      energy: {
        emissionIntensityOfElectricity: this.randomNumber(),
        greenHydrogenUsage: this.randomYesNo(),
      },
      technology: {
        blastFurnacePhaseOut: this.randomPercentageValue(),
        lowCarbonSteelScaleUp: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("Steel") ? data : undefined;
  }

  /**
   * Generates random P2pFreightTransportByRoad instance
   * @returns a random P2pFreightTransportByRoad or undefined
   */
  getSectorFreightTransportByRoad(): P2pFreightTransportByRoad | undefined {
    const data: P2pFreightTransportByRoad = {
      technology: {
        driveMix: this.randomPercentageValue(),
        icePhaseOut: this.valueOrUndefined(generateFutureDate()),
      },
      energy: {
        fuelMix: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("FreightTransportByRoad") ? data : undefined;
  }

  /**
   * Generates random P2pElectricityGeneration instance
   * @returns a random P2pElectricityGeneration or undefined
   */
  getSectorElectricityGeneration(): P2pElectricityGeneration | undefined {
    const data: P2pElectricityGeneration = {
      technology: {
        electricityMixEmissions: this.randomNumber(),
        shareOfRenewableElectricity: this.randomPercentageValue(),
        naturalGasPhaseOut: this.valueOrUndefined(generateFutureDate()),
        coalPhaseOut: this.valueOrUndefined(generateFutureDate()),
        storageCapacityExpansion: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("ElectricityGeneration") ? data : undefined;
  }

  /**
   * Generates random P2pLivestockFarming instance
   * @returns a random P2pLivestockFarming or undefined
   */
  getSectorLivestockFarming(): P2pLivestockFarming | undefined {
    const data: P2pLivestockFarming = {
      emissionsFromManureAndFertiliserAndLivestock: {
        compostedFermentedManure: this.randomPercentageValue(),
        emissionProofFertiliserStorage: this.randomPercentageValue(),
      },
      animalWelfare: {
        mortalityRate: this.randomPercentageValue(),
      },
      animalFeed: {
        ownFeedPercentage: this.randomPercentageValue(),
        externalFeedCertification: this.randomBaseDataPoint(generateYesNo()),
        originOfExternalFeed: faker.company.buzzPhrase(),
        excessNitrogen: this.randomNumber(),
        cropRotation: this.randomNumber(),
        climateFriendlyProteinProduction: this.randomPercentageValue(),
        greenFodderPercentage: this.randomPercentageValue(),
      },
      energy: {
        renewableElectricityPercentage: this.randomPercentageValue(),
        renewableHeatingPercentage: this.randomPercentageValue(),
        electricGasPoweredMachineryVehiclePercentage: this.randomPercentageValue(),
      },
    };
    return this.sectorPresent("LivestockFarming") ? data : undefined;
  }

  /**
   * Generates random P2pCement instance
   * @returns a random P2pCement or undefined
   */
  getSectorCement(): P2pCement | undefined {
    const data: P2pCement = {
      energy: {
        energyMix: this.randomPercentageValue(),
        fuelMix: this.randomPercentageValue(),
        thermalEnergyEfficiency: this.randomPercentageValue(),
        compositionOfThermalInput: this.randomPercentageValue(),
      },
      technology: {
        carbonCaptureAndUseTechnologyUsage: this.randomYesNo(),
        electrificationOfProcessHeat: this.randomPercentageValue(),
      },
      material: {
        clinkerFactorReduction: this.randomNumber(),
        preCalcinedClayUsage: this.randomPercentageValue(),
        circularEconomyContribution: this.randomYesNo(),
      },
    };
    return this.sectorPresent("Cement") ? data : undefined;
  }
}
