import { DEFAULT_PROBABILITY } from "@e2e/utils/FakeFixtureUtils";
import { FrameworkGenerator } from "@e2e/utils/FrameworkFakeFixtureUtils";
import { generateFloat, generateInt, generatePercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { type ExtendedDataPointBigDecimal, type SfdrData } from "@clients/backend";
import { generateFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { generateYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { generateFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { generateFixtureDataset, pickSubsetOfElements } from "@e2e/fixtures/FixtureUtils";
import { HighImpactClimateSector } from "@/api-models/HighImpactClimateSector";

/**
 * Generates a set number of SFDR fixtures
 * @param numFixtures the number of SFDR fixtures to generate
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a set number of SFDR fixtures
 */
export function generateSfdrFixtures(
  numFixtures: number,
  nullProbability = DEFAULT_PROBABILITY,
): FixtureData<SfdrData>[] {
  return generateFixtureDataset<SfdrData>(
    () => generateSfdrData(nullProbability),
    numFixtures,
    (dataSet: SfdrData) => dataSet.general.general.dataDate.substring(0, 4),
  );
}

/**
 * Generates a random SFDR dataset
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns SFDR object with populated properties
 */
export function generateSfdrData(nullProbability = DEFAULT_PROBABILITY): SfdrData {
  const dataGenerator = new SfdrGenerator(nullProbability);
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
        applicableHighImpactClimateSectors: dataGenerator.generateHighImpactClimateSectors(),
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
        sustainableAgriculturePolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        sustainableOceansAndSeasPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        nonRecycledWasteInTonnes: dataGenerator.randomExtendedDataPoint(generateFloat()),
        threatenedSpeciesExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        biodiversityProtectionPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        deforestationPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
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
        environmentalPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        corruptionLegalProceedings: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        transparencyDisclosurePolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        humanRightsDueDiligencePolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        policyAgainstChildLabour: dataGenerator.randomBaseDataPoint(generateYesNo()),
        policyAgainstForcedLabour: dataGenerator.randomBaseDataPoint(generateYesNo()),
        policyAgainstDiscriminationInTheWorkplace: dataGenerator.randomBaseDataPoint(generateYesNo()),
        iso14001Certificate: dataGenerator.randomBaseDataPoint(generateYesNo()),
        policyAgainstBriberyAndCorruption: dataGenerator.randomBaseDataPoint(generateYesNo()),
        fairBusinessMarketingAdvertisingPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        technologiesExpertiseTransferPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        fairCompetitionPolicy: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        violationOfTaxRulesAndRegulation: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        unGlobalCompactPrinciplesCompliancePolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        oecdGuidelinesForMultinationalEnterprisesGrievanceHandling: dataGenerator.randomExtendedDataPoint(
          generateYesNo(),
        ),
        averageGrossHourlyEarningsMaleEmployees: dataGenerator.randomCurrencyDataPoint(),
        averageGrossHourlyEarningsFemaleEmployees: dataGenerator.randomCurrencyDataPoint(),
        femaleBoardMembers: dataGenerator.randomExtendedDataPoint(generateInt()),
        maleBoardMembers: dataGenerator.randomExtendedDataPoint(generateInt()),
        controversialWeaponsExposure: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        workplaceAccidentPreventionPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        rateOfAccidentsInPercent: dataGenerator.randomExtendedDataPoint(generatePercentageValue()),
        workdaysLostInDays: dataGenerator.randomExtendedDataPoint(generateFloat()),
        supplierCodeOfConduct: dataGenerator.randomBaseDataPoint(generateYesNo()),
        grievanceHandlingMechanism: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        whistleblowerProtectionPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        reportedIncidentsOfDiscrimination: dataGenerator.randomExtendedDataPoint(generateInt()),
        sanctionedIncidentsOfDiscrimination: dataGenerator.randomExtendedDataPoint(generateInt()),
        ceoToEmployeePayGapRatio: dataGenerator.randomExtendedDataPoint(generateFloat()),
      },
      greenSecurities: {
        securitiesNotCertifiedAsGreen: dataGenerator.randomExtendedDataPoint(generateYesNo()),
      },
      humanRights: {
        humanRightsPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        humanRightsDueDiligence: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        traffickingInHumanBeingsPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        reportedChildLabourIncidents: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        reportedForcedOrCompulsoryLabourIncidents: dataGenerator.randomExtendedDataPoint(generateYesNo()),
        numberOfReportedIncidentsOfHumanRightsViolations: dataGenerator.randomExtendedDataPoint(generateInt()),
      },
      antiCorruptionAndAntiBribery: {
        casesOfInsufficientActionAgainstBriberyAndCorruption: dataGenerator.randomExtendedDataPoint(generateInt()),
        reportedConvictionsOfBriberyAndCorruption: dataGenerator.randomExtendedDataPoint(generateInt()),
        totalAmountOfReportedFinesOfBriberyAndCorruption: dataGenerator.randomCurrencyDataPoint(),
      },
    },
  };
}

class SfdrGenerator extends FrameworkGenerator {
  /**
   * Generates a random map of procurement categories
   * @returns random map of procurement categories
   */
  generateHighImpactClimateSectors(): { [key: string]: ExtendedDataPointBigDecimal } {
    const keys: HighImpactClimateSector[] = pickSubsetOfElements(Object.values(HighImpactClimateSector), 0);
    return Object.fromEntries(
      new Map<string, ExtendedDataPointBigDecimal>(
        keys.map((naceCode) => [
          naceCode as string,
          this.dataPointGenerator.generateDataPoint(this.randomFloat(), this.reports) as ExtendedDataPointBigDecimal,
        ]),
      ),
    );
  }
}
