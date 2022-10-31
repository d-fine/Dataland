import { faker } from "@faker-js/faker";
import {
  EuTaxonomyDataForFinancials,
  EligibilityKpis,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  InsuranceKpis,
  CreditInstitutionKpis,
  InvestmentFirmKpis,
  SfdrData,
} from "../../../../build/clients/backend";

import { convertToPercentageString, getCompanyTypeCsvValue, getCompanyTypeHeader } from "../CsvUtils";
import {
  generateDatapointOrNotReportedAtRandom,
  generateDatapointOrNotReportedAtYesNo,
  generateReferencedReports,
} from "../common/DataPointFixtures";
import { getCsvCompanyMapping } from "../CompanyFixtures";
import { getCsvDataPointMapping } from "../common/DataPointFixtures";
import {
  getCsvSharedEuTaxonomyValuesMapping,
  populateSharedValues,
} from "../eutaxonomy/EuTaxonomySharedValuesFixtures";
import { FixtureData, DataPoint, ReferencedReports } from "../FixtureUtils";
import { randomPercentageValue } from "../common/NumberFixtures";
import { randomYesNoNaUndefined, randomYesNoUndefined } from "../common/YesNoFixtures";
// eslint-disable-next-line @typescript-eslint/no-var-requires,@typescript-eslint/no-unsafe-assignment
const { parse } = require("json2csv");

export function generateDataDate(): string {
  return faker.date.future(1).toISOString().split("T")[0];
}

export function generateIso4217CurrencyCode(): string {
  const someCommonIso4217CurrencyCodes = ["USD", "EUR", "CHF", "CAD", "AUD"];
  return someCommonIso4217CurrencyCodes[Math.floor(Math.random() * someCommonIso4217CurrencyCodes.length)];
}

// export function generateReports()

