import { faker } from "@faker-js/faker";
import { LksgData, ProductionSite } from "@clients/backend";
import { randomYesNoUndefined } from "@e2e/fixtures/common/YesNoFixtures";
import { randomFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";

export function generateProductionSite(): ProductionSite {
  const fakeSiteName = faker.company.name();
  const yesNo = randomYesNoUndefined();
  const fullFormattedFakeAddress =
    faker.address.street() +
    " " +
    faker.address.buildingNumber() +
    ", " +
    faker.address.zipCode() +
    " " +
    faker.address.city() +
    ", " +
    faker.address.country();
  const fakeGoodsAndServices = Array.from({ length: faker.datatype.number({ min: 0, max: 5 }) }, () => {
    return faker.commerce.productName();
  });

  return {
    name: fakeSiteName,
    isInHouseProductionOrIsContractProcessing: yesNo,
    address: fullFormattedFakeAddress,
    listOfGoodsAndServices: fakeGoodsAndServices,
  };
}

export function generateArrayOfProductionSites(): ProductionSite[] {
  return Array.from({ length: faker.datatype.number({ min: 0, max: 5 }) }, generateProductionSite);
}

export function generateVatIdentificationNumber(): string {
  const fakeCountryCode = faker.address.countryCode();
  const randomNineDigitNumber = faker.random.numeric(9);
  return fakeCountryCode + randomNineDigitNumber.toString();
}

export function getCompanyLegalForm(): string {
  const legalForms = [
    "Public Limited Company (PLC)",
    "Private Limited Company (Ltd)",
    "Limited Liability Partnership (LLP)",
    "Partnership without Limited Liability",
    "Sole Trader",
    "GmbH",
    "AG",
    "GmbH & Co. KG",
  ];
  return legalForms[Math.floor(Math.random() * legalForms.length)];
}

export function generateLksgData(): LksgData {
  const returnBase: LksgData = {};

  returnBase.betterWorkProgramCertificate = randomYesNoUndefined();
  returnBase.dataDate = randomFutureDate();
  returnBase.companyLegalForm = getCompanyLegalForm();
  returnBase.vatIdentificationNumber = generateVatIdentificationNumber();
  returnBase.numberOfEmployees = faker.datatype.number({ min: 1000, max: 200000 });
  returnBase.shareOfTemporaryWorkers = faker.datatype.number({ min: 0, max: 30, precision: 0.01 });
  returnBase.totalRevenue = faker.datatype.float({ min: 10000000, max: 100000000000 });
  returnBase.totalRevenueCurrency = generateIso4217CurrencyCode();
  returnBase.responsibilitiesForFairWorkingConditions = randomYesNoUndefined();
  returnBase.responsibilitiesForTheEnvironment = randomYesNoUndefined();
  returnBase.responsibilitiesForOccupationalSafety = randomYesNoUndefined();
  returnBase.riskManagementSystem = randomYesNoUndefined();
  returnBase.grievanceHandlingMechanism = randomYesNoUndefined();
  returnBase.grievanceHandlingMechanismUsedForReporting = randomYesNoUndefined();
  returnBase.codeOfConduct = randomYesNoUndefined();
  returnBase.codeOfConductRiskManagementTopics = randomYesNoUndefined();
  returnBase.codeOfConductTraining = randomYesNoUndefined();
  returnBase.legalProceedings = randomYesNoUndefined();
  returnBase.employeeUnder18 = randomYesNoUndefined();
  returnBase.employeeUnder18Under15 = randomYesNoUndefined();
  returnBase.employeeUnder18Apprentices = randomYesNoUndefined();
  returnBase.employmentUnderLocalMinimumAgePrevention = randomYesNoUndefined();
  returnBase.employmentUnderLocalMinimumAgePreventionEmploymentContracts = randomYesNoUndefined();
  returnBase.employmentUnderLocalMinimumAgePreventionJobDescription = randomYesNoUndefined();
  returnBase.employmentUnderLocalMinimumAgePreventionIdentityDocuments = randomYesNoUndefined();
  returnBase.employmentUnderLocalMinimumAgePreventionTraining = randomYesNoUndefined();
  returnBase.employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge = randomYesNoUndefined();
  returnBase.forcedLabourAndSlaveryPrevention = randomYesNoUndefined();
  returnBase.forcedLabourAndSlaveryPreventionEmploymentContracts = randomYesNoUndefined();
  returnBase.forcedLabourAndSlaveryPreventionIdentityDocuments = randomYesNoUndefined();
  returnBase.forcedLabourAndSlaveryPreventionFreeMovement = randomYesNoUndefined();
  returnBase.forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets = randomYesNoUndefined();
  returnBase.forcedLabourAndSlaveryPreventionProvisionTraining = randomYesNoUndefined();
  returnBase.documentedWorkingHoursAndWages = randomYesNoUndefined();
  returnBase.adequateLivingWage = randomYesNoUndefined();
  returnBase.regularWagesProcessFlow = randomYesNoUndefined();
  returnBase.fixedHourlyWages = randomYesNoUndefined();
  returnBase.workplaceAccidentsUnder10 = randomYesNoUndefined();
  returnBase.freedomOfAssociation = randomYesNoUndefined();
  returnBase.discriminationForTradeUnionMembers = randomYesNoUndefined();
  returnBase.freedomOfOperationForTradeUnion = randomYesNoUndefined();
  returnBase.freedomOfAssociationTraining = randomYesNoUndefined();
  returnBase.worksCouncil = randomYesNoUndefined();
  returnBase.diversityAndInclusionRole = randomYesNoUndefined();
  returnBase.preventionOfMistreatments = randomYesNoUndefined();
  returnBase.equalOpportunitiesOfficer = randomYesNoUndefined();
  returnBase.riskOfHarmfulPollution = randomYesNoUndefined();
  returnBase.unlawfulEvictionAndTakingOfLand = randomYesNoUndefined();
  returnBase.useOfPrivatePublicSecurityForces = randomYesNoUndefined();
  returnBase.useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights = randomYesNoUndefined();
  returnBase.mercuryAndMercuryWasteHandling = randomYesNoUndefined();
  returnBase.mercuryAndMercuryWasteHandlingPolicy = randomYesNoUndefined();
  returnBase.chemicalHandling = randomYesNoUndefined();
  returnBase.environmentalManagementSystem = randomYesNoUndefined();
  returnBase.environmentalManagementSystemInternationalCertification = randomYesNoUndefined();
  returnBase.environmentalManagementSystemNationalCertification = randomYesNoUndefined();
  returnBase.legalRestrictedWaste = randomYesNoUndefined();
  returnBase.legalRestrictedWasteProcesses = randomYesNoUndefined();
  returnBase.mercuryAddedProductsHandling = randomYesNoUndefined();
  returnBase.mercuryAddedProductsHandlingRiskOfExposure = randomYesNoUndefined();
  returnBase.mercuryAddedProductsHandlingRiskOfDisposal = randomYesNoUndefined();
  returnBase.mercuryAndMercuryCompoundsProductionAndUse = randomYesNoUndefined();
  returnBase.mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure = randomYesNoUndefined();
  returnBase.persistentOrganicPollutantsProductionAndUse = randomYesNoUndefined();
  returnBase.persistentOrganicPollutantsProductionAndUseRiskOfExposure = randomYesNoUndefined();
  returnBase.persistentOrganicPollutantsProductionAndUseRiskOfDisposal = randomYesNoUndefined();
  returnBase.persistentOrganicPollutantsProductionAndUseTransboundaryMovements = randomYesNoUndefined();
  returnBase.persistentOrganicPollutantsProductionAndUseRiskForImportingState = randomYesNoUndefined();
  returnBase.hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein = randomYesNoUndefined();
  returnBase.hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein = randomYesNoUndefined();
  returnBase.hazardousWasteDisposal = randomYesNoUndefined();
  returnBase.hazardousWasteDisposalRiskOfImport = randomYesNoUndefined();
  returnBase.hazardousAndOtherWasteImport = randomYesNoUndefined();
  returnBase.betterWorkProgramCertificate = randomYesNoUndefined();
  returnBase.riskManagementSystemCertification = randomYesNoUndefined();
  returnBase.amforiBsciAuditReport = randomYesNoUndefined();
  returnBase.initiativeClauseSocialCertification = randomYesNoUndefined();
  returnBase.responsibleBusinessAssociationCertification = randomYesNoUndefined();
  returnBase.fairLabourAssociationCertification = randomYesNoUndefined();
  returnBase.lksgInScope = randomYesNoUndefined();
  returnBase.oshMonitoring = randomYesNoUndefined();
  returnBase.oshPolicy = randomYesNoUndefined();
  returnBase.oshTraining = randomYesNoUndefined();
  returnBase.iso26000 = randomYesNoUndefined();
  returnBase.smetaSocialAuditConcept = randomYesNoUndefined();
  returnBase.iso45001Certification = randomYesNoUndefined();
  returnBase.iso14000Certification = randomYesNoUndefined();
  returnBase.emasCertification = randomYesNoUndefined();
  returnBase.sa8000Certification = randomYesNoUndefined();
  returnBase.iso37001Certification = randomYesNoUndefined();
  returnBase.iso37301Certification = randomYesNoUndefined();
  returnBase.oshPolicyMachineSafety = randomYesNoUndefined();
  returnBase.oshPolicyFireProtection = randomYesNoUndefined();
  returnBase.oshPolicyWorkingHours = randomYesNoUndefined();
  returnBase.oshManagementSystem = randomYesNoUndefined();
  returnBase.oshPolicyWorkplaceErgonomics = randomYesNoUndefined();
  returnBase.oshPolicyTrainingAddressed = randomYesNoUndefined();
  returnBase.oshPolicyTraining = randomYesNoUndefined();
  returnBase.oshPolicyPersonalProtectiveEquipment = randomYesNoUndefined();
  returnBase.oshPolicyAccidentsBehaviouralResponse = randomYesNoUndefined();
  returnBase.oshManagementSystemInternationalCertification = randomYesNoUndefined();
  returnBase.oshPolicyDisasterBehaviouralResponse = randomYesNoUndefined();
  returnBase.oshManagementSystemNationalCertification = randomYesNoUndefined();
  returnBase.oshPolicyHandlingChemicalsAndOtherHazardousSubstances = randomYesNoUndefined();
  returnBase.fairWorkingConditionsPolicy = randomYesNoUndefined();
  returnBase.fairAndEthicalRecruitmentPolicy = randomYesNoUndefined();
  returnBase.equalOpportunitiesAndNondiscriminationPolicy = randomYesNoUndefined();
  returnBase.healthAndSafetyPolicy = randomYesNoUndefined();
  returnBase.complaintsAndGrievancesPolicy = randomYesNoUndefined();
  returnBase.forcedLabourPolicy = randomYesNoUndefined();
  returnBase.childLabourPolicy = randomYesNoUndefined();
  returnBase.environmentalImpactPolicy = randomYesNoUndefined();
  returnBase.supplierCodeOfConduct = randomYesNoUndefined();
  returnBase.listOfProductionSites = generateArrayOfProductionSites();
  return returnBase;
}
