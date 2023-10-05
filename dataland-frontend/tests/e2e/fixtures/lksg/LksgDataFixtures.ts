import { faker } from "@faker-js/faker";
import {
  type LksgData,
  type LksgProcurementCategory,
  type LksgProduct,
  type LksgProductionSite,
  NationalOrInternationalMarket,
  ShareOfTemporaryWorkers,
} from "@clients/backend";
import { generateYesNo, generateYesNoNa } from "@e2e/fixtures/common/YesNoFixtures";
import { generateCurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import { DEFAULT_PROBABILITY, Generator, valueOrNull } from "@e2e/utils/FakeFixtureUtils";
import { generateReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";
import { generateArray, generateFixtureDataset, pickOneElement } from "@e2e/fixtures/FixtureUtils";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { generateInt } from "@e2e/fixtures/common/NumberFixtures";
import { generateIso2CountryCode } from "@e2e/fixtures/common/CountryFixtures";
import { generateFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";
import { generateNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
import { generateAddress } from "@e2e/fixtures/common/AddressFixtures";

/**
 * Generates a set number of LKSG fixtures
 * @param numFixtures the number of lksg fixtures to generate
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a set number of LKSG fixtures
 */
export function generateLksgFixture(
  numFixtures: number,
  nullProbability = DEFAULT_PROBABILITY,
): FixtureData<LksgData>[] {
  return generateFixtureDataset<LksgData>(
    () => generateLksgData(nullProbability),
    numFixtures,
    (dataSet) => dataSet?.general?.masterData?.dataDate?.substring(0, 4) || generateReportingPeriod(),
  );
}

/**
 * Generates a random production site
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a random production site
 */
export function generateProductionSite(nullProbability = DEFAULT_PROBABILITY): LksgProductionSite {
  return {
    nameOfProductionSite: valueOrNull(faker.company.name(), nullProbability),
    addressOfProductionSite: generateAddress(nullProbability),
    listOfGoodsOrServices: valueOrNull(generateListOfGoodsOrServices(), nullProbability),
  };
}

/**
 * Generates a random array of goods or services
 * @returns random array of goods or services
 */
function generateListOfGoodsOrServices(): string[] {
  return generateArray(() => faker.commerce.productName(), 1);
}

/**
 * Generates a random LKSG dataset
 * @param nullProbability the probability (as number between 0 and 1) for "null" values in optional fields
 * @returns a random LKSG dataset
 */
export function generateLksgData(nullProbability = DEFAULT_PROBABILITY): LksgData {
  const dataGenerator = new LksgGenerator(nullProbability);
  return {
    general: {
      masterData: {
        dataDate: generateFutureDate(),
        headOfficeInGermany: dataGenerator.randomYesNo(),
        groupOfCompanies: dataGenerator.randomYesNo(),
        groupOfCompaniesName: dataGenerator.valueOrNull(faker.company.name()),
        industry: dataGenerator.valueOrNull(generateNaceCodes()),
        numberOfEmployees: dataGenerator.randomInt(),
        seasonalOrMigrantWorkers: dataGenerator.randomYesNo(),
        shareOfTemporaryWorkers: dataGenerator.randomShareOfTemporaryWorkersInterval(),
        annualTotalRevenue: dataGenerator.randomCurrencyValue(),
        totalRevenueCurrency: dataGenerator.valueOrNull(generateCurrencyCode()),
        fixedAndWorkingCapital: dataGenerator.randomInt(),
      },
      productionSpecific: {
        manufacturingCompany: dataGenerator.randomYesNo(),
        capacity: dataGenerator.valueOrNull(
          generateInt(25).toString() + " " + faker.commerce.product() + " per " + faker.date.weekday(),
        ),
        productionViaSubcontracting: dataGenerator.randomYesNo(),
        subcontractingCompaniesCountries: dataGenerator.randomArray(generateIso2CountryCode),
        subcontractingCompaniesIndustries: dataGenerator.valueOrNull(generateNaceCodes()),
        productionSites: dataGenerator.randomYesNo(),
        listOfProductionSites: dataGenerator.randomArray(() => generateProductionSite(nullProbability)),
        market: dataGenerator.randomNationalOrInternationalMarket(),
        specificProcurement: dataGenerator.randomYesNo(),
      },
      productionSpecificOwnOperations: {
        mostImportantProducts: dataGenerator.randomArray(() => dataGenerator.generateProduct()),
        productsServicesCategoriesPurchased: dataGenerator.valueOrNull(dataGenerator.generateProcurementCategories()),
      },
    },
    governance: {
      riskManagementOwnOperations: {
        riskManagementSystem: dataGenerator.randomYesNo(),
        riskAnalysisInFiscalYear: dataGenerator.randomYesNo(),
        risksIdentified: dataGenerator.randomYesNo(),
        identifiedRisks: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        counteractingMeasures: dataGenerator.randomYesNo(),
        whichCounteractingMeasures: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        regulatedRiskManagementResponsibility: dataGenerator.randomYesNo(),
        environmentalManagementSystem: dataGenerator.randomYesNo(),
        environmentalManagementSystemInternationalCertification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        environmentalManagementSystemNationalCertification: dataGenerator.randomBaseDataPoint(generateYesNo()),
      },
      grievanceMechanismOwnOperations: {
        grievanceHandlingMechanism: dataGenerator.randomYesNo(),
        grievanceHandlingReportingAccessible: dataGenerator.randomYesNo(),
        appropriateGrievanceHandlingInformation: dataGenerator.randomYesNo(),
        appropriateGrievanceHandlingSupport: dataGenerator.randomYesNo(),
        accessToExpertiseForGrievanceHandling: dataGenerator.randomYesNo(),
        grievanceComplaints: dataGenerator.randomYesNo(),
        complaintsNumber: dataGenerator.randomInt(),
        complaintsReason: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        actionsForComplaintsUndertaken: dataGenerator.randomYesNo(),
        whichActionsForComplaintsUndertaken: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        publicAccessToGrievanceHandling: dataGenerator.randomYesNo(),
        whistleblowerProtection: dataGenerator.randomYesNo(),
        dueDiligenceProcessForGrievanceHandling: dataGenerator.randomYesNo(),
      },
      certificationsPoliciesAndResponsibilities: {
        sa8000Certification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        smetaSocialAuditConcept: dataGenerator.randomBaseDataPoint(generateYesNo()),
        betterWorkProgramCertificate: dataGenerator.randomBaseDataPoint(generateYesNoNa()),
        iso45001Certification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        iso14001Certification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        emasCertification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        iso37001Certification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        iso37301Certification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        riskManagementSystemCertification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        amforiBsciAuditReport: dataGenerator.randomBaseDataPoint(generateYesNo()),
        responsibleBusinessAssociationCertification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        fairLaborAssociationCertification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        additionalAudits: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        codeOfConduct: dataGenerator.randomBaseDataPoint(generateYesNo()),
        codeOfConductTraining: dataGenerator.randomYesNo(),
        supplierCodeOfConduct: dataGenerator.randomBaseDataPoint(generateYesNo()),
        policyStatement: dataGenerator.randomBaseDataPoint(generateYesNo()),
        humanRightsStrategy: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        environmentalImpactPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        fairWorkingConditionsPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
      },
      generalViolations: {
        responsibilitiesForFairWorkingConditions: dataGenerator.randomYesNo(),
        responsibilitiesForTheEnvironment: dataGenerator.randomYesNo(),
        responsibilitiesForOccupationalSafety: dataGenerator.randomYesNo(),
        legalProceedings: dataGenerator.randomYesNo(),
        humanRightsViolationS: dataGenerator.randomYesNo(),
        humanRightsViolations: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        humanRightsViolationAction: dataGenerator.randomYesNo(),
        humanRightsViolationActionMeasures: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        highRiskCountriesRawMaterials: dataGenerator.randomYesNo(),
        highRiskCountriesRawMaterialsLocation: dataGenerator.randomArray(generateIso2CountryCode),
        highRiskCountriesActivity: dataGenerator.randomYesNo(),
        highRiskCountries: dataGenerator.randomArray(generateIso2CountryCode),
        highRiskCountriesProcurement: dataGenerator.randomYesNo(),
        highRiskCountriesProcurementName: dataGenerator.randomArray(generateIso2CountryCode),
      },
    },
    social: {
      childLabor: {
        childLaborPreventionPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        employeeSUnder18: dataGenerator.randomYesNo(),
        employeeSUnder15: dataGenerator.randomYesNo(),
        employeeSUnder18InApprenticeship: dataGenerator.randomYesNo(),
        worstFormsOfChildLaborProhibition: dataGenerator.randomYesNo(),
        worstFormsOfChildLabor: dataGenerator.randomYesNo(),
        worstFormsOfChildLaborForms: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        measuresForPreventionOfEmploymentUnderLocalMinimumAge: dataGenerator.randomYesNo(),
        employmentUnderLocalMinimumAgePreventionEmploymentContracts: dataGenerator.randomYesNo(),
        employmentUnderLocalMinimumAgePreventionJobDescription: dataGenerator.randomYesNo(),
        employmentUnderLocalMinimumAgePreventionIdentityDocuments: dataGenerator.randomYesNo(),
        employmentUnderLocalMinimumAgePreventionTraining: dataGenerator.randomYesNo(),
        employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: dataGenerator.randomYesNo(),
        additionalChildLaborMeasures: dataGenerator.valueOrNull(faker.company.buzzNoun()),
      },
      forcedLaborSlavery: {
        forcedLaborAndSlaveryPrevention: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionPractices: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        forcedLaborPreventionPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        forcedLaborAndSlaveryPreventionMeasures: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionEmploymentContracts: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionIdentityDocuments: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionFreeMovement: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionTraining: dataGenerator.randomYesNo(),
        forcedLaborAndSlaveryPreventionMeasuresOther: dataGenerator.valueOrNull(faker.company.buzzNoun()),
      },
      withholdingAdequateWages: {
        adequateWage: dataGenerator.randomYesNo(),
        adequateWagesMeasures: dataGenerator.randomYesNo(),
        documentedWorkingHoursAndWages: dataGenerator.randomYesNo(),
        adequateLivingWage: dataGenerator.randomYesNo(),
        regularWagesProcessFlow: dataGenerator.randomYesNo(),
        fixedHourlyWages: dataGenerator.randomYesNoNa(),
        fixedPieceworkWages: dataGenerator.randomYesNoNa(),
        adequateWageMeasures: dataGenerator.valueOrNull(faker.company.buzzNoun()),
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
        oshManagementSystemInternationalCertification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        oshManagementSystemNationalCertification: dataGenerator.randomBaseDataPoint(generateYesNo()),
        under10WorkplaceAccidents: dataGenerator.randomYesNo(),
        oshTraining: dataGenerator.randomYesNo(),
        healthAndSafetyPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
      },
      disregardForFreedomOfAssociation: {
        freedomOfAssociation: dataGenerator.randomYesNo(),
        employeeRepresentationInPercent: dataGenerator.randomPercentageValue(),
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
        fairAndEthicalRecruitmentPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        equalOpportunitiesAndNonDiscriminationPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
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
        waterSavingMeasuresName: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        pipeMaintaining: dataGenerator.randomYesNo(),
        waterSources: dataGenerator.randomYesNo(),
        contaminationMeasures: dataGenerator.valueOrNull(faker.company.buzzNoun()),
      },
      unlawfulEvictionDeprivationOfLandForestAndWater: {
        unlawfulEvictionAndTakingOfLand: dataGenerator.randomYesNo(),
        unlawfulEvictionAndTakingOfLandRisk: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        unlawfulEvictionAndTakingOfLandStrategies: dataGenerator.randomYesNo(),
        unlawfulEvictionAndTakingOfLandStrategiesName: dataGenerator.valueOrNull(faker.company.buzzNoun()),
        voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure: dataGenerator.randomYesNo(),
      },
      useOfPrivatePublicSecurityForcesWithDisregardForHumanRights: {
        useOfPrivatePublicSecurityForces: dataGenerator.randomYesNo(),
        useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: dataGenerator.randomYesNo(),
        instructionOfSecurityForces: dataGenerator.randomYesNo(),
        humanRightsTraining: dataGenerator.randomYesNo(),
        stateSecurityForces: dataGenerator.randomYesNoNa(),
        privateSecurityForces: dataGenerator.randomYesNoNa(),
        useOfPrivatePublicSecurityForcesMeasures: dataGenerator.valueOrNull(faker.company.buzzNoun()),
      },
    },
    environmental: {
      useOfMercuryMercuryWasteMinamataConvention: {
        mercuryAndMercuryWasteHandling: dataGenerator.randomYesNo(),
        mercuryAndMercuryWasteHandlingPolicy: dataGenerator.randomBaseDataPoint(generateYesNo()),
        mercuryAddedProductsHandling: dataGenerator.randomYesNo(),
        mercuryAddedProductsHandlingRiskOfExposure: dataGenerator.randomYesNo(),
        mercuryAddedProductsHandlingRiskOfDisposal: dataGenerator.randomYesNo(),
        mercuryAndMercuryCompoundsProductionAndUse: dataGenerator.randomYesNo(),
        mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: dataGenerator.randomYesNo(),
      },
      productionAndUseOfPersistentOrganicPollutantsPopsConvention: {
        persistentOrganicPollutantsProductionAndUse: dataGenerator.randomYesNo(),
        persistentOrganicPollutantsUsed: dataGenerator.valueOrNull(faker.company.buzzNoun()),
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

class LksgGenerator extends Generator {
  /**
   * Generates a random product
   * @returns a random product
   */
  generateProduct(): LksgProduct {
    return {
      name: faker.commerce.productName(),
      productionSteps: this.randomArray(() => `${faker.word.verb()} ${faker.commerce.productMaterial()}`),
      relatedCorporateSupplyChain: this.valueOrNull(faker.lorem.sentences()),
    };
  }

  /**
   * Generates a random procurement category
   * @returns random procurement category
   */
  generateProcurementCategory(): LksgProcurementCategory {
    const numberOfSuppliersPerCountryCodeAsMap = new Map<string, number>(
      generateArray(() => [generateIso2CountryCode(), this.valueOrNull(faker.number.int({ min: 0, max: 50 }))!]),
    );
    return {
      procuredProductTypesAndServicesNaceCodes: generateNaceCodes(1),
      numberOfSuppliersPerCountryCode: this.valueOrNull(Object.fromEntries(numberOfSuppliersPerCountryCodeAsMap)),
      shareOfTotalProcurementInPercent: this.randomPercentageValue(),
    };
  }

  /**
   * Generates a random map of procurement categories
   * @returns random map of procurement categories
   */
  generateProcurementCategories(): { [key: string]: LksgProcurementCategory } {
    const procurementCategories = Object.values(ProcurementCategoryType);
    const keys = [] as ProcurementCategoryType[];
    procurementCategories.forEach((category) => {
      if (faker.datatype.boolean()) {
        keys.push(category);
      }
    });
    return Object.fromEntries(
      new Map<string, LksgProcurementCategory>(
        keys.map((procurementCategoryType) => [procurementCategoryType as string, this.generateProcurementCategory()]),
      ),
    );
  }

  /**
   * Randomly returns <10%, 10-25%, 25-50% or >50%
   * @returns one of the four percentage intervals as string
   */
  randomShareOfTemporaryWorkersInterval(): ShareOfTemporaryWorkers | null {
    return this.valueOrNull(pickOneElement(Object.values(ShareOfTemporaryWorkers)));
  }

  /**
   * Randomly returns National, International or Both
   * @returns one of the options as string
   */
  randomNationalOrInternationalMarket(): NationalOrInternationalMarket | null {
    return this.valueOrNull(pickOneElement(Object.values(NationalOrInternationalMarket)));
  }
}
