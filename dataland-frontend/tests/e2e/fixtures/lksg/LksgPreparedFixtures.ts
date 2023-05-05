import { FixtureData } from "@sharedUtils/Fixtures";
import { LksgData } from "@clients/backend";
import { generateLksgFixture, generateProductionSite, generateVatIdentificationNumber } from "./LksgDataFixtures";
import { randomPastDate } from "@e2e/fixtures/common/DateFixtures";
import { randomYesNo } from "@e2e/fixtures/common/YesNoFixtures";
import { faker } from "@faker-js/faker";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { assertDefined } from "@/utils/TypeScriptUtils";

type generatorFunction = (input: FixtureData<LksgData>) => FixtureData<LksgData>;

/**
 * Generates LkSG prepared fixtures by generating random LkSG datasets and afterwards manipulating some fields
 * via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateLksgPreparedFixtures(): Array<FixtureData<LksgData>> {
  const manipulatorFunctions: Array<generatorFunction> = [
    manipulateFixtureForSixLksgDataSetsInDifferentYears,
    manipulateFixtureForOneLksgDataSetWithProductionSites,
    manipulateFixtureForVat20231,
    manipulateFixtureForVat20232,
    manipulateFixtureForVat2022,
    manipulateFixtureToContainEveryField,
  ];
  const preparedFixturesBeforeManipulation = generateLksgFixture(manipulatorFunctions.length);
  const preparedFixtures = [];
  for (let i = 0; i < manipulatorFunctions.length; i++) {
    preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
  }
  return preparedFixtures;
}

/**
 * Sets the company name and the date in the fixture data to a specific string
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForSixLksgDataSetsInDifferentYears(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "six-lksg-data-sets-in-different-years";
  input.t.social!.general!.dataDate = "2022-01-01";
  input.reportingPeriod = "2022";
  return input;
}

/**
 * Sets the company name in the fixture data to a specific string, the field "employeeUnder18Apprentices" to "No", and
 * sets exactly two production sites for the "listOfProductionSites" field.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForOneLksgDataSetWithProductionSites(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "one-lksg-data-set";
  input.t.social!.childLabour!.employeeUnder18Apprentices = "No";
  input.t.social!.general!.listOfProductionSites = [generateProductionSite(), generateProductionSite()];
  return input;
}

/**
 * Sets the company name, vat identification number, data date and reporting period in the fixture data to
 * specific values needed for tests.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForVat20231(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "vat-2023-1";
  input.t.social!.general!.vatIdentificationNumber = "2023-1";
  input.t.social!.general!.dataDate = "2023-04-18";
  input.reportingPeriod = "2023";
  return input;
}

/**
 * Sets the company name, vat identification number, data date and reporting period in the fixture data to
 * specific values needed for tests.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForVat20232(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "vat-2023-2";
  input.t.social!.general!.vatIdentificationNumber = "2023-2";
  input.t.social!.general!.dataDate = "2023-06-22";
  input.reportingPeriod = "2023";
  return input;
}

/**
 * Sets the company name, vat identification number, data date and reporting period in the fixture data to
 * specific values needed for tests.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureForVat2022(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "vat-2022";
  input.t.social!.general!.vatIdentificationNumber = "2022";
  input.t.social!.general!.dataDate = "2022-07-30";
  input.reportingPeriod = "2022";
  return input;
}

/**
 * Generates a new LKSG fixture without any undefined values
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureToContainEveryField(input: FixtureData<LksgData>): FixtureData<LksgData> {
  input.companyInformation.companyName = "lksg-all-fields";
  input.t = {
    social: {
      general: {
        dataDate: randomPastDate(),
        lksgInScope: randomYesNo(),
        vatIdentificationNumber: generateVatIdentificationNumber(),
        numberOfEmployees: faker.datatype.number({ min: 1000, max: 200000 }),
        shareOfTemporaryWorkers: faker.datatype.number({ min: 0, max: 30, precision: 0.01 }),
        totalRevenue: faker.datatype.number({ min: 10000000, max: 100000000000 }),
        totalRevenueCurrency: generateIso4217CurrencyCode(),
        listOfProductionSites: [generateProductionSite(), generateProductionSite()],
      },
      grievanceMechanism: {
        grievanceHandlingMechanism: randomYesNo(),
        grievanceHandlingMechanismUsedForReporting: randomYesNo(),
        legalProceedings: randomYesNo(),
      },
      childLabour: {
        employeeUnder18: randomYesNo(),
        employeeUnder15: randomYesNo(),
        employeeUnder18Apprentices: randomYesNo(),
        employmentUnderLocalMinimumAgePrevention: randomYesNo(),
        employmentUnderLocalMinimumAgePreventionEmploymentContracts: randomYesNo(),
        employmentUnderLocalMinimumAgePreventionJobDescription: randomYesNo(),
        employmentUnderLocalMinimumAgePreventionIdentityDocuments: randomYesNo(),
        employmentUnderLocalMinimumAgePreventionTraining: randomYesNo(),
        employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: randomYesNo(),
      },
      forcedLabourSlaveryAndDebtBondage: {
        forcedLabourAndSlaveryPrevention: randomYesNo(),
        forcedLabourAndSlaveryPreventionEmploymentContracts: randomYesNo(),
        forcedLabourAndSlaveryPreventionIdentityDocuments: randomYesNo(),
        forcedLabourAndSlaveryPreventionFreeMovement: randomYesNo(),
        forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets: randomYesNo(),
        forcedLabourAndSlaveryPreventionTraining: randomYesNo(),
        documentedWorkingHoursAndWages: randomYesNo(),
        adequateLivingWage: randomYesNo(),
        regularWagesProcessFlow: randomYesNo(),
        fixedHourlyWages: randomYesNo(),
      },
      osh: {
        oshMonitoring: randomYesNo(),
        oshPolicy: randomYesNo(),
        oshPolicyPersonalProtectiveEquipment: randomYesNo(),
        oshPolicyMachineSafety: randomYesNo(),
        oshPolicyDisasterBehaviouralResponse: randomYesNo(),
        oshPolicyAccidentsBehaviouralResponse: randomYesNo(),
        oshPolicyWorkplaceErgonomics: randomYesNo(),
        oshPolicyHandlingChemicalsAndOtherHazardousSubstances: randomYesNo(),
        oshPolicyFireProtection: randomYesNo(),
        oshPolicyWorkingHours: randomYesNo(),
        oshPolicyTrainingAddressed: randomYesNo(),
        oshPolicyTraining: randomYesNo(),
        oshManagementSystem: randomYesNo(),
        oshManagementSystemInternationalCertification: randomYesNo(),
        oshManagementSystemNationalCertification: randomYesNo(),
        workplaceAccidentsUnder10: randomYesNo(),
        oshTraining: randomYesNo(),
      },
      freedomOfAssociation: {
        freedomOfAssociation: randomYesNo(),
        discriminationForTradeUnionMembers: randomYesNo(),
        freedomOfOperationForTradeUnion: randomYesNo(),
        freedomOfAssociationTraining: randomYesNo(),
        worksCouncil: randomYesNo(),
      },
      humanRights: {
        diversityAndInclusionRole: randomYesNo(),
        preventionOfMistreatments: randomYesNo(),
        equalOpportunitiesOfficer: randomYesNo(),
        riskOfHarmfulPollution: randomYesNo(),
        unlawfulEvictionAndTakingOfLand: randomYesNo(),
        useOfPrivatePublicSecurityForces: randomYesNo(),
        useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: randomYesNo(),
      },
      evidenceCertificatesAndAttestations: {
        iso26000: randomYesNo(),
        sa8000Certification: randomYesNo(),
        smetaSocialAuditConcept: randomYesNo(),
        betterWorkProgramCertificate: randomYesNo(),
        iso45001Certification: randomYesNo(),
        iso14000Certification: randomYesNo(),
        emasCertification: randomYesNo(),
        iso37001Certification: randomYesNo(),
        iso37301Certification: randomYesNo(),
        riskManagementSystemCertification: randomYesNo(),
        amforiBsciAuditReport: randomYesNo(),
        initiativeClauseSocialCertification: randomYesNo(),
        responsibleBusinessAssociationCertification: randomYesNo(),
        fairLabourAssociationCertification: randomYesNo(),
        fairWorkingConditionsPolicy: randomYesNo(),
        fairAndEthicalRecruitmentPolicy: randomYesNo(),
        equalOpportunitiesAndNondiscriminationPolicy: randomYesNo(),
        healthAndSafetyPolicy: randomYesNo(),
        complaintsAndGrievancesPolicy: randomYesNo(),
        forcedLabourPolicy: randomYesNo(),
        childLabourPolicy: randomYesNo(),
        environmentalImpactPolicy: randomYesNo(),
        supplierCodeOfConduct: randomYesNo(),
      },
    },
    governance: {
      socialAndEmployeeMatters: {
        responsibilitiesForFairWorkingConditions: randomYesNo(),
      },
      environment: {
        responsibilitiesForTheEnvironment: randomYesNo(),
      },
      osh: {
        responsibilitiesForOccupationalSafety: randomYesNo(),
      },
      riskManagement: {
        riskManagementSystem: randomYesNo(),
      },
      codeOfConduct: {
        codeOfConduct: randomYesNo(),
        codeOfConductRiskManagementTopics: randomYesNo(),
        codeOfConductTraining: randomYesNo(),
      },
    },
    environmental: {
      waste: {
        mercuryAndMercuryWasteHandling: randomYesNo(),
        mercuryAndMercuryWasteHandlingPolicy: randomYesNo(),
        chemicalHandling: randomYesNo(),
        environmentalManagementSystem: randomYesNo(),
        environmentalManagementSystemInternationalCertification: randomYesNo(),
        environmentalManagementSystemNationalCertification: randomYesNo(),
        legalRestrictedWaste: randomYesNo(),
        legalRestrictedWasteProcesses: randomYesNo(),
        mercuryAddedProductsHandling: randomYesNo(),
        mercuryAddedProductsHandlingRiskOfExposure: randomYesNo(),
        mercuryAddedProductsHandlingRiskOfDisposal: randomYesNo(),
        mercuryAndMercuryCompoundsProductionAndUse: randomYesNo(),
        mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: randomYesNo(),
        persistentOrganicPollutantsProductionAndUse: randomYesNo(),
        persistentOrganicPollutantsProductionAndUseRiskOfExposure: randomYesNo(),
        persistentOrganicPollutantsProductionAndUseRiskOfDisposal: randomYesNo(),
        persistentOrganicPollutantsProductionAndUseTransboundaryMovements: randomYesNo(),
        persistentOrganicPollutantsProductionAndUseRiskForImportingState: randomYesNo(),
        hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein: randomYesNo(),
        hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein: randomYesNo(),
        hazardousWasteDisposal: randomYesNo(),
        hazardousWasteDisposalRiskOfImport: randomYesNo(),
        hazardousAndOtherWasteImport: randomYesNo(),
      },
    },
  };
  input.reportingPeriod = assertDefined(input.t.social?.general?.dataDate).substring(0, 4);
  return input;
}
