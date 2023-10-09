import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generateFloat } from "@e2e/fixtures/common/NumberFixtures";
import { type SfdrData } from "@clients/backend";
import { generateFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateFutureDate } from "@e2e/fixtures/common/DateFixtures";

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
        dataDate: generateFutureDate(),
        fiscalYearDeviation: generateFiscalYearDeviation(),
        fiscalYearEnd: generateFutureDate(),
        referencedReports: dataGenerator.reports,
        scopeOfEntities: dataGenerator.randomYesNoNa(),
      },
    },
    environmental: {
      greenhouseGasEmissions: {
        scope1GhgEmissionsInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        scope2GhgEmissionsInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        scope3GhgEmissionsInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        enterpriseValue: dataGenerator.randomCurrencyDataPoint(),
        totalRevenue: dataGenerator.randomCurrencyDataPoint(),
        fossilFuelSectorExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
      energyPerformance: {
        renewableEnergyProductionInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        renewableEnergyConsumptionInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyProductionInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        applicableHighImpactClimateSector: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionFossilFuelsInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionCrudeOilInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionNaturalGasInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionLigniteInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionCoalInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionNuclearEnergyInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionOtherInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceAInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceBInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceCInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceDInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceEInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceFInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceGInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceHInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceLInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
      },
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        protectedAreasExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        rareOrEndangeredEcosystemsExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        highlyBiodiverseGrasslandExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
      water: {
        emissionsToWaterInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        waterConsumptionInCubicMeters: dataGenerator.randomExtendedDataPoint(generateFloat()),
        waterReusedInCubicMeters: dataGenerator.randomExtendedDataPoint(generateFloat()),
        waterManagementPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        highWaterStressAreaExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
      waste: {
        hazardousAndRadioactiveWasteInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        manufactureOfAgrochemicalPesticidesProducts: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        landDegradationDesertificationSoilSealingExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        sustainableAgriculturePolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        sustainableOceansAndSeasPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        nonRecycledWasteInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        threatenedSpeciesExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        biodiversityProtectionPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        deforestationPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
      emissions: {
        emissionsOfInorganicPollutantsInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        emissionsOfAirPollutantsInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        emissionsOfOzoneDepletionSubstancesInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        carbonReductionInitiatives: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
    },
    social: {
      socialAndEmployeeMatters: {
        humanRightsLegalProceedings: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        iloCoreLabourStandards: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        environmentalPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        corruptionLegalProceedings: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        transparencyDisclosurePolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        humanRightsDueDiligencePolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        policyAgainstChildLabour: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        policyAgainstForcedLabour: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        policyAgainstDiscriminationInTheWorkplace: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        iso14001Certificate: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        policyAgainstBriberyAndCorruption: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        fairBusinessMarketingAdvertisingPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        technologiesExpertiseTransferPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        fairCompetitionPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        violationOfTaxRulesAndRegulation: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        unGlobalCompactPrinciplesCompliancePolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        oecdGuidelinesForMultinationalEnterprisesGrievanceHandling: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        averageGrossHourlyEarningsMaleEmployees: dataGenerator.randomCurrencyDataPoint(generateFloat()),
        averageGrossHourlyEarningsFemaleEmployees: dataGenerator.randomCurrencyDataPoint(generateFloat()),
        femaleBoardMembers: dataGenerator.randomExtendedDataPoint(generateFloat()),
        maleBoardMembers: dataGenerator.randomExtendedDataPoint(generateFloat()),
        controversialWeaponsExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        workplaceAccidentPreventionPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        rateOfAccidentsInPercent: dataGenerator.randomExtendedDataPoint(generateFloat()),
        workdaysLostInDays: dataGenerator.randomExtendedDataPoint(generateFloat()),
        supplierCodeOfConduct: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        grievanceHandlingMechanism: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        whistleblowerProtectionPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        reportedIncidentsOfDiscrimination: dataGenerator.randomExtendedDataPoint(generateFloat()),
        sanctionedIncidentsOfDiscrimination: dataGenerator.randomExtendedDataPoint(generateFloat()),
        ceoToEmployeePayGapRatio: dataGenerator.randomExtendedDataPoint(generateFloat()),
      },
      greenSecurities: {
        securitiesNotCertifiedAsGreen: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
      humanRights: {
        humanRightsPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        humanRightsDueDiligence: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        traffickingInHumanBeingsPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        reportedChildLabourIncidents: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        reportedForcedOrCompulsoryLabourIncidents: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        numberOfReportedIncidentsOfHumanRightsViolations: dataGenerator.randomExtendedDataPoint(generateFloat()),
      },
      antiCorruptionAndAntiBribery: {
        casesOfInsufficientActionAgainstBriberyAndCorruption: dataGenerator.randomExtendedDataPoint(generateFloat()),
        reportedConvictionsOfBriberyAndCorruption: dataGenerator.randomExtendedDataPoint(generateFloat()),
        totalAmountOfReportedFinesOfBriberyAndCorruption: dataGenerator.randomCurrencyDataPoint(generateFloat()),
      },
    },
  };
}