export function generateSfdrData(reports: ReferencedReports): SfdrData {
  const referencedReports = generateReferencedReports();
  const sfdr: SfdrData = {} as SfdrData;
  const scope1 = faker.datatype.number();
  const scope2 = faker.datatype.number();
  const scope3 = faker.datatype.number();
  const enterpriseValue = faker.datatype.number();
  const totalRevenue = faker.datatype.number();
  const fossilFuelSectorExposure = randomYesNoUndefined();
  const renewableEnergyProduction = faker.datatype.number();
  const renewableEnergyConsumption = faker.datatype.number();
  const nonRenewableEnergyConsumption = faker.datatype.number();
  const nonRenewableEnergyProduction = faker.datatype.number();
  const highImpactClimateSectorEnergyConsumptionNaceA = faker.datatype.number();
  const highImpactClimateSectorEnergyConsumptionNaceB = faker.datatype.number();
  const highImpactClimateSectorEnergyConsumptionNaceC = faker.datatype.number();
  const highImpactClimateSectorEnergyConsumptionNaceD = faker.datatype.number();
  const highImpactClimateSectorEnergyConsumptionNaceE = faker.datatype.number();
  const highImpactClimateSectorEnergyConsumptionNaceF = faker.datatype.number();
  const highImpactClimateSectorEnergyConsumptionNaceG = faker.datatype.number();
  const highImpactClimateSectorEnergyConsumptionNaceH = faker.datatype.number();
  const highImpactClimateSectorEnergyConsumptionNaceL = faker.datatype.number();
  const totalHighImpactClimateSectorEnergyConsumption = faker.datatype.number();
  const primaryForestAndWoodedLandOfNativeSpeciesExposure = randomYesNoUndefined();
  const protectedAreasExposure = randomYesNoUndefined();
  const rareOrEndangeredEcosystemsExposure = randomYesNoUndefined();
  const emissionsToWater = faker.datatype.number();
  const hazardousWaste = faker.datatype.number();
  const humanRightsPolicy = randomYesNoUndefined();
  const humanRightsLegalProceedings = randomYesNoUndefined();
  const iloCoreLabourStandards = randomYesNoUndefined();
  const environmentalPolicy = randomYesNoUndefined();
  const corruptionLegalProceedings = randomYesNoUndefined();
  const transparencyDisclosurePolicy = randomYesNoUndefined();
  const humanRightsDueDiligencePolicy = randomYesNoUndefined();
  const childForcedDiscriminationPolicy = randomYesNoUndefined();
  const iso14001 = randomYesNoUndefined();
  const briberyCoruptionPolicy = randomYesNoUndefined();
  const fairBusinessMarketingAdvertisingPolicy = randomYesNoUndefined();
  const technologiesExpertiseTransferPolicy = randomYesNoUndefined();
  const fairCompetitionPolicy = randomYesNoUndefined();
  const violationOfTaxRulesAndRegulation = randomYesNoUndefined();
  const unGlobalCompactPrinciplesCompliancePolicy = randomYesNoUndefined();
  const oecdGuidelinesForMultinationalEnterprisesPolicy = randomYesNoUndefined();
  const averageGrossHourlyEarningsMaleEmployees = faker.datatype.number();
  const averageGrossHourlyEarningsFemaleEmployees = faker.datatype.number();
  const femaleBoardMembers = faker.datatype.number();
  const maleBoardMembers = faker.datatype.number();
  const controversialWeaponsExposure = randomYesNoUndefined();
  const inorganicPollutants = faker.datatype.number();
  const airPollutants = faker.datatype.number();
  const ozoneDepletionSubstances = faker.datatype.number();
  const carbonReductionInitiatives = randomYesNoUndefined();
  const nonRenewableEnergyConsumptionFossilFuels = faker.datatype.number();
  const nonRenewableEnergyConsumptionCrudeOil = faker.datatype.number();
  const nonRenewableEnergyConsumptionNaturalGas = faker.datatype.number();
  const nonRenewableEnergyConsumptionLignite = faker.datatype.number();
  const nonRenewableEnergyConsumptionCoal = faker.datatype.number();
  const nonRenewableEnergyConsumptionNuclearEnergy = faker.datatype.number();
  const nonRenewableEnergyConsumptionOther = faker.datatype.number();
  const waterConsumption = faker.datatype.number();
  const waterReused = faker.datatype.number();
  const waterManagementPolicy = randomYesNoUndefined();
  const waterStressAreaExposure = randomYesNoUndefined();
  const manufactureOfAgrochemicalPesticidesProducts = randomYesNoUndefined();
  const landDegradationDesertificationSoilSealingExposure = randomYesNoUndefined();
  const sustainableAgriculturePolicy = randomYesNoUndefined();
  const sustainableOceansAndSeasPolicy = randomYesNoUndefined();
  const wasteNonRecycled = faker.datatype.number();
  const threatenedSpeciesExposure = randomYesNoUndefined();
  const biodiversityProtectionPolicy = randomYesNoUndefined();
  const deforestationPolicy = randomYesNoUndefined();
  const securitiesNotCertifiedAsGreen = randomYesNoUndefined();
  const workplaceAccidentPreventionPolicy = randomYesNoUndefined();
  const rateOfAccidents = faker.datatype.number();
  const workdaysLost = faker.datatype.number();
  const supplierCodeOfConduct = randomYesNoUndefined();
  const grievanceHandlingMechanism = randomYesNoUndefined();
  const whistleblowerProtectionPolicy = randomYesNoUndefined();
  const reportedIncidentsOfDiscrimination = faker.datatype.number();
  const sanctionsIncidentsOfDiscrimination = faker.datatype.number();
  const ceoToEmployeePayGap = faker.datatype.number();
  const humanRightsDueDiligence = randomYesNoUndefined();
  const traffickingInHumanBeingsPolicy = randomYesNoUndefined();
  const reportedChildLabourIncidents = randomYesNoUndefined();
  const reportedForcedOrCompulsoryLabourIncidents = randomYesNoUndefined();
  const reportedIncidentsOfHumanRights = faker.datatype.number();
  const reportedCasesOfBriberyCorruption = randomYesNoUndefined();
  const reportedConvictionsOfBriberyCorruption = faker.datatype.number();
  const reportedFinesOfBriberyCorruption = faker.datatype.number();

  sfdr.fiscalYear = faker.datatype.string();
  sfdr.fiscalYearEnd = generateDataDate();
  sfdr.annualReport = faker.datatype.string();
  sfdr.groupLevelAnnualReport = randomYesNoNaUndefined();
  sfdr.annualReport = faker.datatype.string();
  sfdr.annualReportDate = generateDataDate();
  sfdr.annualReportCurrency = generateIso4217CurrencyCode();
  sfdr.sustainabilityReport = faker.datatype.string();
  sfdr.groupLevelSustainabilityReport = randomYesNoNaUndefined();
  sfdr.sustainabilityReportDate = generateDataDate();
  sfdr.sustainabilityReportCurrency = generateIso4217CurrencyCode();
  sfdr.integratedReport = faker.datatype.string();
  sfdr.groupLevelIntegratedReport = randomYesNoNaUndefined();
  sfdr.integratedReportDate = generateDataDate();
  sfdr.integratedReportCurrency = generateIso4217CurrencyCode();
  sfdr.esefReport = faker.datatype.string();
  sfdr.groupLevelEsefReport = randomYesNoNaUndefined();
  sfdr.esefReportDate = generateDataDate();
  sfdr.esefReportCurrency = generateIso4217CurrencyCode();
  sfdr.scopeOfEntities = randomYesNoNaUndefined();
  sfdr.scope1 = generateDatapointOrNotReportedAtRandom(scope1, reports);
  sfdr.scope2 = generateDatapointOrNotReportedAtRandom(scope2, reports);
  sfdr.scope3 = generateDatapointOrNotReportedAtRandom(scope3, reports);
  sfdr.enterpriseValue = generateDatapointOrNotReportedAtRandom(enterpriseValue, reports);
  sfdr.totalRevenue = generateDatapointOrNotReportedAtRandom(totalRevenue, reports);
  sfdr.fossilFuelSectorExposure = generateDatapointOrNotReportedAtYesNo(fossilFuelSectorExposure, reports);
  sfdr.renewableEnergyProduction = generateDatapointOrNotReportedAtRandom(renewableEnergyProduction, reports);
  sfdr.renewableEnergyConsumption = generateDatapointOrNotReportedAtRandom(renewableEnergyConsumption, reports);
  sfdr.nonRenewableEnergyConsumption = generateDatapointOrNotReportedAtRandom(nonRenewableEnergyConsumption, reports);
  sfdr.nonRenewableEnergyProduction = generateDatapointOrNotReportedAtRandom(nonRenewableEnergyProduction, reports);
  sfdr.highImpactClimateSectorEnergyConsumptionNaceA = generateDatapointOrNotReportedAtRandom(
    highImpactClimateSectorEnergyConsumptionNaceA,
    reports
  );
  sfdr.highImpactClimateSectorEnergyConsumptionNaceB = generateDatapointOrNotReportedAtRandom(
    highImpactClimateSectorEnergyConsumptionNaceB,
    reports
  );
  sfdr.highImpactClimateSectorEnergyConsumptionNaceC = generateDatapointOrNotReportedAtRandom(
    highImpactClimateSectorEnergyConsumptionNaceC,
    reports
  );
  sfdr.highImpactClimateSectorEnergyConsumptionNaceD = generateDatapointOrNotReportedAtRandom(
    highImpactClimateSectorEnergyConsumptionNaceD,
    reports
  );
  sfdr.highImpactClimateSectorEnergyConsumptionNaceE = generateDatapointOrNotReportedAtRandom(
    highImpactClimateSectorEnergyConsumptionNaceE,
    reports
  );
  sfdr.highImpactClimateSectorEnergyConsumptionNaceF = generateDatapointOrNotReportedAtRandom(
    highImpactClimateSectorEnergyConsumptionNaceF,
    reports
  );
  sfdr.highImpactClimateSectorEnergyConsumptionNaceG = generateDatapointOrNotReportedAtRandom(
    highImpactClimateSectorEnergyConsumptionNaceG,
    reports
  );
  sfdr.highImpactClimateSectorEnergyConsumptionNaceH = generateDatapointOrNotReportedAtRandom(
    highImpactClimateSectorEnergyConsumptionNaceH,
    reports
  );
  sfdr.highImpactClimateSectorEnergyConsumptionNaceL = generateDatapointOrNotReportedAtRandom(
    highImpactClimateSectorEnergyConsumptionNaceL,
    reports
  );
  sfdr.totalHighImpactClimateSectorEnergyConsumption = generateDatapointOrNotReportedAtRandom(
    totalHighImpactClimateSectorEnergyConsumption,
    reports
  );
  sfdr.primaryForestAndWoodedLandOfNativeSpeciesExposure = generateDatapointOrNotReportedAtYesNo(
    primaryForestAndWoodedLandOfNativeSpeciesExposure,
    reports
  );
  sfdr.protectedAreasExposure = generateDatapointOrNotReportedAtYesNo(protectedAreasExposure, reports);
  sfdr.rareOrEndangeredEcosystemsExposure = generateDatapointOrNotReportedAtYesNo(
    rareOrEndangeredEcosystemsExposure,
    reports
  );
  sfdr.emissionsToWater = generateDatapointOrNotReportedAtRandom(emissionsToWater, reports);
  sfdr.hazardousWaste = generateDatapointOrNotReportedAtRandom(hazardousWaste, reports);
  sfdr.humanRightsPolicy = generateDatapointOrNotReportedAtYesNo(humanRightsPolicy, reports);
  sfdr.humanRightsLegalProceedings = generateDatapointOrNotReportedAtYesNo(humanRightsLegalProceedings, reports);
  sfdr.iloCoreLabourStandards = generateDatapointOrNotReportedAtYesNo(iloCoreLabourStandards, reports);
  sfdr.environmentalPolicy = generateDatapointOrNotReportedAtYesNo(environmentalPolicy, reports);
  sfdr.corruptionLegalProceedings = generateDatapointOrNotReportedAtYesNo(corruptionLegalProceedings, reports);
  sfdr.transparencyDisclosurePolicy = generateDatapointOrNotReportedAtYesNo(transparencyDisclosurePolicy, reports);
  sfdr.humanRightsDueDiligencePolicy = generateDatapointOrNotReportedAtYesNo(humanRightsDueDiligencePolicy, reports);
  sfdr.childForcedDiscriminationPolicy = generateDatapointOrNotReportedAtYesNo(
    childForcedDiscriminationPolicy,
    reports
  );
  sfdr.iso14001 = generateDatapointOrNotReportedAtYesNo(iso14001, reports);
  sfdr.briberyCoruptionPolicy = generateDatapointOrNotReportedAtYesNo(briberyCoruptionPolicy, reports);
  sfdr.fairBusinessMarketingAdvertisingPolicy = generateDatapointOrNotReportedAtYesNo(
    fairBusinessMarketingAdvertisingPolicy,
    reports
  );
  sfdr.technologiesExpertiseTransferPolicy = generateDatapointOrNotReportedAtYesNo(
    technologiesExpertiseTransferPolicy,
    reports
  );
  sfdr.fairCompetitionPolicy = generateDatapointOrNotReportedAtYesNo(fairCompetitionPolicy, reports);
  sfdr.violationOfTaxRulesAndRegulation = generateDatapointOrNotReportedAtYesNo(
    violationOfTaxRulesAndRegulation,
    reports
  );
  sfdr.unGlobalCompactPrinciplesCompliancePolicy = generateDatapointOrNotReportedAtYesNo(
    unGlobalCompactPrinciplesCompliancePolicy,
    reports
  );
  sfdr.oecdGuidelinesForMultinationalEnterprisesPolicy = generateDatapointOrNotReportedAtYesNo(
    oecdGuidelinesForMultinationalEnterprisesPolicy,
    reports
  );
  sfdr.averageGrossHourlyEarningsMaleEmployees = generateDatapointOrNotReportedAtRandom(
    averageGrossHourlyEarningsMaleEmployees,
    reports
  );
  sfdr.averageGrossHourlyEarningsFemaleEmployees = generateDatapointOrNotReportedAtRandom(
    averageGrossHourlyEarningsFemaleEmployees,
    reports
  );
  sfdr.femaleBoardMembers = generateDatapointOrNotReportedAtRandom(femaleBoardMembers, reports);
  sfdr.maleBoardMembers = generateDatapointOrNotReportedAtRandom(maleBoardMembers, reports);
  sfdr.controversialWeaponsExposure = generateDatapointOrNotReportedAtYesNo(controversialWeaponsExposure, reports);
  sfdr.inorganicPollutants = generateDatapointOrNotReportedAtRandom(inorganicPollutants, reports);
  sfdr.airPollutants = generateDatapointOrNotReportedAtRandom(airPollutants, reports);
  sfdr.ozoneDepletionSubstances = generateDatapointOrNotReportedAtRandom(ozoneDepletionSubstances, reports);
  sfdr.carbonReductionInitiatives = generateDatapointOrNotReportedAtYesNo(carbonReductionInitiatives, reports);
  sfdr.nonRenewableEnergyConsumptionFossilFuels = generateDatapointOrNotReportedAtRandom(
    nonRenewableEnergyConsumptionFossilFuels,
    reports
  );
  sfdr.nonRenewableEnergyConsumptionCrudeOil = generateDatapointOrNotReportedAtRandom(
    nonRenewableEnergyConsumptionCrudeOil,
    reports
  );
  sfdr.nonRenewableEnergyConsumptionNaturalGas = generateDatapointOrNotReportedAtRandom(
    nonRenewableEnergyConsumptionNaturalGas,
    reports
  );
  sfdr.nonRenewableEnergyConsumptionLignite = generateDatapointOrNotReportedAtRandom(
    nonRenewableEnergyConsumptionLignite,
    reports
  );
  sfdr.nonRenewableEnergyConsumptionCoal = generateDatapointOrNotReportedAtRandom(
    nonRenewableEnergyConsumptionCoal,
    reports
  );
  sfdr.nonRenewableEnergyConsumptionNuclearEnergy = generateDatapointOrNotReportedAtRandom(
    nonRenewableEnergyConsumptionNuclearEnergy,
    reports
  );
  sfdr.nonRenewableEnergyConsumptionOther = generateDatapointOrNotReportedAtRandom(
    nonRenewableEnergyConsumptionOther,
    reports
  );
  sfdr.waterConsumption = generateDatapointOrNotReportedAtRandom(waterConsumption, reports);
  sfdr.waterReused = generateDatapointOrNotReportedAtRandom(waterReused, reports);
  sfdr.waterManagementPolicy = generateDatapointOrNotReportedAtYesNo(waterManagementPolicy, reports);
  sfdr.waterStressAreaExposure = generateDatapointOrNotReportedAtYesNo(waterStressAreaExposure, reports);
  sfdr.manufactureOfAgrochemicalPesticidesProducts = generateDatapointOrNotReportedAtYesNo(
    manufactureOfAgrochemicalPesticidesProducts,
    reports
  );
  sfdr.landDegradationDesertificationSoilSealingExposure = generateDatapointOrNotReportedAtYesNo(
    landDegradationDesertificationSoilSealingExposure,
    reports
  );
  sfdr.sustainableAgriculturePolicy = generateDatapointOrNotReportedAtYesNo(sustainableAgriculturePolicy, reports);
  sfdr.sustainableOceansAndSeasPolicy = generateDatapointOrNotReportedAtYesNo(sustainableOceansAndSeasPolicy, reports);
  sfdr.wasteNonRecycled = generateDatapointOrNotReportedAtRandom(wasteNonRecycled, reports);
  sfdr.threatenedSpeciesExposure = generateDatapointOrNotReportedAtYesNo(threatenedSpeciesExposure, reports);
  sfdr.biodiversityProtectionPolicy = generateDatapointOrNotReportedAtYesNo(biodiversityProtectionPolicy, reports);
  sfdr.deforestationPolicy = generateDatapointOrNotReportedAtYesNo(deforestationPolicy, reports);
  sfdr.securitiesNotCertifiedAsGreen = generateDatapointOrNotReportedAtYesNo(securitiesNotCertifiedAsGreen, reports);
  sfdr.workplaceAccidentPreventionPolicy = generateDatapointOrNotReportedAtYesNo(
    workplaceAccidentPreventionPolicy,
    reports
  );
  sfdr.rateOfAccidents = generateDatapointOrNotReportedAtRandom(rateOfAccidents, reports);
  sfdr.workdaysLost = generateDatapointOrNotReportedAtRandom(workdaysLost, reports);
  sfdr.supplierCodeOfConduct = generateDatapointOrNotReportedAtYesNo(supplierCodeOfConduct, reports);
  sfdr.grievanceHandlingMechanism = generateDatapointOrNotReportedAtYesNo(grievanceHandlingMechanism, reports);
  sfdr.whistleblowerProtectionPolicy = generateDatapointOrNotReportedAtYesNo(whistleblowerProtectionPolicy, reports);
  sfdr.reportedIncidentsOfDiscrimination = generateDatapointOrNotReportedAtRandom(
    reportedIncidentsOfDiscrimination,
    reports
  );
  sfdr.sanctionsIncidentsOfDiscrimination = generateDatapointOrNotReportedAtRandom(
    sanctionsIncidentsOfDiscrimination,
    reports
  );
  sfdr.ceoToEmployeePayGap = generateDatapointOrNotReportedAtRandom(ceoToEmployeePayGap, reports);
  sfdr.humanRightsDueDiligence = generateDatapointOrNotReportedAtYesNo(humanRightsDueDiligence, reports);
  sfdr.traffickingInHumanBeingsPolicy = generateDatapointOrNotReportedAtYesNo(traffickingInHumanBeingsPolicy, reports);
  sfdr.reportedChildLabourIncidents = generateDatapointOrNotReportedAtYesNo(reportedChildLabourIncidents, reports);
  sfdr.reportedForcedOrCompulsoryLabourIncidents = generateDatapointOrNotReportedAtYesNo(
    reportedForcedOrCompulsoryLabourIncidents,
    reports
  );
  sfdr.reportedIncidentsOfHumanRights = generateDatapointOrNotReportedAtRandom(reportedIncidentsOfHumanRights, reports);
  sfdr.reportedCasesOfBriberyCorruption = generateDatapointOrNotReportedAtYesNo(
    reportedCasesOfBriberyCorruption,
    reports
  );
  sfdr.reportedConvictionsOfBriberyCorruption = generateDatapointOrNotReportedAtRandom(
    reportedConvictionsOfBriberyCorruption,
    reports
  );
  sfdr.reportedFinesOfBriberyCorruption = generateDatapointOrNotReportedAtRandom(
    reportedFinesOfBriberyCorruption,
    reports
  );
  return sfdr;
}
