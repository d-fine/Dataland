import { DEFAULT_PROBABILITY, Generator } from "@e2e/utils/FakeFixtureUtils";
import { generateFloat } from "@e2e/fixtures/common/NumberFixtures";
import { type SfdrData } from "@clients/backend";
import { generateFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";

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
        scope1InTonnes: dataGenerator.randomDataPoint(generateFloat()),
        scope2InTonnes: dataGenerator.randomDataPoint(generateFloat()),
        scope3InTonnes: dataGenerator.randomDataPoint(generateFloat()),
        enterpriseValue: dataGenerator.randomDataPoint(
            generateFloat(),
            dataGenerator.valueOrUndefined(generateCurrencyCode()),
        ),
        totalRevenue: dataGenerator.randomDataPoint(
            generateFloat(),
            dataGenerator.valueOrUndefined(generateCurrencyCode()),
        ),
        fossilFuelSectorExposure: dataGenerator.randomDataPoint(generateYesNo()),
      },
      energyPerformance: {
        renewableEnergyProductionInGWh: dataGenerator.randomDataPoint(generateFloat()),
        renewableEnergyConsumptionInGWh: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyConsumptionInGWh: dataGenerator.randomDataPoint(generateFloat()),
        nonRenewableEnergyProductionInGWh: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceAInGWh: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceBInGWh: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceCInGWh: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceDInGWh: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceEInGWh: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceFInGWh: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceGInGWh: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceHInGWh: dataGenerator.randomDataPoint(generateFloat()),
        highImpactClimateSectorEnergyConsumptionNaceLInGWh: dataGenerator.randomDataPoint(generateFloat()),
        totalHighImpactClimateSectorEnergyConsumptionInGWh: dataGenerator.randomDataPoint(generateFloat()),
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
      },
      water: {
        emissionsToWaterInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        waterConsumptionInCubicMeters: dataGenerator.randomDataPoint(generateFloat()),
        waterReusedInCubicMeters: dataGenerator.randomDataPoint(generateFloat()),
        waterManagementPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        waterStressAreaExposure: dataGenerator.randomDataPoint(generateYesNo()),
      },
      waste: {
        hazardousWasteInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        manufactureOfAgrochemicalPesticidesProducts: dataGenerator.randomDataPoint(generateYesNo()),
        landDegradationDesertificationSoilSealingExposure: dataGenerator.randomDataPoint(generateYesNo()),
        sustainableAgriculturePolicy: dataGenerator.randomDataPoint(generateYesNo()),
        sustainableOceansAndSeasPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        wasteNonRecycledInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        threatenedSpeciesExposure: dataGenerator.randomDataPoint(generateYesNo()),
        biodiversityProtectionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        deforestationPolicy: dataGenerator.randomDataPoint(generateYesNo()),
      },
      emissions: {
        inorganicPollutantsInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        airPollutantsInTonnes: dataGenerator.randomDataPoint(generateFloat()),
        ozoneDepletionSubstancesInTonnes: dataGenerator.randomDataPoint(generateFloat()),
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
        averageGrossHourlyEarningsMaleEmployees: dataGenerator.randomDataPoint(
            generateFloat(),
            dataGenerator.valueOrUndefined(generateCurrencyCode()),
        ),
        averageGrossHourlyEarningsFemaleEmployees: dataGenerator.randomDataPoint(
            generateFloat(),
            dataGenerator.valueOrUndefined(generateCurrencyCode()),
        ),
        femaleBoardMembers: dataGenerator.randomDataPoint(generateFloat()),
        maleBoardMembers: dataGenerator.randomDataPoint(generateFloat()),
        controversialWeaponsExposure: dataGenerator.randomDataPoint(generateYesNo()),
        workplaceAccidentPreventionPolicy: dataGenerator.randomDataPoint(generateYesNo()),
        rateOfAccidents: dataGenerator.randomDataPoint(generateFloat()),
        workdaysLostInDays: dataGenerator.randomDataPoint(generateFloat()),
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
        reportedFinesOfBriberyCorruption: dataGenerator.randomDataPoint(
            generateFloat(),
            dataGenerator.valueOrUndefined(generateCurrencyCode()),
        ),
      },
    },
  };
}
