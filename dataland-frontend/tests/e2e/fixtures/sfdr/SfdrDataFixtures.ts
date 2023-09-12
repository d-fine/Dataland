import { Generator } from "@e2e/utils/FakeFixtureUtils";
import { randomEuroValue } from "@e2e/fixtures/common/NumberFixtures";
import { type SfdrData } from "@clients/backend";
import { randomFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { randomYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { randomPastDate } from "@e2e/fixtures/common/DateFixtures";

/**
 *
 * @param undefinedProbability probability factor
 * @returns SFDR object with populated properties
 */
export function generateSfdrData(undefinedProbability = 0.5): SfdrData {
  const dataGenerator = new Generator(undefinedProbability);
  return {
    general: {
      general: {
        dataDate: randomPastDate(),
        fiscalYearDeviation: randomFiscalYearDeviation(),
        fiscalYearEnd: randomPastDate(),
        referencedReports: dataGenerator.getReports(),
        scopeOfEntities: dataGenerator.randomYesNo(),
      },
    },
    environmental: {
      greenhouseGasEmissions: {
        scope1: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        scope2: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        scope3: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        enterpriseValue: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        totalRevenue: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        fossilFuelSectorExposure: dataGenerator.randomDataPoint(randomYesNo()),
      },
      energyPerformance: {
        renewableEnergyProduction: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        renewableEnergyConsumption: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        nonRenewableEnergyConsumption: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        nonRenewableEnergyProduction: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceA: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceB: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceC: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceD: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceE: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceF: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceG: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceH: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceL: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        totalHighImpactClimateSectorEnergyConsumption: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        nonRenewableEnergyConsumptionFossilFuels: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        nonRenewableEnergyConsumptionCrudeOil: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        nonRenewableEnergyConsumptionNaturalGas: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        nonRenewableEnergyConsumptionLignite: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        nonRenewableEnergyConsumptionCoal: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        nonRenewableEnergyConsumptionNuclearEnergy: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        nonRenewableEnergyConsumptionOther: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
      },
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: dataGenerator.randomDataPoint(randomYesNo()),
        protectedAreasExposure: dataGenerator.randomDataPoint(randomYesNo()),
        rareOrEndangeredEcosystemsExposure: dataGenerator.randomDataPoint(randomYesNo()),
      },
      water: {
        emissionsToWater: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        waterConsumption: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        waterReused: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        waterManagementPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        waterStressAreaExposure: dataGenerator.randomDataPoint(randomYesNo()),
      },
      waste: {
        hazardousWaste: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        manufactureOfAgrochemicalPesticidesProducts: dataGenerator.randomDataPoint(randomYesNo()),
        landDegradationDesertificationSoilSealingExposure: dataGenerator.randomDataPoint(randomYesNo()),
        sustainableAgriculturePolicy: dataGenerator.randomDataPoint(randomYesNo()),
        sustainableOceansAndSeasPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        wasteNonRecycled: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        threatenedSpeciesExposure: dataGenerator.randomDataPoint(randomYesNo()),
        biodiversityProtectionPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        deforestationPolicy: dataGenerator.randomDataPoint(randomYesNo()),
      },
      emissions: {
        inorganicPollutants: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        airPollutants: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        ozoneDepletionSubstances: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        carbonReductionInitiatives: dataGenerator.randomDataPoint(randomYesNo()),
      },
    },
    social: {
      socialAndEmployeeMatters: {
        humanRightsLegalProceedings: dataGenerator.randomDataPoint(randomYesNo()),
        iloCoreLabourStandards: dataGenerator.randomDataPoint(randomYesNo()),
        environmentalPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        corruptionLegalProceedings: dataGenerator.randomDataPoint(randomYesNo()),
        transparencyDisclosurePolicy: dataGenerator.randomDataPoint(randomYesNo()),
        humanRightsDueDiligencePolicy: dataGenerator.randomDataPoint(randomYesNo()),
        childForcedDiscriminationPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        iso14001Certificate: dataGenerator.randomDataPoint(randomYesNo()),
        briberyCorruptionPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        fairBusinessMarketingAdvertisingPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        technologiesExpertiseTransferPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        fairCompetitionPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        violationOfTaxRulesAndRegulation: dataGenerator.randomDataPoint(randomYesNo()),
        unGlobalCompactPrinciplesCompliancePolicy: dataGenerator.randomDataPoint(randomYesNo()),
        oecdGuidelinesForMultinationalEnterprisesPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        averageGrossHourlyEarningsMaleEmployees: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        averageGrossHourlyEarningsFemaleEmployees: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        femaleBoardMembers: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        maleBoardMembers: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        controversialWeaponsExposure: dataGenerator.randomDataPoint(randomYesNo()),
        workplaceAccidentPreventionPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        rateOfAccidents: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        workdaysLost: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        supplierCodeOfConduct: dataGenerator.randomDataPoint(randomYesNo()),
        grievanceHandlingMechanism: dataGenerator.randomDataPoint(randomYesNo()),
        whistleblowerProtectionPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        reportedIncidentsOfDiscrimination: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        sanctionsIncidentsOfDiscrimination: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        ceoToEmployeePayGap: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
      },
      greenSecurities: {
        securitiesNotCertifiedAsGreen: dataGenerator.randomDataPoint(randomYesNo()),
      },
      humanRights: {
        humanRightsPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        humanRightsDueDiligence: dataGenerator.randomDataPoint(randomYesNo()),
        traffickingInHumanBeingsPolicy: dataGenerator.randomDataPoint(randomYesNo()),
        reportedChildLabourIncidents: dataGenerator.randomDataPoint(randomYesNo()),
        reportedForcedOrCompulsoryLabourIncidents: dataGenerator.randomDataPoint(randomYesNo()),
        reportedIncidentsOfHumanRights: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
      },
      antiCorruptionAndAntiBribery: {
        reportedCasesOfBriberyCorruption: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        reportedConvictionsOfBriberyCorruption: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
        reportedFinesOfBriberyCorruption: dataGenerator.randomDataPoint(randomEuroValue(0, 100)),
      },
    },
  };
}
/**
 * Generates an SFDR dataset with the value null for some categories, subcategories and field values.
 * Datasets that were uploaded via the Dataland API can look like this in production.
 * @returns the dataset
 */
export function generateOneSfdrDatasetWithManyNulls(): SfdrData {
  return {
    general: {
      general: {
        dataDate: "27-08-2022",
        fiscalYearDeviation: "Deviation",
        fiscalYearEnd: "marker-for-test",
        scopeOfEntities: null!,
        referencedReports: null!,
      },
    },
    social: {
      socialAndEmployeeMatters: null!,
    },
    environmental: null!,
  };
}
