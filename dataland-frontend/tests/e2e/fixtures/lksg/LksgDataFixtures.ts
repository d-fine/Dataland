import { faker } from "@faker-js/faker";
import { InHouseProductionOrContractProcessing, LksgData, ProductionSite } from "@clients/backend";
import { randomYesNo, randomYesNoUndefined } from "@e2e/fixtures/common/YesNoFixtures";
import { randomFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { getRandomReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";

/**
 * Generates a set number of LKSG fixtures
 *
 * @param numFixtures the number of lksg fixtures to generate
 * @returns a set number of LKSG fixtures
 */
export function generateLksgFixture(numFixtures: number): FixtureData<LksgData>[] {
  return generateFixtureDataset<LksgData>(
    generateLksgData,
    numFixtures,
    (dataSet) => dataSet?.social?.general?.dataDate?.substring(0, 4) || getRandomReportingPeriod()
  );
}

/**
 * Generates a random production site
 *
 * @returns a random production site
 */
export function generateProductionSite(): ProductionSite {
  const fakeGoodsOrServices = Array.from({ length: faker.datatype.number({ min: 0, max: 5 }) }, () => {
    return faker.commerce.productName();
  });

  return {
    name: faker.company.name(),
    isInHouseProductionOrIsContractProcessing: faker.helpers.arrayElement([
      InHouseProductionOrContractProcessing.InHouseProduction,
      InHouseProductionOrContractProcessing.ContractProcessing,
    ]),
    country: faker.address.countryCode(),
    city: faker.address.city(),
    streetAndHouseNumber: faker.address.street() + " " + faker.address.buildingNumber(),
    postalCode: faker.address.zipCode(),
    listOfGoodsOrServices: fakeGoodsOrServices,
  };
}

/**
 * Generates an array consisting of 1 to 5 random production sites
 *
 * @returns 1 to 5 random production sites
 */
export function generateArrayOfProductionSites(): ProductionSite[] {
  return Array.from({ length: faker.datatype.number({ min: 0, max: 5 }) }, generateProductionSite);
}

/**
 * Generates a random VAT ID number
 *
 * @returns a random VAT ID number
 */
export function generateVatIdentificationNumber(): string {
  const fakeCountryCode = faker.address.countryCode();
  const randomNineDigitNumber = faker.random.numeric(9);
  return fakeCountryCode + randomNineDigitNumber.toString();
}
/**
 * Generates a random LKSG dataset
 *
 * @param dataDate Optional parameter if a specific date should be set instead of a random one
 * @returns a random LKSG dataset
 */
export function generateLksgData(dataDate?: string): LksgData {
  return {
    social: {
      general: {
        dataDate: dataDate === undefined ? randomFutureDate() : dataDate,
        lksgInScope: randomYesNo(),
        vatIdentificationNumber: generateVatIdentificationNumber(),
        numberOfEmployees: faker.datatype.number({ min: 1000, max: 200000 }),
        shareOfTemporaryWorkers: faker.datatype.number({ min: 0, max: 30, precision: 0.01 }),
        totalRevenue: faker.datatype.float({ min: 10000000, max: 100000000000 }),
        totalRevenueCurrency: generateIso4217CurrencyCode(),
        listOfProductionSites: generateArrayOfProductionSites(),
      },
      grievanceMechanism: {
        grievanceHandlingMechanism: randomYesNoUndefined(),
        grievanceHandlingMechanismUsedForReporting: randomYesNoUndefined(),
        legalProceedings: randomYesNoUndefined(),
      },
      childLabour: {
        employeeUnder18: randomYesNoUndefined(),
        employeeUnder15: randomYesNoUndefined(),
        employeeUnder18Apprentices: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePrevention: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePreventionEmploymentContracts: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePreventionJobDescription: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePreventionIdentityDocuments: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePreventionTraining: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: randomYesNoUndefined(),
      },
      forcedLabourSlaveryAndDebtBondage: {
        forcedLabourAndSlaveryPrevention: randomYesNoUndefined(),
        forcedLabourAndSlaveryPreventionEmploymentContracts: randomYesNoUndefined(),
        forcedLabourAndSlaveryPreventionIdentityDocuments: randomYesNoUndefined(),
        forcedLabourAndSlaveryPreventionFreeMovement: randomYesNoUndefined(),
        forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets: randomYesNoUndefined(),
        forcedLabourAndSlaveryPreventionTraining: randomYesNoUndefined(),
        documentedWorkingHoursAndWages: randomYesNoUndefined(),
        adequateLivingWage: randomYesNoUndefined(),
        regularWagesProcessFlow: randomYesNoUndefined(),
        fixedHourlyWages: randomYesNoUndefined(),
      },
      osh: {
        oshMonitoring: randomYesNoUndefined(),
        oshPolicy: randomYesNoUndefined(),
        oshPolicyPersonalProtectiveEquipment: randomYesNoUndefined(),
        oshPolicyMachineSafety: randomYesNoUndefined(),
        oshPolicyDisasterBehaviouralResponse: randomYesNoUndefined(),
        oshPolicyAccidentsBehaviouralResponse: randomYesNoUndefined(),
        oshPolicyWorkplaceErgonomics: randomYesNoUndefined(),
        oshPolicyHandlingChemicalsAndOtherHazardousSubstances: randomYesNoUndefined(),
        oshPolicyFireProtection: randomYesNoUndefined(),
        oshPolicyWorkingHours: randomYesNoUndefined(),
        oshPolicyTrainingAddressed: randomYesNoUndefined(),
        oshPolicyTraining: randomYesNoUndefined(),
        oshManagementSystem: randomYesNoUndefined(),
        oshManagementSystemInternationalCertification: randomYesNoUndefined(),
        oshManagementSystemNationalCertification: randomYesNoUndefined(),
        workplaceAccidentsUnder10: randomYesNoUndefined(),
        oshTraining: randomYesNoUndefined(),
      },
      freedomOfAssociation: {
        freedomOfAssociation: randomYesNoUndefined(),
        discriminationForTradeUnionMembers: randomYesNoUndefined(),
        freedomOfOperationForTradeUnion: randomYesNoUndefined(),
        freedomOfAssociationTraining: randomYesNoUndefined(),
        worksCouncil: randomYesNoUndefined(),
      },
      humanRights: {
        diversityAndInclusionRole: randomYesNoUndefined(),
        preventionOfMistreatments: randomYesNoUndefined(),
        equalOpportunitiesOfficer: randomYesNoUndefined(),
        riskOfHarmfulPollution: randomYesNoUndefined(),
        unlawfulEvictionAndTakingOfLand: randomYesNoUndefined(),
        useOfPrivatePublicSecurityForces: randomYesNoUndefined(),
        useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: randomYesNoUndefined(),
      },
      evidenceCertificatesAndAttestations: {
        iso26000: randomYesNoUndefined(),
        sa8000Certification: randomYesNoUndefined(),
        smetaSocialAuditConcept: randomYesNoUndefined(),
        betterWorkProgramCertificate: randomYesNoUndefined(),
        iso45001Certification: randomYesNoUndefined(),
        iso14000Certification: randomYesNoUndefined(),
        emasCertification: randomYesNoUndefined(),
        iso37001Certification: randomYesNoUndefined(),
        iso37301Certification: randomYesNoUndefined(),
        riskManagementSystemCertification: randomYesNoUndefined(),
        amforiBsciAuditReport: randomYesNoUndefined(),
        initiativeClauseSocialCertification: randomYesNoUndefined(),
        responsibleBusinessAssociationCertification: randomYesNoUndefined(),
        fairLabourAssociationCertification: randomYesNoUndefined(),
        fairWorkingConditionsPolicy: randomYesNoUndefined(),
        fairAndEthicalRecruitmentPolicy: randomYesNoUndefined(),
        equalOpportunitiesAndNondiscriminationPolicy: randomYesNoUndefined(),
        healthAndSafetyPolicy: randomYesNoUndefined(),
        complaintsAndGrievancesPolicy: randomYesNoUndefined(),
        forcedLabourPolicy: randomYesNoUndefined(),
        childLabourPolicy: randomYesNoUndefined(),
        environmentalImpactPolicy: randomYesNoUndefined(),
        supplierCodeOfConduct: randomYesNoUndefined(),
      },
    },
    governance: {
      socialAndEmployeeMatters: {
        responsibilitiesForFairWorkingConditions: randomYesNoUndefined(),
      },
      environment: {
        responsibilitiesForTheEnvironment: randomYesNoUndefined(),
      },
      osh: {
        responsibilitiesForOccupationalSafety: randomYesNoUndefined(),
      },
      riskManagement: {
        riskManagementSystem: randomYesNoUndefined(),
      },
      codeOfConduct: {
        codeOfConduct: randomYesNoUndefined(),
        codeOfConductRiskManagementTopics: randomYesNoUndefined(),
        codeOfConductTraining: randomYesNoUndefined(),
      },
    },
    environmental: {
      waste: {
        mercuryAndMercuryWasteHandling: randomYesNoUndefined(),
        mercuryAndMercuryWasteHandlingPolicy: randomYesNoUndefined(),
        chemicalHandling: randomYesNoUndefined(),
        environmentalManagementSystem: randomYesNoUndefined(),
        environmentalManagementSystemInternationalCertification: randomYesNoUndefined(),
        environmentalManagementSystemNationalCertification: randomYesNoUndefined(),
        legalRestrictedWaste: randomYesNoUndefined(),
        legalRestrictedWasteProcesses: randomYesNoUndefined(),
        mercuryAddedProductsHandling: randomYesNoUndefined(),
        mercuryAddedProductsHandlingRiskOfExposure: randomYesNoUndefined(),
        mercuryAddedProductsHandlingRiskOfDisposal: randomYesNoUndefined(),
        mercuryAndMercuryCompoundsProductionAndUse: randomYesNoUndefined(),
        mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: randomYesNoUndefined(),
        persistentOrganicPollutantsProductionAndUse: randomYesNoUndefined(),
        persistentOrganicPollutantsProductionAndUseRiskOfExposure: randomYesNoUndefined(),
        persistentOrganicPollutantsProductionAndUseRiskOfDisposal: randomYesNoUndefined(),
        persistentOrganicPollutantsProductionAndUseTransboundaryMovements: randomYesNoUndefined(),
        persistentOrganicPollutantsProductionAndUseRiskForImportingState: randomYesNoUndefined(),
        hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein: randomYesNoUndefined(),
        hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein: randomYesNoUndefined(),
        hazardousWasteDisposal: randomYesNoUndefined(),
        hazardousWasteDisposalRiskOfImport: randomYesNoUndefined(),
        hazardousAndOtherWasteImport: randomYesNoUndefined(),
      },
    },
  };
}
