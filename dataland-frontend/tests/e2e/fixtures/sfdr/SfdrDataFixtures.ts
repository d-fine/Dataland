import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generateEuroValue } from "@e2e/fixtures/common/NumberFixtures";
import { type SfdrData } from "@clients/backend";
import { generateFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generatePastDate } from "@e2e/fixtures/common/DateFixtures";

/**
 *
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns SFDR object with populated properties
 */
export function generateSfdrData(undefinedProbability = DEFAULT_PROBABILITY): SfdrData {
  const dataGenerator = new Generator(undefinedProbability);
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
        scope1: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        scope2: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        scope3: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        enterpriseValue: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        totalRevenue: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        fossilFuelSectorExposure: dataGenerator.randomDataPoint(dataGenerator.randomYesNo()),
      },
      energyPerformance: {
        renewableEnergyProduction: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        renewableEnergyConsumption: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        nonRenewableEnergyConsumption: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        nonRenewableEnergyProduction: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceA: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceB: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceC: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceD: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceE: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceF: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceG: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceH: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        highImpactClimateSectorEnergyConsumptionNaceL: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        totalHighImpactClimateSectorEnergyConsumption: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        nonRenewableEnergyConsumptionFossilFuels: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        nonRenewableEnergyConsumptionCrudeOil: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        nonRenewableEnergyConsumptionNaturalGas: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        nonRenewableEnergyConsumptionLignite: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        nonRenewableEnergyConsumptionCoal: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        nonRenewableEnergyConsumptionNuclearEnergy: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        nonRenewableEnergyConsumptionOther: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
      },
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: dataGenerator.randomDataPoint(generateYesNo()),
        protectedAreasExposure: dataGenerator.randomDataPoint(generateYesNo()),
        rareOrEndangeredEcosystemsExposure: dataGenerator.randomDataPoint(generateYesNo()),
      },
      water: {
        emissionsToWater: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        waterConsumption: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        waterReused: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        waterManagementPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        waterStressAreaExposure: dataGenerator.randomDataPoint(generateYesNo()),
      },
      waste: {
        hazardousWaste: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        manufactureOfAgrochemicalPesticidesProducts: dataGenerator.randomDataPoint(generateYesNo()),
        landDegradationDesertificationSoilSealingExposure: dataGenerator.randomDataPoint(generateYesNo()),
        sustainableAgriculturePolicy: dataGenerator.randomDataPoint(generateYesNo()),
        sustainableOceansAndSeasPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        wasteNonRecycled: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        threatenedSpeciesExposure: dataGenerator.randomDataPoint(generateYesNo()),
        biodiversityProtectionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        deforestationPolicy: dataGenerator.randomDataPoint(generateYesNo()),
      },
      emissions: {
        inorganicPollutants: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        airPollutants: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        ozoneDepletionSubstances: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
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
        averageGrossHourlyEarningsMaleEmployees: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        averageGrossHourlyEarningsFemaleEmployees: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        femaleBoardMembers: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        maleBoardMembers: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        controversialWeaponsExposure: dataGenerator.randomDataPoint(generateYesNo()),
        workplaceAccidentPreventionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        rateOfAccidents: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        workdaysLost: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        supplierCodeOfConduct: dataGenerator.randomDataPoint(generateYesNo()),
        grievanceHandlingMechanism: dataGenerator.randomDataPoint(generateYesNo()),
        whistleblowerProtectionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        reportedIncidentsOfDiscrimination: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        sanctionsIncidentsOfDiscrimination: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        ceoToEmployeePayGap: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
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
        reportedIncidentsOfHumanRights: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
      },
      antiCorruptionAndAntiBribery: {
        reportedCasesOfBriberyCorruption: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        reportedConvictionsOfBriberyCorruption: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
        reportedFinesOfBriberyCorruption: dataGenerator.randomDataPoint(generateEuroValue(0, 100)),
      },
    },
  };
}
