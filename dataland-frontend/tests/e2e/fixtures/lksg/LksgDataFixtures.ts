import { faker } from "@faker-js/faker";
import {
  type LksgData,
  type LksgProcurementCategory,
  type LksgProduct,
  type LksgProductionSite,
  NationalOrInternationalMarket,
  ShareOfTemporaryWorkers,
} from "@clients/backend";
import { randomYesNo, randomYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { DEFAULT_PROBABILITY, Generator, valueOrUndefined } from "@e2e/utils/FakeFixtureUtils";
import { getRandomReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";
import { generateArray, generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { randomEuroValue, randomNumber, randomPercentageValue } from "@e2e/fixtures/common/NumberFixtures";
import { getRandomIso2CountryCode } from "@e2e/fixtures/common/CountryFixtures";
import { randomFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";
import { generateListOfNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateAddress } from "@e2e/fixtures/common/AddressFixtures";

/**
 * Generates a set number of LKSG fixtures
 * @param numFixtures the number of lksg fixtures to generate
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns a set number of LKSG fixtures
 */
export function generateLksgFixture(
  numFixtures: number,
  undefinedProbability = DEFAULT_PROBABILITY,
): FixtureData<LksgData>[] {
  return generateFixtureDataset<LksgData>(
    () => generateLksgData(undefinedProbability),
    numFixtures,
    (dataSet) => dataSet?.general?.masterData?.dataDate?.substring(0, 4) || getRandomReportingPeriod(),
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
    (dataSet) => dataSet?.general?.masterData?.dataDate?.substring(0, 4) || getRandomReportingPeriod(),
  )[0];
}

/**
 * Generates a random production site
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns a random production site
 */
export function generateProductionSite(undefinedProbability = DEFAULT_PROBABILITY): LksgProductionSite {
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
    generateArray(() => [getRandomIso2CountryCode(), valueOrUndefined(faker.number.int({ min: 0, max: 50 }))!]),
  );
  return {
    procuredProductTypesAndServicesNaceCodes: generateListOfNaceCodes(1),
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
      keys.map((procurementCategoryType) => [procurementCategoryType as string, generateProcurementCategory()]),
    ),
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
 * @param undefinedProbability the probability (as number between 0 and 1) for "undefined" values in nullable fields
 * @returns a random LKSG dataset
 */
export function generateLksgData(undefinedProbability = DEFAULT_PROBABILITY): LksgData {
  const dataGenerator = new Generator(undefinedProbability);
  return {
    general: {
      masterData: {
        dataDate: randomFutureDate(),
        headOfficeInGermany: dataGenerator.randomYesNo(),
        groupOfCompanies: dataGenerator.randomYesNo(),
        groupOfCompaniesName: dataGenerator.valueOrUndefined(faker.company.name()),
        industry: dataGenerator.valueOrUndefined(generateListOfNaceCodes()),
        numberOfEmployees: dataGenerator.valueOrUndefined(randomNumber(10000)),
        seasonalOrMigrantWorkers: dataGenerator.randomYesNo(),
        shareOfTemporaryWorkers: dataGenerator.valueOrUndefined(randomShareOfTemporaryWorkersInterval()),
        annualTotalRevenue: dataGenerator.valueOrUndefined(randomEuroValue()),
        totalRevenueCurrency: dataGenerator.valueOrUndefined(generateIso4217CurrencyCode()),
        fixedAndWorkingCapital: dataGenerator.valueOrUndefined(randomNumber(10000)),
      },
      productionSpecific: {
        manufacturingCompany: dataGenerator.randomYesNo(),
        capacity: dataGenerator.valueOrUndefined(
          randomNumber(25).toString() + " " + faker.commerce.product() + " per " + faker.date.weekday(),
        ),
        productionViaSubcontracting: dataGenerator.randomYesNo(),
        subcontractingCompaniesCountries: dataGenerator.valueOrUndefined(generateArray(getRandomIso2CountryCode)),
        subcontractingCompaniesIndustries: dataGenerator.valueOrUndefined(generateListOfNaceCodes()),
        productionSites: dataGenerator.randomYesNo(),
        listOfProductionSites: dataGenerator.valueOrUndefined(
          generateArray(() => generateProductionSite(undefinedProbability)),
        ),
        market: dataGenerator.valueOrUndefined(randomNationalOrInternationalMarket()),
        specificProcurement: dataGenerator.randomYesNo(),
      },
      productionSpecificOwnOperations: {
        mostImportantProducts: dataGenerator.valueOrUndefined(generateArray(generateProduct)),
        productsServicesCategoriesPurchased: dataGenerator.valueOrUndefined(generateProcurementCategories()),
      },
    },
    governance: {
      riskManagementOwnOperations: {
        riskManagementSystem: dataGenerator.randomYesNo(),
        riskAnalysisInFiscalYear: dataGenerator.randomYesNo(),
        risksIdentified: dataGenerator.randomYesNo(),
        identifiedRisks: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        counteractingMeasures: dataGenerator.randomYesNo(),
        whichCounteractingMeasures: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        regulatedRiskManagementResponsibility: dataGenerator.randomYesNo(),
        environmentalManagementSystem: dataGenerator.randomYesNo(),
        environmentalManagementSystemInternationalCertification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        environmentalManagementSystemNationalCertification: dataGenerator.randomBaseDataPoint(randomYesNo()),
      },
      grievanceMechanismOwnOperations: {
        grievanceHandlingMechanism: dataGenerator.randomYesNo(),
        grievanceHandlingReportingAccessible: dataGenerator.randomYesNo(),
        appropriateGrievanceHandlingInformation: dataGenerator.randomYesNo(),
        appropriateGrievanceHandlingSupport: dataGenerator.randomYesNo(),
        accessToExpertiseForGrievanceHandling: dataGenerator.randomYesNo(),
        grievanceComplaints: dataGenerator.randomYesNo(),
        complaintsNumber: dataGenerator.valueOrUndefined(randomNumber(10000)),
        complaintsReason: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        actionsForComplaintsUndertaken: dataGenerator.randomYesNo(),
        whichActionsForComplaintsUndertaken: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        publicAccessToGrievanceHandling: dataGenerator.randomYesNo(),
        whistleblowerProtection: dataGenerator.randomYesNo(),
        dueDiligenceProcessForGrievanceHandling: dataGenerator.randomYesNo(),
      },
      certificationsPoliciesAndResponsibilities: {
        sa8000Certification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        smetaSocialAuditConcept: dataGenerator.randomBaseDataPoint(randomYesNo()),
        betterWorkProgramCertificate: dataGenerator.randomBaseDataPoint(randomYesNoNa()),
        iso45001Certification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        iso14001Certification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        emasCertification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        iso37001Certification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        iso37301Certification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        riskManagementSystemCertification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        amforiBsciAuditReport: dataGenerator.randomBaseDataPoint(randomYesNo()),
        responsibleBusinessAssociationCertification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        fairLaborAssociationCertification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        additionalAudits: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        codeOfConduct: dataGenerator.randomBaseDataPoint(randomYesNo()),
        codeOfConductTraining: dataGenerator.randomYesNo(),
        supplierCodeOfConduct: dataGenerator.randomBaseDataPoint(randomYesNo()),
        policyStatement: dataGenerator.randomBaseDataPoint(randomYesNo()),
        humanRightsStrategy: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        environmentalImpactPolicy: dataGenerator.randomBaseDataPoint(randomYesNo()),
        fairWorkingConditionsPolicy: dataGenerator.randomBaseDataPoint(randomYesNo()),
      },
      generalViolations: {
        responsibilitiesForFairWorkingConditions: dataGenerator.randomYesNo(),
        responsibilitiesForTheEnvironment: dataGenerator.randomYesNo(),
        responsibilitiesForOccupationalSafety: dataGenerator.randomYesNo(),
        legalProceedings: dataGenerator.randomYesNo(),
        humanRightsViolationS: dataGenerator.randomYesNo(),
        humanRightsViolations: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        humanRightsViolationAction: dataGenerator.randomYesNo(),
        humanRightsViolationActionMeasures: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        highRiskCountriesRawMaterials: dataGenerator.randomYesNo(),
        highRiskCountriesRawMaterialsLocation: dataGenerator.valueOrUndefined(generateArray(getRandomIso2CountryCode)),
        highRiskCountriesActivity: dataGenerator.randomYesNo(),
        highRiskCountries: dataGenerator.valueOrUndefined(generateArray(getRandomIso2CountryCode)),
        highRiskCountriesProcurement: dataGenerator.randomYesNo(),
        highRiskCountriesProcurementName: dataGenerator.valueOrUndefined(generateArray(getRandomIso2CountryCode)),
      },
    },
    social: {
      childLabor: {
        childLaborPreventionPolicy: dataGenerator.randomBaseDataPoint(randomYesNo()),
        employeeSUnder18: dataGenerator.randomYesNo(),
        employeeSUnder15: dataGenerator.randomYesNo(),
        employeeSUnder18InApprenticeship: dataGenerator.randomYesNo(),
        worstFormsOfChildLaborProhibition: dataGenerator.randomYesNo(),
        worstFormsOfChildLabor: dataGenerator.randomYesNo(),
        worstFormsOfChildLaborForms: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        measuresForPreventionOfEmploymentUnderLocalMinimumAge: dataGenerator.randomYesNo(),
        employmentUnderLocalMinimumAgePreventionEmploymentContracts: dataGenerator.randomYesNo(),
        employmentUnderLocalMinimumAgePreventionJobDescription: dataGenerator.randomYesNo(),
        employmentUnderLocalMinimumAgePreventionIdentityDocuments: dataGenerator.randomYesNo(),
        employmentUnderLocalMinimumAgePreventionTraining: dataGenerator.randomYesNo(),
        employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: dataGenerator.randomYesNo(),
        additionalChildLaborMeasures: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
      },
      forcedLaborSlavery: {
        forcedLaborAndSlaveryPrevention: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionPractices: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        forcedLaborPreventionPolicy: dataGenerator.randomBaseDataPoint(randomYesNo()),
        forcedLaborAndSlaveryPreventionMeasures: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionEmploymentContracts: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionIdentityDocuments: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionFreeMovement: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionTraining: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionMeasuresOther: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
      },
      withholdingAdequateWages: {
        adequateWage: dataGenerator.randomYesNo(),
        adequateWagesMeasures: dataGenerator.randomYesNo(),
        documentedWorkingHoursAndWages: dataGenerator.randomYesNo(),
        adequateLivingWage: dataGenerator.randomYesNo(),
        regularWagesProcessFlow: dataGenerator.randomYesNo(),
        fixedHourlyWages: dataGenerator.randomYesNoNa(),
        fixedPieceworkWages: dataGenerator.randomYesNoNa(),
        adequateWageMeasures: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
      },
      disregardForOccupationalHealthSafety: {
        lowSkillWork: dataGenerator.randomYesNo(),
        hazardousMachines: dataGenerator.randomYesNo(),
        oshPolicy: dataGenerator.randomYesNo(),
        oshPolicyPersonalProtectiveEquipment: dataGenerator.randomYesNoNa(),
        oshPolicyMachineSafety: dataGenerator.randomYesNoNa(),
        oshPolicyDisasterBehavioralResponse: dataGenerator.randomYesNo(),
        oshPolicyAccidentsBehavioralResponse: dataGenerator.randomYesNo(),
        oshPolicyWorkplaceErgonomics: dataGenerator.randomYesNo(),
        oshPolicyAccessToWork: dataGenerator.randomYesNo(),
        oshPolicyHandlingChemicalsAndOtherHazardousSubstances: dataGenerator.randomYesNoNa(),
        oshPolicyFireProtection: dataGenerator.randomYesNo(),
        oshPolicyWorkingHours: dataGenerator.randomYesNo(),
        oshPolicyTrainingAddressed: dataGenerator.randomYesNo(),
        oshPolicyTraining: dataGenerator.randomYesNo(),
        oshManagementSystem: dataGenerator.randomYesNo(),
        oshManagementSystemInternationalCertification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        oshManagementSystemNationalCertification: dataGenerator.randomBaseDataPoint(randomYesNo()),
        under10WorkplaceAccidents: dataGenerator.randomYesNo(),
        oshTraining: dataGenerator.randomYesNo(),
        healthAndSafetyPolicy: dataGenerator.randomBaseDataPoint(randomYesNo()),
      },
      disregardForFreedomOfAssociation: {
        freedomOfAssociation: dataGenerator.randomYesNo(),
        employeeRepresentation: dataGenerator.valueOrUndefined(randomPercentageValue()),
        discriminationForTradeUnionMembers: dataGenerator.randomYesNo(),
        freedomOfOperationForTradeUnion: dataGenerator.randomYesNo(),
        freedomOfAssociationTraining: dataGenerator.randomYesNo(),
        worksCouncil: dataGenerator.randomYesNo(),
      },
      unequalTreatmentOfEmployment: {
        unequalTreatmentOfEmployment: dataGenerator.randomYesNo(),
        diversityAndInclusionRole: dataGenerator.randomYesNo(),
        preventionOfMistreatments: dataGenerator.randomYesNo(),
        equalOpportunitiesOfficer: dataGenerator.randomYesNo(),
        fairAndEthicalRecruitmentPolicy: dataGenerator.randomBaseDataPoint(randomYesNo()),
        equalOpportunitiesAndNonDiscriminationPolicy: dataGenerator.randomBaseDataPoint(randomYesNo()),
      },
      contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption: {
        harmfulSoilImpact: dataGenerator.randomYesNo(),
        soilDegradation: dataGenerator.randomYesNo(),
        soilErosion: dataGenerator.randomYesNo(),
        soilBorneDiseases: dataGenerator.randomYesNo(),
        soilContamination: dataGenerator.randomYesNo(),
        soilSalinization: dataGenerator.randomYesNo(),
        harmfulWaterPollution: dataGenerator.randomYesNo(),
        fertilizersOrPollutants: dataGenerator.randomYesNo(),
        wasteWaterFiltration: dataGenerator.randomYesNo(),
        harmfulAirPollution: dataGenerator.randomYesNo(),
        airFiltration: dataGenerator.randomYesNo(),
        harmfulNoiseEmission: dataGenerator.randomYesNo(),
        reduceNoiseEmissions: dataGenerator.randomYesNo(),
        excessiveWaterConsumption: dataGenerator.randomYesNo(),
        waterSavingMeasures: dataGenerator.randomYesNo(),
        waterSavingMeasuresName: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        pipeMaintaining: dataGenerator.randomYesNo(),
        waterSources: dataGenerator.randomYesNo(),
        contaminationMeasures: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
      },
      unlawfulEvictionDeprivationOfLandForestAndWater: {
        unlawfulEvictionAndTakingOfLand: dataGenerator.randomYesNo(),
        unlawfulEvictionAndTakingOfLandRisk: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        unlawfulEvictionAndTakingOfLandStrategies: dataGenerator.randomYesNo(),
        unlawfulEvictionAndTakingOfLandStrategiesName: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure: dataGenerator.randomYesNo(),
      },
      useOfPrivatePublicSecurityForcesWithDisregardForHumanRights: {
        useOfPrivatePublicSecurityForces: dataGenerator.randomYesNo(),
        useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: dataGenerator.randomYesNo(),
        instructionOfSecurityForces: dataGenerator.randomYesNo(),
        humanRightsTraining: dataGenerator.randomYesNo(),
        stateSecurityForces: dataGenerator.randomYesNoNa(),
        privateSecurityForces: dataGenerator.randomYesNoNa(),
        useOfPrivatePublicSecurityForcesMeasures: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
      },
    },
    environmental: {
      useOfMercuryMercuryWasteMinamataConvention: {
        mercuryAndMercuryWasteHandling: dataGenerator.randomYesNo(),
        mercuryAndMercuryWasteHandlingPolicy: dataGenerator.randomBaseDataPoint(randomYesNo()),
        mercuryAddedProductsHandling: dataGenerator.randomYesNo(),
        mercuryAddedProductsHandlingRiskOfExposure: dataGenerator.randomYesNo(),
        mercuryAddedProductsHandlingRiskOfDisposal: dataGenerator.randomYesNo(),
        mercuryAndMercuryCompoundsProductionAndUse: dataGenerator.randomYesNo(),
        mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: dataGenerator.randomYesNo(),
      },
      productionAndUseOfPersistentOrganicPollutantsPopsConvention: {
        persistentOrganicPollutantsProductionAndUse: dataGenerator.randomYesNo(),
        persistentOrganicPollutantsUsed: dataGenerator.valueOrUndefined(faker.company.buzzNoun()),
        persistentOrganicPollutantsProductionAndUseRiskOfExposure: dataGenerator.randomYesNo(),
        persistentOrganicPollutantsProductionAndUseRiskOfDisposal: dataGenerator.randomYesNo(),
        legalRestrictedWasteProcesses: dataGenerator.randomYesNo(),
      },
      exportImportOfHazardousWasteBaselConvention: {
        persistentOrganicPollutantsProductionAndUseTransboundaryMovements: dataGenerator.randomYesNo(),
        persistentOrganicPollutantsProductionAndUseRiskForImportingState: dataGenerator.randomYesNo(),
        hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein: dataGenerator.randomYesNo(),
        hazardousWasteTransboundaryMovementsOutsideOecdEuOrLiechtenstein: dataGenerator.randomYesNo(),
        hazardousWasteDisposal: dataGenerator.randomYesNo(),
        hazardousWasteDisposalRiskOfImport: dataGenerator.randomYesNo(),
        hazardousWasteDisposalOtherWasteImport: dataGenerator.randomYesNo(),
      },
    },
  };
}
