import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { randomEuroValue } from "@e2e/fixtures/common/NumberFixtures";
import { generateNumericOrEmptyDatapoint, generateYesNoOrEmptyDatapoint } from "@e2e/fixtures/common/DataPointFixtures";
import { generateReferencedReports } from "@e2e/fixtures/common/DataPointFixtures";
import { SfdrData } from "@clients/backend";
import { randomFiscalYearDeviation } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";
import { randomYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { randomPastDate } from "@e2e/fixtures/common/DateFixtures";

/**
 *
 * @param undefinedProbability probability factor
 * @returns SFDR object with populated properties
 */
export function generateSfdrData(undefinedProbability = 0.5): SfdrData {
  const reports = generateReferencedReports();
  return {
    general: {
      general: {
        dataDate: randomPastDate(),
        fiscalYear: randomFiscalYearDeviation(),
        fiscalYearEnd: randomPastDate(),
        referencedReports: reports,
        scopeOfEntities: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
    },
    environmental: {
      greenhouseGasEmissions: {
        scope1: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        scope2: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        scope3: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        enterpriseValue: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        totalRevenue: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        fossilFuelSectorExposure: generateYesNoOrEmptyDatapoint(reports),
      },
      energyPerformance: {
        renewableEnergyProduction: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        renewableEnergyConsumption: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        nonRenewableEnergyConsumption: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        nonRenewableEnergyProduction: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        highImpactClimateSectorEnergyConsumptionNaceA: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        highImpactClimateSectorEnergyConsumptionNaceB: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        highImpactClimateSectorEnergyConsumptionNaceC: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        highImpactClimateSectorEnergyConsumptionNaceD: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        highImpactClimateSectorEnergyConsumptionNaceE: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        highImpactClimateSectorEnergyConsumptionNaceF: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        highImpactClimateSectorEnergyConsumptionNaceG: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        highImpactClimateSectorEnergyConsumptionNaceH: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        highImpactClimateSectorEnergyConsumptionNaceL: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        totalHighImpactClimateSectorEnergyConsumption: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        nonRenewableEnergyConsumptionFossilFuels: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        nonRenewableEnergyConsumptionCrudeOil: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        nonRenewableEnergyConsumptionNaturalGas: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        nonRenewableEnergyConsumptionLignite: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        nonRenewableEnergyConsumptionCoal: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        nonRenewableEnergyConsumptionNuclearEnergy: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        nonRenewableEnergyConsumptionOther: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
      },
      biodiversity: {
        primaryForestAndWoodedLandOfNativeSpeciesExposure: generateYesNoOrEmptyDatapoint(reports),
        protectedAreasExposure: generateYesNoOrEmptyDatapoint(reports),
        rareOrEndangeredEcosystemsExposure: generateYesNoOrEmptyDatapoint(reports),
      },
      water: {
        emissionsToWater: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        waterConsumption: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        waterReused: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        waterManagementPolicy: generateYesNoOrEmptyDatapoint(reports),
        waterStressAreaExposure: generateYesNoOrEmptyDatapoint(reports),
      },
      waste: {
        hazardousWaste: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        manufactureOfAgrochemicalPesticidesProducts: generateYesNoOrEmptyDatapoint(reports),
        landDegradationDesertificationSoilSealingExposure: generateYesNoOrEmptyDatapoint(reports),
        sustainableAgriculturePolicy: generateYesNoOrEmptyDatapoint(reports),
        sustainableOceansAndSeasPolicy: generateYesNoOrEmptyDatapoint(reports),
        wasteNonRecycled: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        threatenedSpeciesExposure: generateYesNoOrEmptyDatapoint(reports),
        biodiversityProtectionPolicy: generateYesNoOrEmptyDatapoint(reports),
        deforestationPolicy: generateYesNoOrEmptyDatapoint(reports),
      },
      emissions: {
        inorganicPollutants: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        airPollutants: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        ozoneDepletionSubstances: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        carbonReductionInitiatives: generateYesNoOrEmptyDatapoint(reports),
      },
    },
    social: {
      socialAndEmployeeMatters: {
        humanRightsLegalProceedings: generateYesNoOrEmptyDatapoint(reports),
        iloCoreLabourStandards: generateYesNoOrEmptyDatapoint(reports),
        environmentalPolicy: generateYesNoOrEmptyDatapoint(reports),
        corruptionLegalProceedings: generateYesNoOrEmptyDatapoint(reports),
        transparencyDisclosurePolicy: generateYesNoOrEmptyDatapoint(reports),
        humanRightsDueDiligencePolicy: generateYesNoOrEmptyDatapoint(reports),
        childForcedDiscriminationPolicy: generateYesNoOrEmptyDatapoint(reports),
        iso14001Certificate: generateYesNoOrEmptyDatapoint(reports),
        briberyCorruptionPolicy: generateYesNoOrEmptyDatapoint(reports),
        fairBusinessMarketingAdvertisingPolicy: generateYesNoOrEmptyDatapoint(reports),
        technologiesExpertiseTransferPolicy: generateYesNoOrEmptyDatapoint(reports),
        fairCompetitionPolicy: generateYesNoOrEmptyDatapoint(reports),
        violationOfTaxRulesAndRegulation: generateYesNoOrEmptyDatapoint(reports),
        unGlobalCompactPrinciplesCompliancePolicy: generateYesNoOrEmptyDatapoint(reports),
        oecdGuidelinesForMultinationalEnterprisesPolicy: generateYesNoOrEmptyDatapoint(reports),
        averageGrossHourlyEarningsMaleEmployees: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        averageGrossHourlyEarningsFemaleEmployees: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        femaleBoardMembers: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        maleBoardMembers: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        controversialWeaponsExposure: generateYesNoOrEmptyDatapoint(reports),
        workplaceAccidentPreventionPolicy: generateYesNoOrEmptyDatapoint(reports),
        rateOfAccidents: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        workdaysLost: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        supplierCodeOfConduct: generateYesNoOrEmptyDatapoint(reports),
        grievanceHandlingMechanism: generateYesNoOrEmptyDatapoint(reports),
        whistleblowerProtectionPolicy: generateYesNoOrEmptyDatapoint(reports),
        reportedIncidentsOfDiscrimination: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        sanctionsIncidentsOfDiscrimination: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        ceoToEmployeePayGap: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
      },
      greenSecurities: {
        securitiesNotCertifiedAsGreen: generateYesNoOrEmptyDatapoint(reports),
      },
      humanRights: {
        humanRightsPolicy: generateYesNoOrEmptyDatapoint(reports),
        humanRightsDueDiligence: generateYesNoOrEmptyDatapoint(reports),
        traffickingInHumanBeingsPolicy: generateYesNoOrEmptyDatapoint(reports),
        reportedChildLabourIncidents: generateYesNoOrEmptyDatapoint(reports),
        reportedForcedOrCompulsoryLabourIncidents: generateYesNoOrEmptyDatapoint(reports),
        reportedIncidentsOfHumanRights: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
      },
      antiCorruptionAndAntiBribery: {
        reportedCasesOfBriberyCorruption: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        reportedConvictionsOfBriberyCorruption: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
        reportedFinesOfBriberyCorruption: valueOrUndefined(
          generateNumericOrEmptyDatapoint(reports, randomEuroValue(0, 100)),
          undefinedProbability,
        ),
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
