import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generateFloat } from "@e2e/fixtures/common/NumberFixtures";
import { type SfdrData } from "@clients/backend";
import { generateFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";

/**
 * Generates a random SFDR dataset
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns SFDR object with populated properties
 */
export function generateSfdrData(nullProbability = DEFAULT_PROBABILITY): SfdrData {
  const dataGenerator = new Generator(nullProbability);
  return {
    general: {
      general: {
        dataDate: generatePastDate(),
        fiscalYearDeviation: generateFiscalYearDeviation(),
        fiscalYearEnd: generatePastDate(),
        referencedReports: dataGenerator.reports,
        scopeOfEntities: dataGenerator.randomYesNo(),
      },
    },
    environmental: {
      greenhouseGasEmissions: {
        scope1: dataGenerator.randomDataPoint(generateFloat()),
        scope2: dataGenerator.randomDataPoint(generateFloat()),
        scope3: dataGenerator.randomDataPoint(generateFloat()),
        enterpriseValue: dataGenerator.randomDataPoint(generateFloat()),
        totalRevenue: dataGenerator.randomDataPoint(generateFloat()),
        fossilFuelSectorExposure: dataGenerator.randomDataPoint(dataGenerator.randomYesNo()),
      },
      energyPerformance: {
        renewableEnergyProduction: dataGenerator.randomDataPoint(generateFloat()),
        renewableEnergyConsumption: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumption: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyProduction: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceA: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceB: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceC: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceD: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceE: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceF: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceG: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceH: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceL: dataGenerator.randomDataPoint(generateFloat()),
        totalHighImpactClimateSectorEnergyConsumption: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionFossilFuels: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionCrudeOil: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionNaturalGas: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionLignite: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionCoal: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionNuclearEnergy: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionOther: dataGenerator.randomDataPoint(generateFloat()),
      },
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: dataGenerator.randomDataPoint(generateYesNo()),
        protectedAreasExposure: dataGenerator.randomDataPoint(generateYesNo()),
        rareOrEndangeredEcosystemsExposure: dataGenerator.randomDataPoint(generateYesNo()),
      },
      water: {
        emissionsToWater: dataGenerator.randomDataPoint(generateFloat()),
        waterConsumption: dataGenerator.randomDataPoint(generateFloat()),
        waterReused: dataGenerator.randomDataPoint(generateFloat()),
        waterManagementPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        waterStressAreaExposure: dataGenerator.randomDataPoint(generateYesNo()),
      },
      waste: {
        hazardousWaste: dataGenerator.randomDataPoint(generateFloat()),
        manufactureOfAgrochemicalPesticidesProducts: dataGenerator.randomDataPoint(generateYesNo()),
        landDegradationDesertificationSoilSealingExposure: dataGenerator.randomDataPoint(generateYesNo()),
        sustainableAgriculturePolicy: dataGenerator.randomDataPoint(generateYesNo()),
        sustainableOceansAndSeasPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        wasteNonRecycled: dataGenerator.randomDataPoint(generateFloat()),
        threatenedSpeciesExposure: dataGenerator.randomDataPoint(generateYesNo()),
        biodiversityProtectionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        deforestationPolicy: dataGenerator.randomDataPoint(generateYesNo()),
      },
      emissions: {
        inorganicPollutants: dataGenerator.randomDataPoint(generateFloat()),
        airPollutants: dataGenerator.randomDataPoint(generateFloat()),
        ozoneDepletionSubstances: dataGenerator.randomDataPoint(generateFloat()),
        carbonReductionInitiatives: dataGenerator.randomDataPoint(generateYesNo()),
      },
    },
    social: {
      socialAndEmployeeMatters: {
        humanRightsLegalProceedings: dataGenerator.randomDataPoint(generateYesNo()),
        iloCoreLabourStandards: dataGenerator.randomDataPoint(generateYesNo()),
        environmentalPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        corruptionLegalProceedings: dataGenerator.randomDataPoint(generateYesNo()),
        transparencyDisclosurePolicy: dataGenerator.randomDataPoint(generateYesNo()),
        humanRightsDueDiligencePolicy: dataGenerator.randomDataPoint(generateYesNo()),
        childForcedDiscriminationPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        iso14001Certificate: dataGenerator.randomDataPoint(generateYesNo()),
        briberyCorruptionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        fairBusinessMarketingAdvertisingPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        technologiesExpertiseTransferPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        fairCompetitionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        violationOfTaxRulesAndRegulation: dataGenerator.randomDataPoint(generateYesNo()),
        unGlobalCompactPrinciplesCompliancePolicy: dataGenerator.randomDataPoint(generateYesNo()),
        oecdGuidelinesForMultinationalEnterprisesPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        averageGrossHourlyEarningsMaleEmployees: dataGenerator.randomDataPoint(generateFloat()),
        averageGrossHourlyEarningsFemaleEmployees: dataGenerator.randomDataPoint(generateFloat()),
        femaleBoardMembers: dataGenerator.randomDataPoint(generateFloat()),
        maleBoardMembers: dataGenerator.randomDataPoint(generateFloat()),
        controversialWeaponsExposure: dataGenerator.randomDataPoint(generateYesNo()),
        workplaceAccidentPreventionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        rateOfAccidents: dataGenerator.randomDataPoint(generateFloat()),
        workdaysLost: dataGenerator.randomDataPoint(generateFloat()),
        supplierCodeOfConduct: dataGenerator.randomDataPoint(generateYesNo()),
        grievanceHandlingMechanism: dataGenerator.randomDataPoint(generateYesNo()),
        whistleblowerProtectionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        reportedIncidentsOfDiscrimination: dataGenerator.randomDataPoint(generateFloat()),
        sanctionsIncidentsOfDiscrimination: dataGenerator.randomDataPoint(generateFloat()),
        ceoToEmployeePayGap: dataGenerator.randomDataPoint(generateFloat()),
      },
      greenSecurities: {
        securitiesNotCertifiedAsGreen: dataGenerator.randomDataPoint(generateYesNo()),
      },
      humanRights: {
        humanRightsPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        humanRightsDueDiligence: dataGenerator.randomDataPoint(generateYesNo()),
        traffickingInHumanBeingsPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        reportedChildLabourIncidents: dataGenerator.randomDataPoint(generateYesNo()),
        reportedForcedOrCompulsoryLabourIncidents: dataGenerator.randomDataPoint(generateYesNo()),
        reportedIncidentsOfHumanRights: dataGenerator.randomDataPoint(generateFloat()),
      },
      antiCorruptionAndAntiBribery: {
        reportedCasesOfBriberyCorruption: dataGenerator.randomDataPoint(generateFloat()),
        reportedConvictionsOfBriberyCorruption: dataGenerator.randomDataPoint(generateFloat()),
        reportedFinesOfBriberyCorruption: dataGenerator.randomDataPoint(generateFloat()),
      },
    },
  };
}
