import { faker } from "@faker-js/faker";
import { LKSGData, ProductionSite } from "../../../../build/clients/backend";

import { randomYesNoUndefined } from "../common/YesNoFixtures";

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
  const fakeGoodsAndServices = Array.from(
    { length: faker.datatype.number({ min: 1, max: 5 }) },
    faker.commerce.productName
  );

  return {
    name: fakeSiteName,
    isInHouseProductionOrIsContractProcessing: yesNo,
    address: fullFormattedFakeAddress,
    listOfGoodsAndServices: fakeGoodsAndServices,
  };
}

export function generateArrayOfProductionSites(): ProductionSite[] {
  return Array.from({ length: faker.datatype.number({ min: 1, max: 50 }) }, generateProductionSite);
}

function addZeroIfOneCharacterString(inputString: string): string {
  let formattedString;
  if (inputString.length === 1) {
    formattedString = "0" + inputString;
  } else {
    formattedString = inputString;
  }
  return formattedString;
}

export function generateDataDate(): string {
  const fakeFutureDate = faker.date.future(1);
  console.log(fakeFutureDate);
  const fakeYear = fakeFutureDate.getFullYear();
  const fakeMonth = addZeroIfOneCharacterString(fakeFutureDate.toLocaleDateString().split(".")[1]);
  const fakeDay = addZeroIfOneCharacterString(fakeFutureDate.toLocaleDateString().split(".")[0]);
  return fakeYear + "-" + fakeMonth + "-" + fakeDay;
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

export function generateIso4217CurrencyCode() {
  const someCommonIso4217CurrencyCodes = ["USD", "EUR", "CHF", "CAD", "AUD"];
  return someCommonIso4217CurrencyCodes[Math.floor(Math.random() * someCommonIso4217CurrencyCodes.length)];
}

export function generateLKSGData(): LKSGData {
  const returnBase: LKSGData = {};

  returnBase.betterWorkProgramCertificate = randomYesNoUndefined();
  returnBase.dataDate = generateDataDate();
  returnBase.companyLegalForm = getCompanyLegalForm();
  returnBase.vatidentificationNumber = generateVatIdentificationNumber();
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
  returnBase.amforiBSCIAuditReport = randomYesNoUndefined();
  returnBase.initiativeClauseSocialCertification = randomYesNoUndefined();
  returnBase.responsibleBusinessAssociationCertification = randomYesNoUndefined();
  returnBase.fairLabourAssociationCertification = randomYesNoUndefined();
  returnBase.lksginScope = randomYesNoUndefined();
  returnBase.oshmonitoring = randomYesNoUndefined();
  returnBase.oshpolicy = randomYesNoUndefined();
  returnBase.oshtraining = randomYesNoUndefined();
  returnBase.iso26000 = randomYesNoUndefined();
  returnBase.smetasocialAuditConcept = randomYesNoUndefined();
  returnBase.iso45001certification = randomYesNoUndefined();
  returnBase.iso14000certification = randomYesNoUndefined();
  returnBase.emascertification = randomYesNoUndefined();
  returnBase.sa8000certification = randomYesNoUndefined();
  returnBase.iso37001certification = randomYesNoUndefined();
  returnBase.iso37301certification = randomYesNoUndefined();
  returnBase.oshpolicyMachineSafety = randomYesNoUndefined();
  returnBase.oshpolicyFireProtection = randomYesNoUndefined();
  returnBase.oshpolicyWorkingHours = randomYesNoUndefined();
  returnBase.oshmanagementSystem = randomYesNoUndefined();
  returnBase.oshpolicyWorkplaceErgonomics = randomYesNoUndefined();
  returnBase.oshpolicyTrainingAddressed = randomYesNoUndefined();
  returnBase.oshpolicyTraining = randomYesNoUndefined();
  returnBase.oshpolicyPersonalProtectiveEquipment = randomYesNoUndefined();
  returnBase.oshpolicyAccidentsBehaviouralResponse = randomYesNoUndefined();
  returnBase.oshmanagementSystemInternationalCertification = randomYesNoUndefined();
  returnBase.oshpolicyDisasterBehaviouralResponse = randomYesNoUndefined();
  returnBase.oshmanagementSystemNationalCertification = randomYesNoUndefined();
  returnBase.oshpolicyHandlingChemicalsAndOtherHazardousSubstances = randomYesNoUndefined();
  returnBase.listOfProductionSites = generateArrayOfProductionSites();
  return returnBase;
}
