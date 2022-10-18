import { faker } from "@faker-js/faker";
import { LKSGData, ProductionSite } from "../../../../build/clients/backend";
//import { FixtureData } from "../FixtureUtils";

//import { getCsvCompanyMapping } from "../CompanyFixtures";
import { randomYesNoUndefined } from "../common/YesNoFixtures";

//const { parse } = require("json2csv");

export function generateProductionSite(): ProductionSite {
  const fakeSiteAddress = faker.address;
  const siteLocation = fakeSiteAddress.city();
  const yesNo = randomYesNoUndefined();
  const fullFormattedAddress =
    fakeSiteAddress.street() +
    " " +
    fakeSiteAddress.buildingNumber() +
    ", " +
    fakeSiteAddress.zipCode() +
    " " +
    siteLocation +
    ", " +
    fakeSiteAddress.country();
  return {
    location: siteLocation,
    isInHouseProductionOrIsContractProcessing: yesNo,
    addressOfProductionSite: fullFormattedAddress,
  };
}

export function generateDataDate(): string {
  const fakeFutureDate = faker.date.future(1);
  const fakeYear = fakeFutureDate.getFullYear();
  const fakeMonth = fakeFutureDate.toLocaleString().split(".")[1];
  const fakeDay = fakeFutureDate.toLocaleString().split(".")[0];
  return fakeYear + "-" + fakeMonth + "-" + fakeDay;
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
  ];
  return legalForms[Math.floor(Math.random() * legalForms.length)];
}

export function generateLKSGproductionSites(): ProductionSite[] {
  const productionSite = generateProductionSite();
  return Array.of(productionSite);
}

export function generateLKSGData(): LKSGData {
  const returnBase: LKSGData = {};

  returnBase.betterWorkProgramCertificate = randomYesNoUndefined();
  returnBase.dataDate = generateDataDate();
  returnBase.companyLegalForm = getCompanyLegalForm();
  returnBase.vatidentificationNumber = faker.datatype.string(); //TODO stay close to a realisitc format
  returnBase.numberOfEmployees = faker.datatype.number({ min: 1000, max: 200000 });
  returnBase.shareOfTemporaryWorkers = faker.datatype.number({ min: 0, max: 100 });
  returnBase.totalRevenue = faker.datatype.number({ min: 10, max: 500000 });
  returnBase.totalRevenueCurrency = faker.datatype.string(); //TODO we need to fake a valid currency so that we won't have problems in the Frontend => data dicitonary says whe should use ISO 4217 currency codes
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
  returnBase.listOfGoodsOrServices = [faker.datatype.string()]; //TODO needs to be comma-seperated values, so that we can work with it in our frontend
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
  returnBase.listOfProductionSites = generateLKSGproductionSites();
  return returnBase;
}

/*
export function generateCSVLKSGData(companyInformationWithLKSGdata: Array<FixtureData<LKSGData>>) {
  const options = {
    fields: [...getCsvCompanyMapping<LKSGData>(), ...generateLKSGData()],
    delimiter: ";",
  };
  return parse(companyInformationWithLKSGdata, options);
}*/
