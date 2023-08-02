import { faker } from "@faker-js/faker";
import {
  LksgData,
  LksgProcurementCategory,
  LksgProduct,
  LksgProductionSite,
  NationalOrInternationalMarket,
  ShareOfTemporaryWorkers,
} from "@clients/backend";
import { randomYesNo, randomYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { getRandomReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";
import { generateArray, generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
import { randomEuroValue, randomNumber, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { getRandomIso2CountryCode } from "@e2e/fixtures/common/CountryFixtures";
import { randomFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { generateBaseDataPointOrUndefined } from "@e2e/fixtures/common/BaseDataPointFixtures";
import { ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";
import { valueOrNull } from "@e2e/fixtures/common/DataPointFixtures";
import { generateListOfNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateAddress } from "@e2e/fixtures/common/AddressFixtures";

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
 * Generates a Lksg fixture with a dataset with many null values for categories, subcategories and field values
 * @returns the fixture
 */
export function generateOneLksgFixtureWithManyNulls(): FixtureData<LksgData> {
  return generateFixtureDataset<LksgData>(
    () => generateOneLksgDatasetWithManyNulls(),
    1,
    (dataSet) => dataSet?.general?.masterData?.dataDate?.substring(0, 4) || getRandomReportingPeriod()
  )[0];
}

/**
 * Generates a random production site
 * @param undefinedProbability the percentage of undefined values in the returned production site
 * @returns a random production site
 */
export function generateProductionSite(undefinedProbability = 0.5): LksgProductionSite {
  return {
    nameOfProductionSite: valueOrUndefined(faker.company.name(), undefinedProbability),
    addressOfProductionSite: generateAddress(undefinedProbability),
    listOfGoodsOrServices: valueOrUndefined(generateListOfGoodsOrServices(), undefinedProbability),
  };
}

/**
 * Generates a random product
 * @returns a random product
 */
function generateProduct(): LksgProduct {
  return {
    name: faker.commerce.productName(),
    productionSteps: valueOrUndefined(generateArray(() => `${faker.word.verb()} ${faker.commerce.productMaterial()}`)),
    relatedCorporateSupplyChain: valueOrUndefined(faker.lorem.sentences()),
  };
}

/**
 * Generates a random procurement category
 * @returns random procurement category
 */
function generateProcurementCategory(): LksgProcurementCategory {
  const numberOfSuppliersPerCountryCodeAsMap = new Map<string, number>(
    generateArray(() => [getRandomIso2CountryCode(), valueOrNull(faker.number.int({ min: 0, max: 50 }))!])
  );
  return {
    procuredProductTypesAndServicesNaceCodes: generateListOfNaceCodes(),
    numberOfSuppliersPerCountryCode: valueOrUndefined(Object.fromEntries(numberOfSuppliersPerCountryCodeAsMap)),
    percentageOfTotalProcurement: valueOrUndefined(randomPercentageValue()),
  };
}

/**
 * Generates a random map of procurement categories
 * @returns random map of procurement categories
 */
function generateProcurementCategories(): { [key: string]: LksgProcurementCategory } {
  const procurementCategories = Object.values(ProcurementCategoryType);
  const keys = [] as ProcurementCategoryType[];
  procurementCategories.forEach((category) => {
    if (faker.datatype.boolean()) {
      keys.push(category);
    }
  });
  return Object.fromEntries(
    new Map<string, LksgProcurementCategory>(
      keys.map((procurementCategoryType) => [procurementCategoryType as string, generateProcurementCategory()])
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
 * Randomly returns <10%, 10-25%, 25-50% or >50%
 * @returns one of the four percentage intervals as string
 */
export function randomShareOfTemporaryWorkersInterval(): ShareOfTemporaryWorkers {
  return faker.helpers.arrayElement(Object.values(ShareOfTemporaryWorkers));
}

/**
 * Randomly returns National, International or Both
 * @returns one of the options as string
 */
export function randomNationalOrInternationalMarket(): NationalOrInternationalMarket {
  return faker.helpers.arrayElement(Object.values(NationalOrInternationalMarket));
}

/**
 * Generates an LKSG dataset with the value null for some categories, subcategories and field values.
 * Datasets that were uploaded via the Dataland API can look like this in production.
 * @returns the dataset
 */
export function generateOneLksgDatasetWithManyNulls(): LksgData {
  return {
    general: {
      masterData: {
        dataDate: "1999-12-24",
        headOfficeInGermany: null!,
        groupOfCompanies: null!,
        groupOfCompaniesName: null!,
        industry: null!,
        numberOfEmployees: null!,
        seasonalOrMigrantWorkers: null!,
        shareOfTemporaryWorkers: null!,
        totalRevenueCurrency: null!,
        annualTotalRevenue: null!,
        fixedAndWorkingCapital: null!,
      },
      productionSpecific: null!,
      productionSpecificOwnOperations: null!,
    },
    governance: null!,
    social: null!,
    environmental: null!,
  };
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
        dataDate: randomFutureDate(),
        headOfficeInGermany: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        groupOfCompanies: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        groupOfCompaniesName: valueOrUndefined(faker.company.name(), undefinedProbability),
        industry: valueOrUndefined(generateListOfNaceCodes(), undefinedProbability),
        numberOfEmployees: valueOrUndefined(randomNumber(10000), undefinedProbability),
        seasonalOrMigrantWorkers: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        shareOfTemporaryWorkers: valueOrUndefined(randomShareOfTemporaryWorkersInterval(), undefinedProbability),
        annualTotalRevenue: valueOrUndefined(randomEuroValue(), undefinedProbability),
        totalRevenueCurrency: valueOrUndefined(generateIso4217CurrencyCode(), undefinedProbability),
        fixedAndWorkingCapital: valueOrUndefined(randomNumber(10000), undefinedProbability),
      },
      productionSpecific: {
        manufacturingCompany: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        capacity: valueOrUndefined(randomNumber(25).toString() + " " + faker.commerce.product() + " per " + faker.date.weekday(), undefinedProbability),
        productionViaSubcontracting: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        subcontractingCompaniesCountries: valueOrUndefined(generateArray(getRandomIso2CountryCode), undefinedProbability),
        subcontractingCompaniesIndustries: valueOrUndefined(generateListOfNaceCodes(), undefinedProbability),
        productionSites: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        listOfProductionSites: valueOrUndefined(generateArray(() => generateProductionSite(undefinedProbability)), undefinedProbability),
        market: valueOrUndefined(randomNationalOrInternationalMarket(), undefinedProbability),
        specificProcurement: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
      productionSpecificOwnOperations: {
        mostImportantProducts: valueOrUndefined(generateArray(generateProduct), undefinedProbability),
        productsServicesCategoriesPurchased: valueOrUndefined(generateProcurementCategories(), undefinedProbability),
      },
    },
    governance: {
      riskManagementOwnOperations: {
        riskManagementSystem: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        riskManagementSystemFiscalYear: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        riskManagementSystemRisks: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        riskManagementSystemIdentifiedRisks: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        riskManagementSystemCounteract: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        riskManagementSystemMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        riskManagementSystemResponsibility: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        environmentalManagementSystem: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        environmentalManagementSystemInternationalCertification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        environmentalManagementSystemNationalCertification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
      grievanceMechanismOwnOperations: {
        grievanceHandlingMechanism: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        grievanceHandlingMechanismUsedForReporting: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        grievanceMechanismInformationProvided: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        grievanceMechanismSupportProvided: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        grievanceMechanismAccessToExpertise: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        grievanceMechanismComplaints: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        grievanceMechanismComplaintsNumber: valueOrUndefined(randomNumber(10000), undefinedProbability),
        grievanceMechanismComplaintsReason: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        grievanceMechanismComplaintsAction: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        grievanceMechanismComplaintsActionUndertaken: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        grievanceMechanismPublicAccess: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        grievanceMechanismProtection: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        grievanceMechanismDueDiligenceProcess: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
      certificationsPoliciesAndResponsibilities: {
        sa8000Certification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        smetaSocialAuditConcept: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        betterWorkProgramCertificate: generateBaseDataPointOrUndefined(randomYesNoNa(), undefinedProbability),
        iso45001Certification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        iso14001Certification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        emasCertification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        iso37001Certification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        iso37301Certification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        riskManagementSystemCertification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        amforiBsciAuditReport: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        responsibleBusinessAssociationCertification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        fairLaborAssociationCertification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        additionalAudits: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        codeOfConduct: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        codeOfConductTraining: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        supplierCodeOfConduct: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        policyStatement: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        humanRightsStrategy: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        environmentalImpactPolicy: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        fairWorkingConditionsPolicy: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
      generalViolations: {
        responsibilitiesForFairWorkingConditions: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        responsibilitiesForTheEnvironment: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        responsibilitiesForOccupationalSafety: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        legalProceedings: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        humanRightsViolationS: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        humanRightsViolations: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        humanRightsViolationAction: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        humanRightsViolationActionMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        highRiskCountriesRawMaterials: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        highRiskCountriesRawMaterialsLocation: valueOrUndefined(generateArray(getRandomIso2CountryCode), undefinedProbability),
        highRiskCountriesActivity: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        highRiskCountries: valueOrUndefined(generateArray(getRandomIso2CountryCode), undefinedProbability),
        highRiskCountriesProcurement: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        highRiskCountriesProcurementName: valueOrUndefined(generateArray(getRandomIso2CountryCode), undefinedProbability),
      },
    },
    social: {
      childLabor: {
        childLaborPreventionPolicy: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        employeeSUnder18: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        employeeSUnder15: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        employeeSUnder18InApprenticeship: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        worstFormsOfChildLaborProhibition: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        worstFormsOfChildLabor: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        worstFormsOfChildLaborForms: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        measuresForPreventionOfEmploymentUnderLocalMinimumAge: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        employmentUnderLocalMinimumAgePreventionEmploymentContracts: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        employmentUnderLocalMinimumAgePreventionJobDescription: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        employmentUnderLocalMinimumAgePreventionIdentityDocuments: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        employmentUnderLocalMinimumAgePreventionTraining: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        additionalChildLaborMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
      },
      forcedLaborSlavery: {
        forcedLaborAndSlaveryPrevention: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        forcedLaborAndSlaveryPreventionPractices: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        forcedLaborPreventionPolicy: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        forcedLaborAndSlaveryPreventionMeasures: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        forcedLaborAndSlaveryPreventionEmploymentContracts: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        forcedLaborAndSlaveryPreventionIdentityDocuments: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        forcedLaborAndSlaveryPreventionFreeMovement: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        forcedLaborAndSlaveryPreventionTraining: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        forcedLaborAndSlaveryPreventionMeasuresOther: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
      },
      withholdingAdequateWages: {
        adequateWage: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        adequateWagesMeasures: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        documentedWorkingHoursAndWages: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        adequateLivingWage: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        regularWagesProcessFlow: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        fixedHourlyWages: randomYesNoNa(),
        fixedPieceworkWages: randomYesNoNa(),
        adequateWageMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
      },
      disregardForOccupationalHealthSafety: {
        lowSkillWork: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        hazardousMachines: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshPolicy: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshPolicyPersonalProtectiveEquipment: randomYesNoNa(),
        oshPolicyMachineSafety: randomYesNoNa(),
        oshPolicyDisasterBehavioralResponse: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshPolicyAccidentsBehavioralResponse: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshPolicyWorkplaceErgonomics: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshPolicyAccessToWork: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshPolicyHandlingChemicalsAndOtherHazardousSubstances: randomYesNoNa(),
        oshPolicyFireProtection: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshPolicyWorkingHours: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshPolicyTrainingAddressed: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshPolicyTraining: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshManagementSystem: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshManagementSystemInternationalCertification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshManagementSystemNationalCertification: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        under10WorkplaceAccidents: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        oshTraining: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        healthAndSafetyPolicy: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
      disregardForFreedomOfAssociation: {
        freedomOfAssociation: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        employeeRepresentation: valueOrUndefined(randomPercentageValue(), undefinedProbability),
        discriminationForTradeUnionMembers: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        freedomOfOperationForTradeUnion: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        freedomOfAssociationTraining: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        worksCouncil: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
      unequalTreatmentOfEmployment: {
        unequalTreatmentOfEmployment: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        diversityAndInclusionRole: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        preventionOfMistreatments: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        equalOpportunitiesOfficer: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        fairAndEthicalRecruitmentPolicy: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        equalOpportunitiesAndNonDiscriminationPolicy: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
      contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption: {
        harmfulSoilImpact: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        soilDegradation: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        soilErosion: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        soilBorneDiseases: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        soilContamination: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        soilSalinization: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        harmfulWaterPollution: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        fertilizersOrPollutants: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        wasteWaterFiltration: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        harmfulAirPollution: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        airFiltration: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        harmfulNoiseEmission: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        reduceNoiseEmissions: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        excessiveWaterConsumption: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        waterSavingMeasures: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        waterSavingMeasuresName: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        pipeMaintaining: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        waterSources: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        contaminationMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
      },
      unlawfulEvictionDeprivationOfLandForestAndWater: {
        unlawfulEvictionAndTakingOfLand: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        unlawfulEvictionAndTakingOfLandRisk: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        unlawfulEvictionAndTakingOfLandStrategies: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        unlawfulEvictionAndTakingOfLandStrategiesName: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
      useOfPrivatePublicSecurityForcesWithDisregardForHumanRights: {
        useOfPrivatePublicSecurityForces: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        instructionOfSecurityForces: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        humanRightsTraining: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        stateSecurityForces: randomYesNoNa(),
        privateSecurityForces: randomYesNoNa(),
        useOfPrivatePublicSecurityForcesMeasures: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
      },
    },
    environmental: {
      useOfMercuryMercuryWasteMinamataConvention: {
        mercuryAndMercuryWasteHandling: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        mercuryAndMercuryWasteHandlingPolicy: valueOrUndefined(generateBaseDataPointOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        mercuryAddedProductsHandling: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        mercuryAddedProductsHandlingRiskOfExposure: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        mercuryAddedProductsHandlingRiskOfDisposal: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        mercuryAndMercuryCompoundsProductionAndUse: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
      productionAndUseOfPersistentOrganicPollutantsPopsConvention: {
        persistentOrganicPollutantsProductionAndUse: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        persistentOrganicPollutantsUsed: valueOrUndefined(faker.company.buzzNoun(), undefinedProbability),
        persistentOrganicPollutantsProductionAndUseRiskOfExposure: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        persistentOrganicPollutantsProductionAndUseRiskOfDisposal: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        legalRestrictedWasteProcesses: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
      exportImportOfHazardousWasteBaselConvention: {
        persistentOrganicPollutantsProductionAndUseTransboundaryMovements: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        persistentOrganicPollutantsProductionAndUseRiskForImportingState: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        hazardousWasteTransboundaryMovementsOutsideOecdEuOrLiechtenstein: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        hazardousWasteDisposal: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        hazardousWasteDisposalRiskOfImport: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
        hazardousWasteDisposalOtherWasteImport: valueOrUndefined(valueOrUndefined(randomYesNo(), undefinedProbability), undefinedProbability),
      },
    },

  }
}
