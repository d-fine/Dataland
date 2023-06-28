import { faker } from "@faker-js/faker/locale/de";
import {
  LksgAddress,
  LksgCountryAssociatedSuppliers,
  LksgData,
  LksgProduct,
  LksgProductCategory,
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
import { generateIso2CountryCode, generateListOfIso2CountryCodes } from "@e2e/fixtures/common/CountryFixtures";
import { randomPastDate } from "@e2e/fixtures/common/DateFixtures";
import { generateBaseDataPointOrUndefined } from "@e2e/fixtures/common/BaseDataPointFixtures";
import { ProcurementCategory } from "@/api-models/ProcurementCategory";

/**
 * Generates a set number of LKSG fixtures
 * @param numFixtures the number of lksg fixtures to generate
 * @param undefinedProbability the probability of fields to be undefined (number between 0 and 1)
 * @returns a set number of LKSG fixtures
 */
export function generateLksgFixture(numFixtures: number, undefinedProbability = 0.5): FixtureData<LksgData>[] {
  return generateFixtureDataset<LksgData>(
    () => generateLksgData(undefinedProbability),
    numFixtures,
    (dataSet) => dataSet?.general?.masterData?.dataDate?.substring(0, 4) || getRandomReportingPeriod()
  );
}

/**
 * Generates a array of random length with content
 * @param generator generator for a single entry
 * @param min the minimum number of entries
 * @param max the maximum number of entries
 * @returns the generated array
 */
function generateArray<T>(generator: () => T, min = 0, max = 5): T[] {
  return Array.from({ length: faker.number.int({ min, max }) }, () => generator());
}

/**
 * Generates a random production site
 * @param undefinedProbability the percentage of undefined values in the returned production site
 * @returns a random production site
 */
export function generateProductionSite(undefinedProbability = 0.5): LksgProductionSite {
  return {
    nameOfProductionSite: faker.company.name(),
    addressOfProductionSite: generateAddress(),
    listOfGoodsOrServices: valueOrUndefined(generateListOfGoodsOrServices(), undefinedProbability),
  };
}

/**
 * Generates an array consisting of 0 to 5 random production sites
 * @param undefinedProbability the percentage of undefined values in the returned production site
 * @returns 0 to 5 random production sites
 */
export function generateArrayOfProductionSites(undefinedProbability = 0.5): LksgProductionSite[] {
  return generateArray(() => generateProductionSite(undefinedProbability));
}

/**
 * Generates a random product
 * @returns a random product
 */
function generateProduct(): LksgProduct {
  return {
    productName: faker.commerce.productName(),
    productionSteps: valueOrUndefined(generateArray(() => faker.commerce.productName())),
    relatedCorporateSupplyChain: valueOrUndefined(faker.commerce.productName()),
  };
}

/**
 * Generates a random company associated supplier
 * @returns random company associated supplier
 */
function generateCompanyAssociatedSupplier(): LksgCountryAssociatedSuppliers {
  return {
    country: generateIso2CountryCode(), // TODO should this be extended to all strings? just because the backend would accept it
    numberOfSuppliers: valueOrUndefined(randomNumber(10)),
  };
}

/**
 * Generates a random product category
 * @returns random product category
 */
function generateProductCategory(): LksgProductCategory {
  return {
    definitionProductTypeService: generateArray(() => faker.commerce.productName()),
    suppliersPerCountry: valueOrUndefined(generateArray(generateCompanyAssociatedSupplier)),
    orderVolume: valueOrUndefined(randomPercentageValue()),
  };
}

/**
 * Generates a random map of product categories
 * @returns random map of product categories
 */
function generateProductCategories(): { [key: string]: LksgProductCategory } {
  const procurementCategories = Object.values(ProcurementCategory) as ProcurementCategory[];
  const keys = [] as ProcurementCategory[];
  procurementCategories.forEach((category) => {
    if (faker.datatype.boolean()) {
      keys.push(category);
    }
  });
  return Object.fromEntries(
    new Map<string, LksgProductCategory>(
      keys.map((procurementCategory) => [procurementCategory as string, generateProductCategory()])
    )
  );
}

/**
 * Generates a random array of goods or services
 * @returns random array of goods or services
 */
export function generateListOfGoodsOrServices(): string[] {
  return generateArray(() => faker.commerce.productName(), 1);
}

/**
 * Generates a random address
 * @returns a random address
 */
export function generateAddress(): LksgAddress {
  return {
    streetAndHouseNumber: faker.location.street() + " " + faker.location.buildingNumber(),
    city: faker.location.city(),
    state: faker.location.state(),
    postalCode: faker.location.zipCode(),
    country: generateIso2CountryCode(),
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
 * Generates a random list of Nace codes (unique and sorted)
 * @returns random list of Nace codes
 */
export function generateListOfNaceCodes(): string[] {
  const values = Array.from({ length: faker.number.int({ min: 0, max: 5 }) }, () => {
    return faker.helpers.arrayElement(["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"]);
  }).sort((a, b) => a.localeCompare(b));
  return [...new Set(values)];
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
        headOfficeInGermany: valueOrUndefined(randomYesNo(), undefinedProbability),
        groupOfCompanies: valueOrUndefined(randomYesNo(), undefinedProbability),
        groupOfCompaniesName: valueOrUndefined(faker.company.name(), undefinedProbability),
        industry: valueOrUndefined(generateListOfNaceCodes(), undefinedProbability),
        numberOfEmployees: valueOrUndefined(randomNumber(10000), undefinedProbability),
        seasonalOrMigrantWorkers: valueOrUndefined(randomYesNo(), undefinedProbability),
        shareOfTemporaryWorkers: valueOrUndefined(randomShareOfTemporaryWorkersInterval(), undefinedProbability),
        totalRevenueCurrency: valueOrUndefined(generateIso4217CurrencyCode(), undefinedProbability),
        totalRevenue: valueOrUndefined(randomEuroValue(), undefinedProbability),
        fixedAndWorkingCapital: valueOrUndefined(randomNumber(10000000), undefinedProbability),
      },
      productionSpecific: {
        manufacturingCompany: valueOrUndefined(randomYesNo(), undefinedProbability),
        capacity: valueOrUndefined(
          randomNumber(25).toString() + " " + faker.commerce.product() + " per " + faker.date.weekday(),
          undefinedProbability
        ),
        isContractProcessing: valueOrUndefined(randomYesNo(), undefinedProbability),
        subcontractingCompaniesCountries: valueOrUndefined(generateListOfIso2CountryCodes(), undefinedProbability),
        subcontractingCompaniesIndustries: valueOrUndefined(generateListOfNaceCodes(), undefinedProbability),
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
      productionSpecificOwnOperations: {
        mostImportantProducts: valueOrUndefined(generateArray(generateProduct), undefinedProbability),
        productCategories: valueOrUndefined(generateProductCategories(), undefinedProbability),
      },
    },
    governance: {
      riskManagementOwnOperations: {
        adequateAndEffectiveRiskManagementSystem: valueOrUndefined(randomYesNo(), undefinedProbability),
        riskManagementSystemFiscalYear: valueOrUndefined(randomYesNo(), undefinedProbability),
        riskManagementSystemRisks: valueOrUndefined(randomYesNo(), undefinedProbability),
        riskManagementSystemIdentifiedRisks: valueOrUndefined(faker.commerce.productAdjective(), undefinedProbability),
        riskManagementSystemCounteract: valueOrUndefined(randomYesNo(), undefinedProbability),
        riskManagementSystemMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        riskManagementSystemResponsibility: valueOrUndefined(randomYesNo(), undefinedProbability),
        environmentalManagementSystem: valueOrUndefined(randomYesNo(), undefinedProbability),
        environmentalManagementSystemInternationalCertification: generateBaseDataPointOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        environmentalManagementSystemNationalCertification: generateBaseDataPointOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
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
        sa8000Certification: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        smetaSocialAuditConcept: valueOrUndefined(randomYesNo(), undefinedProbability),
        betterWorkProgramCertificate: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        iso45001Certification: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        iso14000Certification: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        emasCertification: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        iso37001Certification: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        iso37301Certification: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        riskManagementSystemCertification: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        amforiBsciAuditReport: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        responsibleBusinessAssociationCertification: generateBaseDataPointOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        fairLaborAssociationCertification: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        additionalAudits: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        codeOfConduct: valueOrUndefined(randomYesNo(), undefinedProbability),
        codeOfConductTraining: valueOrUndefined(randomYesNo(), undefinedProbability),
        supplierCodeOfConduct: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        policyStatement: valueOrUndefined(randomYesNo(), undefinedProbability),
        humanRightsStrategy: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        environmentalImpactPolicy: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        fairWorkingConditionsPolicy: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
      },
      generalViolations: {
        responsibilitiesForFairWorkingConditions: valueOrUndefined(randomYesNo(), undefinedProbability),
        responsibilitiesForTheEnvironment: valueOrUndefined(randomYesNo(), undefinedProbability),
        responsibilitiesForOccupationalSafety: valueOrUndefined(randomYesNo(), undefinedProbability),
        legalProceedings: valueOrUndefined(randomYesNo(), undefinedProbability),
        humanRightsViolation: valueOrUndefined(randomYesNo(), undefinedProbability),
        humanRightsViolations: valueOrUndefined(faker.location.country(), undefinedProbability),
        humanRightsViolationAction: valueOrUndefined(randomYesNo(), undefinedProbability),
        humanRightsViolationActionMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        highRiskCountriesRawMaterials: valueOrUndefined(randomYesNo(), undefinedProbability),
        highRiskCountriesRawMaterialsLocation: valueOrUndefined(generateListOfIso2CountryCodes(), undefinedProbability),
        highRiskCountriesActivity: valueOrUndefined(randomYesNo(), undefinedProbability),
        highRiskCountries: valueOrUndefined(generateListOfIso2CountryCodes(), undefinedProbability),
        highRiskCountriesProcurement: valueOrUndefined(randomYesNo(), undefinedProbability),
        highRiskCountriesProcurementName: valueOrUndefined(generateListOfIso2CountryCodes(), undefinedProbability),
      },
    },
    social: {
      childLabor: {
        employeeUnder18: valueOrUndefined(randomYesNo(), undefinedProbability),
        employeeUnder15: valueOrUndefined(randomYesNo(), undefinedProbability),
        employeeUnder18Apprentices: valueOrUndefined(randomYesNo(), undefinedProbability),
        worstFormsOfChildLaborProhibition: valueOrUndefined(randomYesNo(), undefinedProbability),
        worstFormsOfChildLaborForms: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
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
        childLaborMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        childLaborPreventionPolicy: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
      },
      forcedLaborSlavery: {
        forcedLaborAndSlaveryPrevention: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionPractices: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        forcedLaborAndSlaveryPreventionMeasures: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionEmploymentContracts: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionIdentityDocuments: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionFreeMovement: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets: valueOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        forcedLaborAndSlaveryPreventionTraining: valueOrUndefined(randomYesNo(), undefinedProbability),
        forcedLaborAndSlaveryPreventionMeasuresOther: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        forcedLaborPreventionPolicy: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
      },
      withholdingAdequateWages: {
        adequateWage: valueOrUndefined(randomYesNo(), undefinedProbability),
        adequateWageBeingWithheld: valueOrUndefined(randomYesNo(), undefinedProbability),
        documentedWorkingHoursAndWages: valueOrUndefined(randomYesNo(), undefinedProbability),
        adequateLivingWage: valueOrUndefined(randomYesNo(), undefinedProbability),
        regularWagesProcessFlow: valueOrUndefined(randomYesNo(), undefinedProbability),
        fixedHourlyWages: valueOrUndefined(randomYesNoNa(), undefinedProbability),
        fixedPieceworkWages: valueOrUndefined(randomYesNoNa(), undefinedProbability),
        adequateWageMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
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
        oshManagementSystemInternationalCertification: generateBaseDataPointOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
        oshManagementSystemNationalCertification: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        workplaceAccidentsUnder10: valueOrUndefined(randomYesNo(), undefinedProbability),
        oshTraining: valueOrUndefined(randomYesNo(), undefinedProbability),
        healthAndSafetyPolicy: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
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
        fairAndEthicalRecruitmentPolicy: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        equalOpportunitiesAndNonDiscriminationPolicy: generateBaseDataPointOrUndefined(
          randomYesNo(),
          undefinedProbability
        ),
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
        waterSavingMeasuresName: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        pipeMaintaining: valueOrUndefined(randomYesNo(), undefinedProbability),
        waterSources: valueOrUndefined(randomYesNo(), undefinedProbability),
        contaminationMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
      },
      unlawfulEvictionDeprivationOfLandForestAndWater: {
        unlawfulEvictionAndTakingOfLand: valueOrUndefined(randomYesNo(), undefinedProbability),
        unlawfulEvictionAndTakingOfLandRisk: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        unlawfulEvictionAndTakingOfLandStrategies: valueOrUndefined(randomYesNo(), undefinedProbability),
        unlawfulEvictionAndTakingOfLandStrategiesName: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
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
        useOfPrivatePublicSecurityForcesMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
      },
    },
    environmental: {
      useOfMercuryMercuryWasteMinamataConvention: {
        mercuryAndMercuryWasteHandling: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAndMercuryWasteHandlingPolicy: generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAddedProductsHandling: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAddedProductsHandlingRiskOfExposure: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAddedProductsHandlingRiskOfDisposal: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAndMercuryCompoundsProductionAndUse: valueOrUndefined(randomYesNo(), undefinedProbability),
        mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: valueOrUndefined(randomYesNo(), undefinedProbability),
      },
      productionAndUseOfPersistentOrganicPollutantsPopsConvention: {
        persistentOrganicPollutantsProductionAndUse: valueOrUndefined(randomYesNo(), undefinedProbability),
        persistentOrganicPollutantsUsed: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
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
