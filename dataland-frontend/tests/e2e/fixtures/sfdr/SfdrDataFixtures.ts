import { faker } from "@faker-js/faker";
import {
  AnticorruptionAndAntibribery,
  Biodiversity,
  Emissions,
  EnergyPerformance,
  EnvironmentalData,
  SfdrGeneral,
  GreenhouseGasEmissions,
  GreenSecurities,
  SfdrHumanRights,
  SfdrData,
  SfdrSocialAndEmployeeMatters,
  SocialData,
  SfdrWaste,
  Water,
} from "@clients/backend";
import {
  generateNumericOrEmptyDatapoint,
  generateYesNoOrEmptyDatapoint,
  generateReferencedReports,
} from "@e2e/fixtures/common/DataPointFixtures";
import { randomYesNoNaUndefined } from "@e2e/fixtures/common/YesNoFixtures";
import { randomFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { randomEuroValue, randomNumber } from "@e2e/fixtures/common/NumberFixtures";
import { randomFiscalYearDeviationOrUndefined } from "@e2e/fixtures/common/FiscalYearDeviationFixtures";

export function generateSfdrData(): SfdrData {
  const sfdr: SfdrData = {};
  const reports = generateReferencedReports();

  const general: SfdrGeneral = {};
  general.fiscalYear = randomFiscalYearDeviationOrUndefined();
  general.fiscalYearEnd = randomFutureDate();
  general.annualReport = faker.datatype.string();
  general.groupLevelAnnualReport = randomYesNoNaUndefined();
  general.annualReport = faker.datatype.string();
  general.annualReportDate = randomFutureDate();
  general.annualReportCurrency = generateIso4217CurrencyCode();
  general.sustainabilityReport = faker.datatype.string();
  general.groupLevelSustainabilityReport = randomYesNoNaUndefined();
  general.sustainabilityReportDate = randomFutureDate();
  general.sustainabilityReportCurrency = generateIso4217CurrencyCode();
  general.integratedReport = faker.datatype.string();
  general.groupLevelIntegratedReport = randomYesNoNaUndefined();
  general.integratedReportDate = randomFutureDate();
  general.integratedReportCurrency = generateIso4217CurrencyCode();
  general.esefReport = faker.datatype.string();
  general.groupLevelEsefReport = randomYesNoNaUndefined();
  general.esefReportDate = randomFutureDate();
  general.esefReportCurrency = generateIso4217CurrencyCode();
  general.scopeOfEntities = randomYesNoNaUndefined();

  const greenhouseGasEmissions: GreenhouseGasEmissions = {};
  greenhouseGasEmissions.scope1 = generateNumericOrEmptyDatapoint(reports);
  greenhouseGasEmissions.scope2 = generateNumericOrEmptyDatapoint(reports);
  greenhouseGasEmissions.scope3 = generateNumericOrEmptyDatapoint(reports);
  greenhouseGasEmissions.enterpriseValue = generateNumericOrEmptyDatapoint(reports, randomEuroValue());
  greenhouseGasEmissions.totalRevenue = generateNumericOrEmptyDatapoint(reports, randomEuroValue());
  greenhouseGasEmissions.fossilFuelSectorExposure = generateYesNoOrEmptyDatapoint(reports);

  const energyPerformance: EnergyPerformance = {};
  energyPerformance.renewableEnergyProduction = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.renewableEnergyConsumption = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.nonRenewableEnergyConsumption = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.nonRenewableEnergyProduction = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.highImpactClimateSectorEnergyConsumptionNaceA = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.highImpactClimateSectorEnergyConsumptionNaceB = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.highImpactClimateSectorEnergyConsumptionNaceC = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.highImpactClimateSectorEnergyConsumptionNaceD = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.highImpactClimateSectorEnergyConsumptionNaceE = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.highImpactClimateSectorEnergyConsumptionNaceF = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.highImpactClimateSectorEnergyConsumptionNaceG = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.highImpactClimateSectorEnergyConsumptionNaceH = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.highImpactClimateSectorEnergyConsumptionNaceL = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.totalHighImpactClimateSectorEnergyConsumption = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.nonRenewableEnergyConsumptionFossilFuels = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.nonRenewableEnergyConsumptionCrudeOil = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.nonRenewableEnergyConsumptionNaturalGas = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.nonRenewableEnergyConsumptionLignite = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.nonRenewableEnergyConsumptionCoal = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.nonRenewableEnergyConsumptionNuclearEnergy = generateNumericOrEmptyDatapoint(reports);
  energyPerformance.nonRenewableEnergyConsumptionOther = generateNumericOrEmptyDatapoint(reports);

  const biodiversity: Biodiversity = {};
  biodiversity.primaryForestAndWoodedLandOfNativeSpeciesExposure = generateYesNoOrEmptyDatapoint(reports);
  biodiversity.protectedAreasExposure = generateYesNoOrEmptyDatapoint(reports);
  biodiversity.rareOrEndangeredEcosystemsExposure = generateYesNoOrEmptyDatapoint(reports);

  const water: Water = {};
  water.emissionsToWater = generateNumericOrEmptyDatapoint(reports);
  water.waterConsumption = generateNumericOrEmptyDatapoint(reports);
  water.waterReused = generateNumericOrEmptyDatapoint(reports);
  water.waterManagementPolicy = generateYesNoOrEmptyDatapoint(reports);
  water.waterStressAreaExposure = generateYesNoOrEmptyDatapoint(reports);

  const waste: SfdrWaste = {};
  waste.hazardousWaste = generateNumericOrEmptyDatapoint(reports);
  waste.manufactureOfAgrochemicalPesticidesProducts = generateYesNoOrEmptyDatapoint(reports);
  waste.landDegradationDesertificationSoilSealingExposure = generateYesNoOrEmptyDatapoint(reports);
  waste.sustainableAgriculturePolicy = generateYesNoOrEmptyDatapoint(reports);
  waste.sustainableOceansAndSeasPolicy = generateYesNoOrEmptyDatapoint(reports);
  waste.wasteNonRecycled = generateNumericOrEmptyDatapoint(reports);
  waste.threatenedSpeciesExposure = generateYesNoOrEmptyDatapoint(reports);
  waste.biodiversityProtectionPolicy = generateYesNoOrEmptyDatapoint(reports);
  waste.deforestationPolicy = generateYesNoOrEmptyDatapoint(reports);

  const socialAndEmployeeMatters: SfdrSocialAndEmployeeMatters = {};
  socialAndEmployeeMatters.humanRightsLegalProceedings = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.iloCoreLabourStandards = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.environmentalPolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.corruptionLegalProceedings = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.transparencyDisclosurePolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.humanRightsDueDiligencePolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.childForcedDiscriminationPolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.iso14001 = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.briberyCorruptionPolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.fairBusinessMarketingAdvertisingPolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.technologiesExpertiseTransferPolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.fairCompetitionPolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.violationOfTaxRulesAndRegulation = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.unGlobalCompactPrinciplesCompliancePolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.oecdGuidelinesForMultinationalEnterprisesPolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.averageGrossHourlyEarningsMaleEmployees = generateNumericOrEmptyDatapoint(
    reports,
    randomEuroValue(0, 100)
  );
  socialAndEmployeeMatters.averageGrossHourlyEarningsFemaleEmployees = generateNumericOrEmptyDatapoint(
    reports,
    randomEuroValue(0, 100)
  );
  socialAndEmployeeMatters.femaleBoardMembers = generateNumericOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.maleBoardMembers = generateNumericOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.controversialWeaponsExposure = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.workplaceAccidentPreventionPolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.rateOfAccidents = generateNumericOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.workdaysLost = generateNumericOrEmptyDatapoint(reports, randomNumber(10000));
  socialAndEmployeeMatters.supplierCodeOfConduct = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.grievanceHandlingMechanism = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.whistleblowerProtectionPolicy = generateYesNoOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.reportedIncidentsOfDiscrimination = generateNumericOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.sanctionsIncidentsOfDiscrimination = generateNumericOrEmptyDatapoint(reports);
  socialAndEmployeeMatters.ceoToEmployeePayGap = generateNumericOrEmptyDatapoint(reports);

  const emissions: Emissions = {};
  emissions.inorganicPollutants = generateNumericOrEmptyDatapoint(reports);
  emissions.airPollutants = generateNumericOrEmptyDatapoint(reports);
  emissions.ozoneDepletionSubstances = generateNumericOrEmptyDatapoint(reports);
  emissions.carbonReductionInitiatives = generateYesNoOrEmptyDatapoint(reports);

  const greenSecurities: GreenSecurities = {};
  greenSecurities.securitiesNotCertifiedAsGreen = generateYesNoOrEmptyDatapoint(reports);

  const humanRights: SfdrHumanRights = {};
  humanRights.humanRightsPolicy = generateYesNoOrEmptyDatapoint(reports);
  humanRights.humanRightsDueDiligence = generateYesNoOrEmptyDatapoint(reports);
  humanRights.traffickingInHumanBeingsPolicy = generateYesNoOrEmptyDatapoint(reports);
  humanRights.reportedChildLabourIncidents = generateYesNoOrEmptyDatapoint(reports);
  humanRights.reportedForcedOrCompulsoryLabourIncidents = generateYesNoOrEmptyDatapoint(reports);
  humanRights.reportedIncidentsOfHumanRights = generateNumericOrEmptyDatapoint(reports);

  const anticorruptionAndAntibribery: AnticorruptionAndAntibribery = {};
  anticorruptionAndAntibribery.reportedCasesOfBriberyCorruption = generateYesNoOrEmptyDatapoint(reports);
  anticorruptionAndAntibribery.reportedConvictionsOfBriberyCorruption = generateNumericOrEmptyDatapoint(reports);
  anticorruptionAndAntibribery.reportedFinesOfBriberyCorruption = generateNumericOrEmptyDatapoint(
    reports,
    randomEuroValue()
  );

  const social: SocialData = {};
  social.general = general;
  social.socialAndEmployeeMatters = socialAndEmployeeMatters;
  social.humanRights = humanRights;
  social.greenSecurities = greenSecurities;
  social.anticorruptionAndAntibribery = anticorruptionAndAntibribery;

  const environment: EnvironmentalData = {};
  environment.greenhouseGasEmissions = greenhouseGasEmissions;
  environment.energyPerformance = energyPerformance;
  environment.biodiversity = biodiversity;
  environment.water = water;
  environment.waste = waste;
  environment.emissions = emissions;

  sfdr.socialData = social;
  sfdr.environmentalData = environment;
  sfdr.referencedReports = reports;
  return sfdr;
}
