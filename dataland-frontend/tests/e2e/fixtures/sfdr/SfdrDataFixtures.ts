import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generateCurrencyValue, generateFloat } from "@e2e/fixtures/common/NumberFixtures";
import { type SfdrData } from "@clients/backend";
import { generateFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateFutureDate } from "@e2e/fixtures/common/DateFixtures";

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
        dataDate: generateFutureDate(),
        fiscalYearDeviation: generateFiscalYearDeviation(),
        fiscalYearEnd: generateFutureDate(),
        referencedReports: dataGenerator.reports,
        scopeOfEntities: dataGenerator.randomYesNoNa(),
      },
    },
    environmental: {
      greenhouseGasEmissions: {
        scope1InTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        scope2InTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        scope3InTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        enterpriseValue: dataGenerator.randomCurrencyDataPoint(generateCurrencyValue()),
        totalRevenue: dataGenerator.randomCurrencyDataPoint(generateCurrencyValue()),
        fossilFuelSectorExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
      energyPerformance: {
        renewableEnergyProductionInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        renewableEnergyConsumptionInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyProductionInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceAInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceBInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceCInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceDInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceEInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceFInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceGInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceHInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceLInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        totalHighImpactClimateSectorEnergyConsumptionInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionFossilFuelsInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionCrudeOilInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionNaturalGasInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionLigniteInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionCoalInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionNuclearEnergyInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionOtherInGWh: dataGenerator.randomExtendedDataPoint(generateFloat()),
      },
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        protectedAreasExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        rareOrEndangeredEcosystemsExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
      water: {
        emissionsToWaterInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        waterConsumptionInCubicMeters: dataGenerator.randomExtendedDataPoint(generateFloat()),
        waterReusedInCubicMeters: dataGenerator.randomExtendedDataPoint(generateFloat()),
        waterManagementPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        waterStressAreaExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
      waste: {
        hazardousWasteInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        manufactureOfAgrochemicalPesticidesProducts: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        landDegradationDesertificationSoilSealingExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        sustainableAgriculturePolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        sustainableOceansAndSeasPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        wasteNonRecycledInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        threatenedSpeciesExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        biodiversityProtectionPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        deforestationPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
      emissions: {
        inorganicPollutantsInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        airPollutantsInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        ozoneDepletionSubstancesInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
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
        childForcedDiscriminationPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        iso14001Certificate: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        briberyCorruptionPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        fairBusinessMarketingAdvertisingPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        technologiesExpertiseTransferPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        fairCompetitionPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        violationOfTaxRulesAndRegulation: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        unGlobalCompactPrinciplesCompliancePolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        oecdGuidelinesForMultinationalEnterprisesPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        averageGrossHourlyEarningsMaleEmployees: dataGenerator.randomCurrencyDataPoint(generateCurrencyValue()),
        averageGrossHourlyEarningsFemaleEmployees: dataGenerator.randomCurrencyDataPoint(generateCurrencyValue()),
        femaleBoardMembers: dataGenerator.randomExtendedDataPoint(generateFloat()),
        maleBoardMembers: dataGenerator.randomExtendedDataPoint(generateFloat()),
        controversialWeaponsExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        workplaceAccidentPreventionPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        rateOfAccidents: dataGenerator.randomExtendedDataPoint(generateFloat()),
        workdaysLostInDays: dataGenerator.randomExtendedDataPoint(generateFloat()),
        supplierCodeOfConduct: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        grievanceHandlingMechanism: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        whistleblowerProtectionPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        reportedIncidentsOfDiscrimination: dataGenerator.randomExtendedDataPoint(generateFloat()),
        sanctionsIncidentsOfDiscrimination: dataGenerator.randomExtendedDataPoint(generateFloat()),
        ceoToEmployeePayGap: dataGenerator.randomExtendedDataPoint(generateFloat()),
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
        reportedIncidentsOfHumanRights: dataGenerator.randomExtendedDataPoint(generateFloat()),
      },
      antiCorruptionAndAntiBribery: {
        reportedCasesOfBriberyCorruption: dataGenerator.randomExtendedDataPoint(generateFloat()),
        reportedConvictionsOfBriberyCorruption: dataGenerator.randomExtendedDataPoint(generateFloat()),
        reportedFinesOfBriberyCorruption: dataGenerator.randomCurrencyDataPoint(generateCurrencyValue()),
      },
    },
  };
}
