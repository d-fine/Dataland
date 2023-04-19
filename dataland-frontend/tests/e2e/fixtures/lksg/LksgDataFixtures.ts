import { faker } from "@faker-js/faker";
import {
  InHouseProductionOrContractProcessing,
  LksgAddress,
  LksgData,
  NationalOrInternationalMarket, ShareOfTemporaryWorkers,
} from "@clients/backend";
import {randomYesNo, randomYesNoNaUndefined, randomYesNoUndefined} from "@e2e/fixtures/common/YesNoFixtures";
import { randomFutureDate } from "@e2e/fixtures/common/DateFixtures";
import { generateIso4217CurrencyCode } from "@e2e/fixtures/common/CurrencyFixtures";
import {
  randomListOfStringOrUndefined,
  randomStringOrUndefined
} from "@e2e/utils/FakeFixtureUtils";
import { getRandomReportingPeriod } from "@e2e/fixtures/common/ReportingPeriodFixtures";
import { generateFixtureDataset } from "@e2e/fixtures/FixtureUtils";
import { FixtureData } from "@sharedUtils/Fixtures";
import {
  randomEuroValue,
  randomNumber,
  randomNumberOrUndefined,
  randomPercentageValue
} from "@e2e/fixtures/common/NumberFixtures";
import {
  generateIso2CountryCode,
  generateListOfIso2CountryCodes,
  generateListOfIso2CountryCodesOrUndefined
} from "@e2e/fixtures/common/CountryFixtures";

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
    (dataSet) => dataSet?.general?.masterData?.dataDate?.substring(0, 4) || getRandomReportingPeriod()
  );
}

/**
 * Generates a random list of goods or services
 *
 * @returns random list of goods or services
 */
