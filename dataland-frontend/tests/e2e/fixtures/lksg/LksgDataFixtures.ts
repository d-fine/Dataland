import { faker } from "@faker-js/faker";
import {
  LksgAddress,
  LksgData,
  LksgProductionSite,
  NationalOrInternationalMarket,
  ShareOfTemporaryWorkers,
} from "@clients/backend";
import { randomYesNo, randomYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { getRandomReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
import { randomEuroValue, randomNumber, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { generateIso2CountryCode } from "@e2e/fixtures/common/CountryFixtures";
import { randomPastDate } from "@e2e/fixtures/common/DateFixtures";

/**
 * Generates a set number of LKSG fixtures
 * @param numFixtures the number of lksg fixtures to generate
 * @param undefinedRatio the ratio of fields to be undefined (number between 0 and 1)
 * @returns a set number of LKSG fixtures
 */
export function generateLksgFixture(numFixtures: number, undefinedRatio = 0.5): FixtureData<LksgData>[] {
  return generateFixtureDataset<LksgData>(
    () => generateLksgData(undefinedRatio),
    numFixtures,
    (dataSet) => dataSet?.general?.masterData?.dataDate?.substring(0, 4) || getRandomReportingPeriod()
  );
}

/**
 * Generates a random production site
 * @param undefinedProbability the percentage of undefined values in the returned production site
 * @returns a random production site
 */
export function generateProductionSite(undefinedProbability = 0.5): LksgProductionSite {
  return {
    nameOfProductionSite: valueOrUndefined(faker.company.name(), undefinedProbability),
    addressOfProductionSite: valueOrUndefined(generateAddress(), undefinedProbability),
    listOfGoodsOrServices: valueOrUndefined(generateListOfGoodsOrServices(), undefinedProbability),
  };
}

/**
 * Generates an array consisting of 1 to 5 random production sites
 * @returns 1 to 5 random production sites
 */
export function generateArrayOfProductionSites(): LksgProductionSite[] {
  return Array.from({ length: faker.datatype.number({ min: 0, max: 5 }) }, generateProductionSite);
}

/**
 * Generates a random list of goods or services
 * @returns random list of goods or services
 */
export function generateListOfGoodsOrServices(): string[] {
  return Array.from({ length: faker.datatype.number({ min: 0, max: 5 }) }, () => {
    return faker.commerce.productName();
  });
}

/**
 * Generates a random VAT ID number
 * @returns a random VAT ID number
 */
export function generateVatIdentificationNumber(): string {
  const fakeCountryCode = faker.address.countryCode();
  const randomNineDigitNumber = faker.random.numeric(9);
  return fakeCountryCode + randomNineDigitNumber.toString();
}

/**
 * Generates a random address
 * @returns a random address
 */
export function generateAddress(): LksgAddress {
  return {
    streetAndHouseNumber: faker.address.street() + " " + faker.address.buildingNumber(),
    city: faker.address.city(),
    state: faker.address.state(),
    postalCode: faker.address.zipCode(),
    country: faker.address.country(),
  };
}

/**
 * Randomly returns <10%, 10-25%, 25-50% or >50%
 * @returns one of the four percentage intervals as string
 */
export function randomShareOfTemporaryWorkersInterval(): ShareOfTemporaryWorkers {
  return faker.helpers.arrayElement(Object.values(ShareOfTemporaryWorkers));
}

/**
 * Generates a random LKSG dataset
 * @param undefinedProbability the ratio of fields to be undefined (number between 0 and 1)
 * @returns a random LKSG dataset
 */
export function generateLksgData(undefinedProbability = 0.5): LksgData {
  return {
    general: {
      masterData: {
        dataDate: randomPastDate(),
        headOffice: valueOrUndefined(randomYesNo(), undefinedProbability),
        groupOfCompanies: valueOrUndefined(randomYesNo(), undefinedProbability),
        groupOfCompaniesName: valueOrUndefined(faker.company.name(), undefinedProbability),
        industry: valueOrUndefined(faker.name.jobArea(), undefinedProbability),
        numberOfEmployees: valueOrUndefined(randomNumber(10000), undefinedProbability),
        seasonalOrMigrantWorkers: valueOrUndefined(randomYesNo(), undefinedProbability),
        shareOfTemporaryWorkers: valueOrUndefined(randomShareOfTemporaryWorkersInterval(), undefinedProbability),
        totalRevenueCurrency: valueOrUndefined(generateIso4217CurrencyCode(), undefinedProbability),
        totalRevenue: valueOrUndefined(randomEuroValue(), undefinedProbability),
        fixedAndWorkingCapital: valueOrUndefined(randomNumber(10000000), undefinedProbability),
      },
      productionSpecific: {
        manufacturingCompany: valueOrUndefined(randomYesNo(), undefinedProbability),
        capacity: valueOrUndefined(randomNumber(100000), undefinedProbability),
        isContractProcessing: valueOrUndefined(randomYesNo(), undefinedProbability),
        subcontractingCompaniesCountries: valueOrUndefined(generateIso2CountryCode(), undefinedProbability),
        subcontractingCompaniesIndustries: valueOrUndefined(faker.name.jobArea(), undefinedProbability),
        productionSites: valueOrUndefined(randomYesNo(), undefinedProbability),
        listOfProductionSites: valueOrUndefined(generateArrayOfProductionSites(), undefinedProbability),
        market: valueOrUndefined(
          faker.helpers.arrayElement([
            NationalOrInternationalMarket.National,
            NationalOrInternationalMarket.International,
            NationalOrInternationalMarket.Both,
          ]),
          undefinedProbability
        ),
        specificProcurement: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
    },
    governance: {
      riskManagementOwnOperations: {
        adequateAndEffectiveRiskManagementSystem: valueOrUndefined(randomYesNo(), undefinedProbability),
        riskManagementSystemFiscalYear: valueOrUndefined(randomYesNo(), undefinedProbability),
        riskManagementSystemRisks: valueOrUndefined(randomYesNo(), undefinedProbability),
        riskManagementSystemIdentifiedRisks: valueOrUndefined(faker.commerce.productAdjective(), undefinedProbability),
        riskManagementSystemCounteract: valueOrUndefined(randomYesNo(), undefinedProbability),
        riskManagementSystemMeasures: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        riskManagementSystemResponsibility: valueOrUndefined(randomYesNo(), undefinedProbability),
        environmentalManagementSystem: valueOrUndefined(randomYesNo(), undefinedProbability),
        environmentalManagementSystemInternationalCertification: valueOrUndefined(randomYesNo(), undefinedProbability),
        environmentalManagementSystemNationalCertification: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      grievanceMechanismOwnOperations: {
        grievanceHandlingMechanism: valueOrUndefined(randomYesNo(), undefinedProbability),
        grievanceHandlingMechanismUsedForReporting: valueOrUndefined(randomYesNo(), undefinedProbability),
        grievanceMechanismInformationProvided: valueOrUndefined(randomYesNo(), undefinedProbability),
        grievanceMechanismSupportProvided: valueOrUndefined(randomYesNo(), undefinedProbability),
        grievanceMechanismAccessToExpertise: valueOrUndefined(randomYesNo(), undefinedProbability),
        grievanceMechanismComplaints: valueOrUndefined(randomYesNo(), undefinedProbability),
        grievanceMechanismComplaintsNumber: valueOrUndefined(randomNumber(100), undefinedProbability),
        grievanceMechanismComplaintsReason: valueOrUndefined(faker.company.catchPhraseNoun(), undefinedProbability),
        grievanceMechanismComplaintsAction: valueOrUndefined(randomYesNo(), undefinedProbability),
        grievanceMechanismComplaintsActionUndertaken: valueOrUndefined(
          faker.company.catchPhraseNoun(),
          undefinedProbability
        ),
        grievanceMechanismPublicAccess: valueOrUndefined(randomYesNo(), undefinedProbability),
        grievanceMechanismProtection: valueOrUndefined(randomYesNo(), undefinedProbability),
        grievanceMechanismDueDiligenceProcess: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      certificationsPoliciesAndResponsibilities: {
        sa8000Certification: valueOrUndefined(randomYesNo(), undefinedProbability),
        smetaSocialAuditConcept: valueOrUndefined(randomYesNo(), undefinedProbability),
        betterWorkProgramCertificate: valueOrUndefined(randomYesNo(), undefinedProbability),
        iso45001Certification: valueOrUndefined(randomYesNo(), undefinedProbability),
        iso14000Certification: valueOrUndefined(randomYesNo(), undefinedProbability),
        emasCertification: valueOrUndefined(randomYesNo(), undefinedProbability),
        iso37001Certification: valueOrUndefined(randomYesNo(), undefinedProbability),
        iso37301Certification: valueOrUndefined(randomYesNo(), undefinedProbability),
        riskManagementSystemCertification: valueOrUndefined(randomYesNo(), undefinedProbability),
        amforiBsciAuditReport: valueOrUndefined(randomYesNo(), undefinedProbability),
        responsibleBusinessAssociationCertification: valueOrUndefined(randomYesNo(), undefinedProbability),
        fairLaborAssociationCertification: valueOrUndefined(randomYesNo(), undefinedProbability),
        additionalAudits: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        codeOfConduct: valueOrUndefined(randomYesNo(), undefinedProbability),
        codeOfConductTraining: valueOrUndefined(randomYesNo(), undefinedProbability),
        supplierCodeOfConduct: valueOrUndefined(randomYesNo(), undefinedProbability),
        policyStatement: valueOrUndefined(randomYesNo(), undefinedProbability),
        humanRightsStrategy: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        environmentalImpactPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
        fairWorkingConditionsPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      generalViolations: {
        responsibilitiesForFairWorkingConditions: valueOrUndefined(randomYesNo(), undefinedProbability),
        responsibilitiesForTheEnvironment: valueOrUndefined(randomYesNo(), undefinedProbability),
        responsibilitiesForOccupationalSafety: valueOrUndefined(randomYesNo(), undefinedProbability),
        legalProceedings: valueOrUndefined(randomYesNo(), undefinedProbability),
        humanRightsViolation: valueOrUndefined(randomYesNo(), undefinedProbability),
        humanRightsViolationLocation: valueOrUndefined(faker.address.country(), undefinedProbability),
        humanRightsViolationAction: valueOrUndefined(randomYesNo(), undefinedProbability),
        humanRightsViolationActionMeasures: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        highRiskCountriesRawMaterials: valueOrUndefined(randomYesNo(), undefinedProbability),
        highRiskCountriesRawMaterialsLocation: valueOrUndefined(generateIso2CountryCode(), undefinedProbability),
        highRiskCountriesActivity: valueOrUndefined(randomYesNo(), undefinedProbability),
        highRiskCountries: valueOrUndefined(generateIso2CountryCode(), undefinedProbability),
        highRiskCountriesProcurement: valueOrUndefined(randomYesNo(), undefinedProbability),
        highRiskCountriesProcurementName: valueOrUndefined(generateIso2CountryCode(), undefinedProbability),
      },
    },
    social: {
      childLabor: {
        employeeUnder18: valueOrUndefined(randomYesNo(), undefinedProbability),
        employeeUnder18Under15: valueOrUndefined(randomYesNo(), undefinedProbability),
        employeeUnder18Apprentices: valueOrUndefined(randomYesNo(), undefinedProbability),
        worstFormsOfChildLabor: valueOrUndefined(randomYesNo(), undefinedProbability),
        worstFormsOfChildLaborForms: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        employmentUnderLocalMinimumAgePrevention: valueOrUndefined(randomYesNo(), undefinedProbability),
        employmentUnderLocalMinimumAgePreventionEmploymentContracts: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        employmentUnderLocalMinimumAgePreventionJobDescription: valueOrUndefined(randomYesNo(), undefinedProbability),
        employmentUnderLocalMinimumAgePreventionIdentityDocuments: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        employmentUnderLocalMinimumAgePreventionTraining: valueOrUndefined(randomYesNo(), undefinedProbability),
        employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        childLaborMeasures: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        childLaborPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      forcedLaborSlavery: {
        forcedLaborAndSlaveryPrevention: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionPractices: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        forcedLaborAndSlaveryPreventionMeasures: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionEmploymentContracts: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionIdentityDocuments: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionFreeMovement: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        forcedLaborAndSlaveryPreventionTraining: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryMeasures: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        forcedLaborPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      withholdingAdequateWages: {
        adequateWage: valueOrUndefined(randomYesNo(), undefinedProbability),
        adequateWageBeingWithheld: valueOrUndefined(randomYesNo(), undefinedProbability),
        documentedWorkingHoursAndWages: valueOrUndefined(randomYesNo(), undefinedProbability),
        adequateLivingWage: valueOrUndefined(randomYesNo(), undefinedProbability),
        regularWagesProcessFlow: valueOrUndefined(randomYesNo(), undefinedProbability),
        fixedHourlyWages: valueOrUndefined(randomYesNoNa(), undefinedProbability),
        fixedPieceworkWages: valueOrUndefined(randomYesNoNa(), undefinedProbability),
        adequateWageMeasures: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
      },
      disregardForOccupationalHealthSafety: {
        lowSkillWork: valueOrUndefined(randomYesNo(), undefinedProbability),
        hazardousMachines: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshPolicyPersonalProtectiveEquipment: valueOrUndefined(randomYesNoNa(), undefinedProbability),
        oshPolicyMachineSafety: valueOrUndefined(randomYesNoNa(), undefinedProbability),
        oshPolicyDisasterBehaviouralResponse: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshPolicyAccidentsBehaviouralResponse: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshPolicyWorkplaceErgonomics: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshPolicyAccessToWork: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshPolicyHandlingChemicalsAndOtherHazardousSubstances: valueOrUndefined(randomYesNoNa(), undefinedProbability),
        oshPolicyFireProtection: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshPolicyWorkingHours: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshPolicyTrainingAddressed: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshPolicyTraining: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshManagementSystem: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshManagementSystemInternationalCertification: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshManagementSystemNationalCertification: valueOrUndefined(randomYesNo(), undefinedProbability),
        workplaceAccidentsUnder10: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshTraining: valueOrUndefined(randomYesNo(), undefinedProbability),
        healthAndSafetyPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      disregardForFreedomOfAssociation: {
        freedomOfAssociation: valueOrUndefined(randomYesNo(), undefinedProbability),
        representedEmployees: randomPercentageValue(),
        discriminationForTradeUnionMembers: valueOrUndefined(randomYesNo(), undefinedProbability),
        freedomOfOperationForTradeUnion: valueOrUndefined(randomYesNo(), undefinedProbability),
        freedomOfAssociationTraining: valueOrUndefined(randomYesNo(), undefinedProbability),
        worksCouncil: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      unequalTreatmentOfEmployment: {
        unequalTreatmentOfEmployment: valueOrUndefined(randomYesNo(), undefinedProbability),
        diversityAndInclusionRole: valueOrUndefined(randomYesNo(), undefinedProbability),
        preventionOfMistreatments: valueOrUndefined(randomYesNo(), undefinedProbability),
        equalOpportunitiesOfficer: valueOrUndefined(randomYesNo(), undefinedProbability),
        fairAndEthicalRecruitmentPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
        equalOpportunitiesAndNondiscriminationPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption: {
        harmfulSoilChange: valueOrUndefined(randomYesNo(), undefinedProbability),
        soilDegradation: valueOrUndefined(randomYesNo(), undefinedProbability),
        soilErosion: valueOrUndefined(randomYesNo(), undefinedProbability),
        soilBornDiseases: valueOrUndefined(randomYesNo(), undefinedProbability),
        soilContamination: valueOrUndefined(randomYesNo(), undefinedProbability),
        soilSalinisation: valueOrUndefined(randomYesNo(), undefinedProbability),
        harmfulWaterPollution: valueOrUndefined(randomYesNo(), undefinedProbability),
        fertilisersOrPollutants: valueOrUndefined(randomYesNo(), undefinedProbability),
        wasteWaterFiltration: valueOrUndefined(randomYesNo(), undefinedProbability),
        harmfulAirPollution: valueOrUndefined(randomYesNo(), undefinedProbability),
        airFiltration: valueOrUndefined(randomYesNo(), undefinedProbability),
        harmfulNoiseEmission: valueOrUndefined(randomYesNo(), undefinedProbability),
        reduceNoiseEmissions: valueOrUndefined(randomYesNo(), undefinedProbability),
        excessiveWaterConsumption: valueOrUndefined(randomYesNo(), undefinedProbability),
        waterSavingMeasures: valueOrUndefined(randomYesNo(), undefinedProbability),
        waterSavingMeasuresName: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        pipeMaintaining: valueOrUndefined(randomYesNo(), undefinedProbability),
        waterSources: valueOrUndefined(randomYesNo(), undefinedProbability),
        contaminationMeasures: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
      },
      unlawfulEvictionDeprivationOfLandForestAndWater: {
        unlawfulEvictionAndTakingOfLand: valueOrUndefined(randomYesNo(), undefinedProbability),
        unlawfulEvictionAndTakingOfLandRisk: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        unlawfulEvictionAndTakingOfLandStrategies: valueOrUndefined(randomYesNo(), undefinedProbability),
        unlawfulEvictionAndTakingOfLandStrategiesName: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      useOfPrivatePublicSecurityForcesWithDisregardForHumanRights: {
        useOfPrivatePublicSecurityForces: valueOrUndefined(randomYesNo(), undefinedProbability),
        useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        instructionOfSecurityForces: valueOrUndefined(randomYesNo(), undefinedProbability),
        humanRightsTraining: valueOrUndefined(randomYesNo(), undefinedProbability),
        stateSecurityForces: valueOrUndefined(randomYesNoNa(), undefinedProbability),
        privateSecurityForces: valueOrUndefined(randomYesNoNa(), undefinedProbability),
        useOfPrivatePublicSecurityForcesMeasures: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
      },
    },
    environmental: {
      useOfMercuryMercuryWasteMinamataConvention: {
        mercuryAndMercuryWasteHandling: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAndMercuryWasteHandlingPolicy: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAddedProductsHandling: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAddedProductsHandlingRiskOfExposure: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAddedProductsHandlingRiskOfDisposal: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAndMercuryCompoundsProductionAndUse: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      productionAndUseOfPersistentOrganicPollutantsPopsConvention: {
        persistentOrganicPollutantsProductionAndUse: valueOrUndefined(randomYesNo(), undefinedProbability),
        persistentOrganicPollutantsUsed: valueOrUndefined(faker.company.bsNoun(), undefinedProbability),
        persistentOrganicPollutantsProductionAndUseRiskOfExposure: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        persistentOrganicPollutantsProductionAndUseRiskOfDisposal: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        legalRestrictedWasteProcesses: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      exportImportOfHazardousWasteBaselConvention: {
        persistentOrganicPollutantsProductionAndUseTransboundaryMovements: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        persistentOrganicPollutantsProductionAndUseRiskForImportingState: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        hazardousWasteTransboundaryMovementsOutsideOecdEuLiechtenstein: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        hazardousWasteDisposal: valueOrUndefined(randomYesNo(), undefinedProbability),
        hazardousWasteDisposalRiskOfImport: valueOrUndefined(randomYesNo(), undefinedProbability),
        hazardousAndOtherWasteImport: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
    },
  };
}
