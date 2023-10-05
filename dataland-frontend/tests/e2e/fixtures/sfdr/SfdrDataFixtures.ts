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
        scope1GhgEmissionsInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        scope2GhgEmissionsInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        scope3GhgEmissionsInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        enterpriseValue: dataGenerator.randomDataPoint(generateFloat(1e6, 1e12)),
        totalRevenue: dataGenerator.randomDataPoint(generateFloat(1e6, 1e11)),
        fossilFuelSectorExposure: dataGenerator.randomDataPoint(dataGenerator.randomYesNo()),
      },
      energyPerformance: {
        renewableEnergyProductionInGWh: dataGenerator.randomDataPoint(generateFloat()),
        renewableEnergyConsumptionInGWh: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyProductionInGWh: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionInGWh: dataGenerator.randomDataPoint(generateFloat()),
        applicableHighImpactClimateSector: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionFossilFuelsInGWh: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionCrudeOilInGWh: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionNaturalGasInGWh: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionLigniteInGWh: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionCoalInGWh: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionNuclearEnergyInGWh: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionOtherInGWh: dataGenerator.randomDataPoint(generateFloat()),
      },
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: dataGenerator.randomDataPoint(generateYesNo()),
        protectedAreasExposure: dataGenerator.randomDataPoint(generateYesNo()),
        rareOrEndangeredEcosystemsExposure: dataGenerator.randomDataPoint(generateYesNo()),
        highlyBiodiverseGrasslandExposure: dataGenerator.randomDataPoint(generateYesNo()),
      },
      water: {
        emissionsToWaterInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        waterConsumptionInCubicMeters: dataGenerator.randomDataPoint(generateFloat()),
        waterReusedInCubicMeters: dataGenerator.randomDataPoint(generateFloat()),
        waterManagementPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        highWaterStressAreaExposure: dataGenerator.randomDataPoint(generateYesNo()),
      },
      waste: {
        hazardousAndRadioactiveWasteInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        manufactureOfAgrochemicalPesticidesProducts: dataGenerator.randomDataPoint(generateYesNo()),
        landDegradationDesertificationSoilSealingExposure: dataGenerator.randomDataPoint(generateYesNo()),
        sustainableAgriculturePolicy: dataGenerator.randomDataPoint(generateYesNo()),
        sustainableOceansAndSeasPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        nonRecycledWasteInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        threatenedSpeciesExposure: dataGenerator.randomDataPoint(generateYesNo()),
        biodiversityProtectionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        deforestationPolicy: dataGenerator.randomDataPoint(generateYesNo()),
      },
      emissions: {
        emissionsOfInorganicPollutantsInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        emissionsOfAirPollutantsInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        emissionsOfOzoneDepletionSubstancesInTonnes: dataGenerator.randomDataPoint(generateFloat()),
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
        policyAgainstChildLabour: dataGenerator.randomDataPoint(generateYesNo()),
        policyAgainstForcedLabour: dataGenerator.randomDataPoint(generateYesNo()),
        policyAgainstDiscriminationInTheWorkplace: dataGenerator.randomDataPoint(generateYesNo()),
        iso14001Certificate: dataGenerator.randomDataPoint(generateYesNo()),
        policyAgainstBriberyAndCorruption: dataGenerator.randomDataPoint(generateYesNo()),
        fairBusinessMarketingAdvertisingPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        technologiesExpertiseTransferPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        fairCompetitionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        violationOfTaxRulesAndRegulation: dataGenerator.randomDataPoint(generateYesNo()),
        unGlobalCompactPrinciplesCompliancePolicy: dataGenerator.randomDataPoint(generateYesNo()),
        oecdGuidelinesForMultinationalEnterprisesGrievanceHandling: dataGenerator.randomDataPoint(generateYesNo()),
        averageGrossHourlyEarningsMaleEmployees: dataGenerator.randomDataPoint(generateFloat()),
        averageGrossHourlyEarningsFemaleEmployees: dataGenerator.randomDataPoint(generateFloat()),
        femaleBoardMembers: dataGenerator.randomDataPoint(generateFloat()),
        maleBoardMembers: dataGenerator.randomDataPoint(generateFloat()),
        controversialWeaponsExposure: dataGenerator.randomDataPoint(generateYesNo()),
        workplaceAccidentPreventionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        rateOfAccidentsInPercent: dataGenerator.randomDataPoint(generateFloat()),
        workdaysLostInDays: dataGenerator.randomDataPoint(generateFloat()),
        supplierCodeOfConduct: dataGenerator.randomDataPoint(generateYesNo()),
        grievanceHandlingMechanism: dataGenerator.randomDataPoint(generateYesNo()),
        whistleblowerProtectionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        reportedIncidentsOfDiscrimination: dataGenerator.randomDataPoint(generateFloat()),
        sanctionedIncidentsOfDiscrimination: dataGenerator.randomDataPoint(generateFloat()),
        ceoToEmployeePayGapRatio: dataGenerator.randomDataPoint(generateFloat()),
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
        numberOfReportedIncidentsOfHumanRightsViolations: dataGenerator.randomDataPoint(generateFloat()),
      },
      antiCorruptionAndAntiBribery: {
        casesOfInsufficientActionAgainstBriberyAndCorruption: dataGenerator.randomDataPoint(generateFloat()),
        reportedConvictionsOfBriberyAndCorruption: dataGenerator.randomDataPoint(generateFloat()),
        totalAmountOfReportedFinesOfBriberyAndCorruption: dataGenerator.randomDataPoint(generateFloat()),
      },
    },
  };
}