export function generateListOfGoodsOrServices(): string[] {
  const fakeGoodsOrServices = Array.from({ length: faker.datatype.number({ min: 0, max: 5 }) }, () => {
    return faker.commerce.productName();
  });
  return fakeGoodsOrServices
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
 * Generates a random address
 *
 * @returns a random address
 */
export function generateAddress(): LksgAddress {
  return {
    streetAndHouseNumber: faker.address.street() + " " + faker.address.buildingNumber(),
    city: faker.address.city(),
    state: faker.address.state(),
    postalCode: faker.address.zipCode(),
    country: faker.address.country()
  }
}

/**
 * Generates an array consisting of 1 to 5 random addresses
 *
 * @returns 1 to 5 random addresses
 */
export function generateListOfAddresses(): LksgAddress[] {
  return Array.from({ length: faker.datatype.number({ min: 0, max: 5 }) }, generateAddress);
}

/**
 * Randomly returns <10%, 10-25%, 25-50% or >50%
 *
 * @returns one of the four percentage intervals as string
 */
export function randomShareOfTemporaryWorkersInterval(): ShareOfTemporaryWorkers {
  return faker.helpers.arrayElement(Object(ShareOfTemporaryWorkers));
}

/**
 * Generates a random LKSG dataset
 *
 * @param dataDate Optional parameter if a specific date should be set instead of a random one
 * @returns a random LKSG dataset
 */
export function generateLksgData(dataDate?: string): LksgData {
  return {
    general: {
      masterData: {
        dataDate: dataDate === undefined ? randomFutureDate() : dataDate,
        name: faker.company.name(),
        address: generateAddress(),
        headOffice: randomYesNo(),
        commercialRegister: generateVatIdentificationNumber(),
        groupOfCompanies: randomYesNo(),
        groupOfCompaniesName: faker.company.name(),
        industry: faker.name.jobArea(),
        numberOfEmployees: randomNumber(10000),
        seasonalOrMigrantWorkers: randomYesNo(),
        shareOfTemporaryWorkers: randomShareOfTemporaryWorkersInterval(),
        totalRevenueCurrency: generateIso4217CurrencyCode(),
        totalRevenue: randomEuroValue(),
        fixedAndWorkingCapital: randomNumberOrUndefined(10000000),
      },
      productionspecific: {
        listOfProductionSites: randomYesNo(),
        capacity: randomEuroValue(),
        isInhouseProductionOrIsContractProcessing: faker.helpers.arrayElement([
            InHouseProductionOrContractProcessing.InHouseProduction,
            InHouseProductionOrContractProcessing.ContractProcessing,
        ]),
        subcontractingCompaniesCountries: generateListOfIso2CountryCodes(),
        subcontractingCompaniesIndustries: Array.from(
            { length: faker.datatype.number({ min: 0, max: 5 }) },
            faker.name.jobArea
        ),
        productionSites: randomYesNo(),
        numberOfProductionSites: randomNumber(1000),
        nameOfProductionSites: [faker.company.name()],
        addressesOfProductionSites: generateListOfAddresses(),
        listOfGoodsOrServices: generateListOfGoodsOrServices(),
        market: faker.helpers.arrayElement([
            NationalOrInternationalMarket.National,
            NationalOrInternationalMarket.International,
            NationalOrInternationalMarket.Both
        ]),
        specificProcurement: randomYesNo(),
      },
      productionspecificOwnOperations: {
        mostImportantProducts: randomListOfStringOrUndefined(""),
        productionSteps: randomListOfStringOrUndefined(""),
        relatedCorporateSupplyChain: randomListOfStringOrUndefined(""),
        productCategories: randomListOfStringOrUndefined(""),
        definitionProductTypeService: randomListOfStringOrUndefined(""),
        sourcingCountryPerCategory: Array.from(
            { length: faker.datatype.number({ min: 0, max: 5 }) },
            generateIso2CountryCode
        ),
        numberOfDirectSuppliers: randomNumberOrUndefined(100),
        orderVolumePerProcurement: randomNumberOrUndefined(1000),
      },
      riskManagementOwnOperations: {
        adequateAndEffectiveRiskManagementSystem: randomYesNoUndefined(),
        riskManagementSystemFiscalYear: randomYesNoUndefined(),
        riskManagementSystemRisks: randomYesNoUndefined(),
        riskManagementSystemIdentifiedRisks: randomListOfStringOrUndefined(""),
        riskManagementSystemCounteract: randomYesNoUndefined(),
        riskManagementSystemMeasures: randomListOfStringOrUndefined(""),
        riskManagementSystemResponsibility: randomYesNoUndefined(),
        environmentalManagementSystem: randomYesNoUndefined(),
        environmentalManagementSystemInternationalCertification: randomYesNoUndefined(),
        environmentalManagementSystemNationalCertification: randomYesNoUndefined(),
      },
      grievanceMechanismOwnOperations: {
        grievanceHandlingMechanism: randomYesNoUndefined(),
        grievanceHandlingMechanismUsedForReporting: randomYesNoUndefined(),
        grievanceMechanismInformationProvided: randomYesNoUndefined(),
        grievanceMechanismSupportProvided: randomYesNoUndefined(),
        grievanceMechanismAccessToExpertise: randomYesNoUndefined(),
        grievanceMechanismComplaints: randomYesNoUndefined(),
        grievanceMechanismComplaintsNumber: randomNumberOrUndefined(100),
        grievanceMechanismComplaintsReason: randomListOfStringOrUndefined(""),
        grievanceMechanismComplaintsAction: randomYesNoUndefined(),
        grievanceMechanismComplaintsActionUndertaken: randomListOfStringOrUndefined(""),
        grievanceMechanismPublicAccess: randomYesNoUndefined(),
        grievanceMechanismProtection: randomYesNoUndefined(),
        grievanceMechanismDueDiligenceProcess: randomYesNoUndefined(),
      },
    },
    governance: {
      evidenceCertificatesAndAttestations: {
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
        responsibleBusinessAssociationCertification: randomYesNo(),
        fairLaborAssociationCertification: randomYesNo(),
        additionalAudits: randomListOfStringOrUndefined(""),
      },
      humanRights: {
        codeOfConduct: randomYesNo(),
        codeOfConductTraining: randomYesNo(),
        supplierCodeOfConduct: randomYesNo(),
        policyStatement: randomYesNo(),
        humanRightsStrategy: randomListOfStringOrUndefined(""),
        environmentalImpactPolicy: randomYesNo(),
        fairWorkingConditionsPolicy: randomYesNo(),
        responsibilitiesForFairWorkingConditions: randomYesNo(),
        responsibilitiesForTheEnvironment: randomYesNo(),
        responsibilitiesForOccupationalSafety: randomYesNo(),
        legalProceedings: randomYesNo(),
        humanRightsViolation: randomYesNo(),
        humanRightsViolationLocation: [""],
        humanRightsViolationAction: randomYesNo(),
        humanRightsViolationActionMeasures: [""],
        highRiskCountriesRawMaterials: randomYesNo(),
        highRiskCountriesRawMaterialsLocation: generateListOfIso2CountryCodes(),
        highRiskCountriesActivity: randomYesNo(),
        highRiskCountries: generateListOfIso2CountryCodes(),
        highRiskCountriesProcurement: randomYesNo(),
        highRiskCountriesProcurementName: generateListOfIso2CountryCodesOrUndefined(),
      },
    },
    social: {
      childLabor: {
        employeeUnder18: randomYesNoUndefined(),
        employeeUnder18Under15: randomYesNoUndefined(),
        employeeUnder18Apprentices: randomYesNoUndefined(),
        worstFormsOfChildLabor: randomYesNoUndefined(),
        worstFormsOfChildLaborForms: randomListOfStringOrUndefined(""),
        employmentUnderLocalMinimumAgePrevention: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePreventionEmploymentContracts: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePreventionJobDescription: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePreventionIdentityDocuments: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePreventionTraining: randomYesNoUndefined(),
        employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: randomYesNoUndefined(),
        childLaborMeasures: randomListOfStringOrUndefined(""),
        childLaborPolicy: randomYesNoUndefined(),
      },
      forcedLaborSlavery: {
        forcedLaborAndSlaveryPrevention: randomYesNoUndefined(),
        forcedLaborAndSlaveryPreventionPractices: randomListOfStringOrUndefined(""),
        forcedLaborAndSlaveryPreventionMeasures: randomYesNoUndefined(),
        forcedLaborAndSlaveryPreventionEmploymentContracts: randomYesNoUndefined(),
        forcedLaborAndSlaveryPreventionIdentityDocuments: randomYesNoUndefined(),
        forcedLaborAndSlaveryPreventionFreeMovement: randomYesNoUndefined(),
        forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets: randomYesNoUndefined(),
        forcedLaborAndSlaveryPreventionTraining: randomYesNoUndefined(),
        forcedLaborAndSlaveryMeasures: randomListOfStringOrUndefined(""),
        forcedLaborPolicy: randomYesNoUndefined(),
      },
      withholdingAdequateWages: {
        adequatWage: randomYesNoUndefined(),
        adequatWageBeingWithheld: randomYesNoUndefined(),
        documentedWorkingHoursAndWages: randomYesNoUndefined(),
        adequateLivingWage: randomYesNoUndefined(),
        regularWagesProcessFlow: randomYesNoUndefined(),
        fixedHourlyWages: randomYesNoNaUndefined(),
        fixedPieceworkWages: randomYesNoNaUndefined(),
        adequateWageMeasures: randomListOfStringOrUndefined(""),
      },
      disregardForOccupationalHealthSafety: {
        lowSkillWork: randomYesNoUndefined(),
        hazardousMachines: randomYesNoUndefined(),
        oshPolicy: randomYesNoUndefined(),
        oshPolicyPersonalProtectiveEquipment: randomYesNoNaUndefined(),
        oshPolicyMachineSafety: randomYesNoNaUndefined(),
        oshPolicyDisasterBehaviouralResponse: randomYesNoUndefined(),
        oshPolicyAccidentsBehaviouralResponse: randomYesNoUndefined(),
        oshPolicyWorkplaceErgonomics: randomYesNoUndefined(),
        oshPolicyAccessToWork: randomYesNoUndefined(),
        oshPolicyHandlingChemicalsAndOtherHazardousSubstances: randomYesNoNaUndefined(),
        oshPolicyFireProtection: randomYesNoUndefined(),
        oshPolicyWorkingHours: randomYesNoUndefined(),
        oshPolicyTrainingAddressed: randomYesNoUndefined(),
        oshPolicyTraining: randomYesNoUndefined(),
        oshManagementSystem: randomYesNoUndefined(),
        oshManagementSystemInternationalCertification: randomYesNoUndefined(),
        oshManagementSystemNationalCertification: randomYesNoUndefined(),
        workplaceAccidentsUnder10: randomYesNoUndefined(),
        oshTraining: randomYesNoUndefined(),
        healthAndSafetyPolicy: randomYesNoUndefined(),
      },
      disregardForFreedomOfAssociation: {
        freedomOfAssociation: randomYesNoUndefined(),
        representedEmployees: randomPercentageValue(),
        discriminationForTradeUnionMembers: randomYesNoUndefined(),
        freedomOfOperationForTradeUnion: randomYesNoUndefined(),
        freedomOfAssociationTraining: randomYesNoUndefined(),
        worksCouncil: randomYesNoUndefined(),
      },
      unequalTreatmentOfEmployment: {
        unequalTreatmentOfEmployment: randomYesNoUndefined(),
        diversityAndInclusionRole: randomYesNoUndefined(),
        preventionOfMistreatments: randomYesNoUndefined(),
        equalOpportunitiesOfficer: randomYesNoUndefined(),
        fairAndEthicalRecruitmentPolicy: randomYesNoUndefined(),
        equalOpportunitiesAndNondiscriminationPolicy: randomYesNoUndefined(),
      },
      contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption: {
        harmfulSoilChange: randomYesNoUndefined(),
        soilDegradation: randomYesNoUndefined(),
        soilErosion: randomYesNoUndefined(),
        soilBornDiseases: randomYesNoUndefined(),
        soilContamination: randomYesNoUndefined(),
        soilSalinisation: randomYesNoUndefined(),
        harmfulWaterPollution: randomYesNoUndefined(),
        fertilisersOrPollutants: randomYesNoUndefined(),
        wasteWaterFiltration: randomYesNoUndefined(),
        harmfulAirPollution: randomYesNoUndefined(),
        airFiltration: randomYesNoUndefined(),
        harmfulNoiseEmission: randomYesNoUndefined(),
        reduceNoiseEmissions: randomYesNoUndefined(),
        excessiveWaterConsumption: randomYesNoUndefined(),
        waterSavingMeasures: randomYesNoUndefined(),
        waterSavingMeasuresName: randomListOfStringOrUndefined(""),
        pipeMaintaining: randomYesNoUndefined(),
        waterSources: randomYesNoUndefined(),
        contaminationMeasures: randomListOfStringOrUndefined(""),
      },
      unlawfulEvictionDeprivationOfLandForestAndWater: {
        unlawfulEvictionAndTakingOfLand: randomYesNoUndefined(),
        unlawfulEvictionAndTakingOfLandRisk: randomStringOrUndefined(""),
        unlawfulEvictionAndTakingOfLandStrategies: randomYesNoUndefined(),
        unlawfulEvictionAndTakingOfLandStrategiesName: randomListOfStringOrUndefined(""),
        voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure: randomYesNoUndefined(),
      },
      useOfPrivatePublicSecurityForcesWithDisregardForHumanRights: {
        useOfPrivatePublicSecurityForces: randomYesNoUndefined(),
        useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: randomYesNoUndefined(),
        instructionOfSecurityForces: randomYesNoUndefined(),
        humanRightsTraining: randomYesNoUndefined(),
        stateSecurityForces:randomYesNoNaUndefined(),
        privateSecurityForces: randomYesNoNaUndefined(),
        useOfPrivatePublicSecurityForcesMeasures: randomListOfStringOrUndefined(""),
      },
    },
    environmental: {
      useOfMercuryMercuryWasteMinamataConvention: {
        mercuryAndMercuryWasteHandling: randomYesNoUndefined(),
        mercuryAndMercuryWasteHandlingPolicy: randomYesNoUndefined(),
        mercuryAddedProductsHandling: randomYesNoUndefined(),
        mercuryAddedProductsHandlingRiskOfExposure: randomYesNoUndefined(),
        mercuryAddedProductsHandlingRiskOfDisposal: randomYesNoUndefined(),
        mercuryAndMercuryCompoundsProductionAndUse: randomYesNoUndefined(),
        mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: randomYesNoUndefined(),
      },
      productionAndUseOfPersistentOrganicPollutantsPopsConvention: {
        persistentOrganicPollutantsProductionAndUse: randomYesNoUndefined(),
        persistentOrganicPollutantsUsed: randomListOfStringOrUndefined(""),
        persistentOrganicPollutantsProductionAndUseRiskOfExposure: randomYesNoUndefined(),
        persistentOrganicPollutantsProductionAndUseRiskOfDisposal: randomYesNoUndefined(),
        legalRestrictedWasteProcesses: randomYesNoUndefined(),
      },
      exportImportOfHazardousWasteBaselConvention: {
        persistentOrganicPollutantsProductionAndUseTransboundaryMovements: randomYesNoUndefined(),
        persistentOrganicPollutantsProductionAndUseRiskForImportingState: randomYesNoUndefined(),
        hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein: randomYesNoUndefined(),
        hazardousWasteTransboundaryMovementsOutsideOecdEuLiechtenstein: randomYesNoUndefined(),
        hazardousWasteDisposal: randomYesNoUndefined(),
        hazardousWasteDisposalRiskOfImport: randomYesNoUndefined(),
        hazardousAndOtherWasteImport: randomYesNoUndefined(),
      },
    },
  };
}
