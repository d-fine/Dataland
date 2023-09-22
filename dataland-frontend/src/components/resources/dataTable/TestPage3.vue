<!-- INFO: This file exists to help you understand the new data table and experiment. TODO: Remove -->

<template>
  <MultiLayerDataTable :config="viewConfiguration" :datasets="datasets" style="width: 100%" />
</template>

<style scoped></style>

<script lang="ts">
import { defineComponent } from "vue";
import MultiLayerDataTable from "@/components/resources/dataTable/MultiLayerDataTable.vue";
import { type MLDTDataset } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";

import { convertDataModel } from "@/components/resources/dataTable/conversion/MultiLayerDataTableConfigurationConverter";
import { lksgDataModel } from "@/components/resources/frameworkDataSearch/lksg/LksgDataModel";
import { type LksgData } from "@clients/backend";

const demoDataset: MLDTDataset<LksgData> = {
  headerLabel: "Testing",
  dataset: {
    general: {
      masterData: {
        dataDate: "2024-07-30",
        headOfficeInGermany: "Yes",
        groupOfCompanies: "Yes",
        groupOfCompaniesName: "Emmerich, Swaniawski and Rosenbaum",
        industry: ["G", "A"],
        numberOfEmployees: 9714,
        seasonalOrMigrantWorkers: "No",
        shareOfTemporaryWorkers: "Greater50",
        annualTotalRevenue: 201479614,
        totalRevenueCurrency: "CAD",
        fixedAndWorkingCapital: 2870,
      },
      productionSpecific: {
        manufacturingCompany: "No",
        capacity: "16 Keyboard per Saturday",
        productionViaSubcontracting: "No",
        subcontractingCompaniesCountries: ["BR"],
        subcontractingCompaniesIndustries: ["A", "B", "G", "L"],
        productionSites: "Yes",
        listOfProductionSites: [
          {
            nameOfProductionSite: "Harvey - Hane",
            addressOfProductionSite: {
              streetAndHouseNumber: "Koepp Passage 14188",
              postalCode: "48955",
              city: "New Lonieport",
              state: "Florida",
              country: "LC",
            },
            listOfGoodsOrServices: [
              "Awesome Concrete Tuna",
              "Oriental Wooden Chicken",
              "Unbranded Bronze Gloves",
              "Handcrafted Soft Tuna",
              "Refined Wooden Shirt",
            ],
          },
          {
            nameOfProductionSite: "Wolff - Bernier",
            addressOfProductionSite: {
              streetAndHouseNumber: "Russel Fork 4754",
              postalCode: "27578",
              city: "Carson",
              state: "Pennsylvania",
              country: "ML",
            },
            listOfGoodsOrServices: ["Awesome Concrete Bike", "Handcrafted Concrete Ball"],
          },
        ],
        market: "National",
        specificProcurement: "Yes",
      },
      productionSpecificOwnOperations: {
        mostImportantProducts: [
          {
            name: "Handcrafted Rubber Soap",
          },
          {
            name: "Sleek Metal Shirt",
            relatedCorporateSupplyChain:
              "Aperiam voluptate suscipit est quia accusantium ad. Unde voluptates accusantium delectus neque architecto doloribus nulla earum.",
          },
          {
            name: "Recycled Metal Salad",
            relatedCorporateSupplyChain:
              "Eius quisquam corrupti voluptates reprehenderit autem reprehenderit sunt. Similique harum quaerat tempora quos soluta quod ipsa.",
          },
          {
            name: "Sleek Bronze Car",
            relatedCorporateSupplyChain:
              "Dolore natus qui ipsam voluptatum enim ullam necessitatibus. Minus doloribus expedita voluptas vitae non sint expedita nihil.",
          },
        ],
        productsServicesCategoriesPurchased: {
          Products: {
            procuredProductTypesAndServicesNaceCodes: ["D", "L", "M"],
            percentageOfTotalProcurement: 0.1231,
          },
        },
      },
    },
    governance: {
      riskManagementOwnOperations: {
        riskManagementSystem: "Yes",
        riskAnalysisInFiscalYear: "No",
        risksIdentified: "No",
        identifiedRisks: "functionalities",
        counteractingMeasures: "No",
        whichCounteractingMeasures: "synergies",
        regulatedRiskManagementResponsibility: "No",
        environmentalManagementSystem: "Yes",
        environmentalManagementSystemInternationalCertification: {
          value: "Yes",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        environmentalManagementSystemNationalCertification: {
          value: "Yes",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
      },
      grievanceMechanismOwnOperations: {
        grievanceHandlingMechanism: "Yes",
        grievanceHandlingReportingAccessible: "No",
        appropriateGrievanceHandlingInformation: "Yes",
        appropriateGrievanceHandlingSupport: "No",
        accessToExpertiseForGrievanceHandling: "No",
        grievanceComplaints: "No",
        complaintsNumber: 2875,
        complaintsReason: "infrastructures",
        actionsForComplaintsUndertaken: "Yes",
        whichActionsForComplaintsUndertaken: "relationships",
        publicAccessToGrievanceHandling: "Yes",
        whistleblowerProtection: "No",
        dueDiligenceProcessForGrievanceHandling: "No",
      },
      certificationsPoliciesAndResponsibilities: {
        sa8000Certification: {
          value: "Yes",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        smetaSocialAuditConcept: {
          value: "Yes",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        betterWorkProgramCertificate: {
          value: "NA",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        iso45001Certification: {
          value: "No",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        iso14001Certification: {
          value: "Yes",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        emasCertification: {
          value: "Yes",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        iso37001Certification: {
          value: "Yes",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        iso37301Certification: {
          value: "Yes",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        riskManagementSystemCertification: {
          value: "No",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        amforiBsciAuditReport: {
          value: "Yes",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        responsibleBusinessAssociationCertification: {
          value: "Yes",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        fairLaborAssociationCertification: {
          value: "No",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        additionalAudits: "solutions",
        codeOfConduct: {
          value: "No",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        codeOfConductTraining: "Yes",
        supplierCodeOfConduct: {
          value: "Yes",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        policyStatement: {
          value: "Yes",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        humanRightsStrategy: "markets",
        environmentalImpactPolicy: {
          value: "Yes",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        fairWorkingConditionsPolicy: {
          value: "Yes",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
      },
      generalViolations: {
        responsibilitiesForFairWorkingConditions: "Yes",
        responsibilitiesForTheEnvironment: "Yes",
        responsibilitiesForOccupationalSafety: "Yes",
        legalProceedings: "Yes",
        humanRightsViolationS: "Yes",
        humanRightsViolations: "deliverables",
        humanRightsViolationAction: "Yes",
        humanRightsViolationActionMeasures: "markets",
        highRiskCountriesRawMaterials: "No",
        highRiskCountriesRawMaterialsLocation: ["GT"],
        highRiskCountriesActivity: "No",
        highRiskCountries: ["KN", "MR", "TK", "SN"],
        highRiskCountriesProcurement: "Yes",
        highRiskCountriesProcurementName: ["JM", "GB", "PR"],
      },
    },
    social: {
      childLabor: {
        childLaborPreventionPolicy: {
          value: "No",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        employeeSUnder18: "Yes",
        employeeSUnder15: "Yes",
        employeeSUnder18InApprenticeship: "No",
        worstFormsOfChildLaborProhibition: "No",
        worstFormsOfChildLabor: "No",
        worstFormsOfChildLaborForms: "relationships",
        measuresForPreventionOfEmploymentUnderLocalMinimumAge: "Yes",
        employmentUnderLocalMinimumAgePreventionEmploymentContracts: "Yes",
        employmentUnderLocalMinimumAgePreventionJobDescription: "Yes",
        employmentUnderLocalMinimumAgePreventionIdentityDocuments: "No",
        employmentUnderLocalMinimumAgePreventionTraining: "No",
        employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: "No",
        additionalChildLaborMeasures: "action-items",
      },
      forcedLaborSlavery: {
        forcedLaborAndSlaveryPrevention: "No",
        forcedLaborAndSlaveryPreventionPractices: "content",
        forcedLaborPreventionPolicy: {
          value: "No",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        forcedLaborAndSlaveryPreventionMeasures: "Yes",
        forcedLaborAndSlaveryPreventionEmploymentContracts: "No",
        forcedLaborAndSlaveryPreventionIdentityDocuments: "No",
        forcedLaborAndSlaveryPreventionFreeMovement: "No",
        forcedLaborAndSlaveryPreventionProvisionSocialRoomsAndToilets: "Yes",
        forcedLaborAndSlaveryPreventionTraining: "No",
        forcedLaborAndSlaveryPreventionMeasuresOther: "networks",
      },
      withholdingAdequateWages: {
        adequateWage: "Yes",
        adequateWagesMeasures: "Yes",
        documentedWorkingHoursAndWages: "Yes",
        adequateLivingWage: "Yes",
        regularWagesProcessFlow: "No",
        fixedHourlyWages: "No",
        fixedPieceworkWages: "NA",
        adequateWageMeasures: "portals",
      },
      disregardForOccupationalHealthSafety: {
        lowSkillWork: "Yes",
        hazardousMachines: "No",
        oshPolicy: "No",
        oshPolicyPersonalProtectiveEquipment: "Yes",
        oshPolicyMachineSafety: "Yes",
        oshPolicyDisasterBehavioralResponse: "Yes",
        oshPolicyAccidentsBehavioralResponse: "No",
        oshPolicyWorkplaceErgonomics: "Yes",
        oshPolicyAccessToWork: "Yes",
        oshPolicyHandlingChemicalsAndOtherHazardousSubstances: "Yes",
        oshPolicyFireProtection: "No",
        oshPolicyWorkingHours: "Yes",
        oshPolicyTrainingAddressed: "No",
        oshPolicyTraining: "No",
        oshManagementSystem: "No",
        oshManagementSystemInternationalCertification: {
          value: "Yes",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        oshManagementSystemNationalCertification: {
          value: "Yes",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        under10WorkplaceAccidents: "Yes",
        oshTraining: "No",
        healthAndSafetyPolicy: {
          value: "Yes",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
      },
      disregardForFreedomOfAssociation: {
        freedomOfAssociation: "Yes",
        employeeRepresentation: 0.479,
        discriminationForTradeUnionMembers: "No",
        freedomOfOperationForTradeUnion: "Yes",
        freedomOfAssociationTraining: "No",
        worksCouncil: "No",
      },
      unequalTreatmentOfEmployment: {
        unequalTreatmentOfEmployment: "No",
        diversityAndInclusionRole: "Yes",
        preventionOfMistreatments: "No",
        equalOpportunitiesOfficer: "Yes",
        fairAndEthicalRecruitmentPolicy: {
          value: "Yes",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        equalOpportunitiesAndNonDiscriminationPolicy: {
          value: "Yes",
          dataSource: {
            name: "Policy",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
      },
      contaminationOfSoilWaterAirNoiseEmissionsExcessiveWaterConsumption: {
        harmfulSoilImpact: "Yes",
        soilDegradation: "No",
        soilErosion: "No",
        soilBorneDiseases: "No",
        soilContamination: "Yes",
        soilSalinization: "No",
        harmfulWaterPollution: "Yes",
        fertilizersOrPollutants: "Yes",
        wasteWaterFiltration: "Yes",
        harmfulAirPollution: "Yes",
        airFiltration: "No",
        harmfulNoiseEmission: "No",
        reduceNoiseEmissions: "Yes",
        excessiveWaterConsumption: "Yes",
        waterSavingMeasures: "Yes",
        waterSavingMeasuresName: "partnerships",
        pipeMaintaining: "Yes",
        waterSources: "No",
        contaminationMeasures: "ROI",
      },
      unlawfulEvictionDeprivationOfLandForestAndWater: {
        unlawfulEvictionAndTakingOfLand: "No",
        unlawfulEvictionAndTakingOfLandRisk: "deliverables",
        unlawfulEvictionAndTakingOfLandStrategies: "Yes",
        unlawfulEvictionAndTakingOfLandStrategiesName: "methodologies",
        voluntaryGuidelinesOnTheResponsibleGovernanceOfTenure: "Yes",
      },
      useOfPrivatePublicSecurityForcesWithDisregardForHumanRights: {
        useOfPrivatePublicSecurityForces: "No",
        useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: "Yes",
        instructionOfSecurityForces: "Yes",
        humanRightsTraining: "Yes",
        stateSecurityForces: "Yes",
        privateSecurityForces: "No",
        useOfPrivatePublicSecurityForcesMeasures: "markets",
      },
    },
    environmental: {
      useOfMercuryMercuryWasteMinamataConvention: {
        mercuryAndMercuryWasteHandling: "No",
        mercuryAndMercuryWasteHandlingPolicy: {
          value: "No",
          dataSource: {
            name: "Certification",
            reference: "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
          },
        },
        mercuryAddedProductsHandling: "Yes",
        mercuryAddedProductsHandlingRiskOfExposure: "No",
        mercuryAddedProductsHandlingRiskOfDisposal: "Yes",
        mercuryAndMercuryCompoundsProductionAndUse: "Yes",
        mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: "No",
      },
      productionAndUseOfPersistentOrganicPollutantsPopsConvention: {
        persistentOrganicPollutantsProductionAndUse: "No",
        persistentOrganicPollutantsUsed: "platforms",
        persistentOrganicPollutantsProductionAndUseRiskOfExposure: "No",
        persistentOrganicPollutantsProductionAndUseRiskOfDisposal: "No",
        legalRestrictedWasteProcesses: "Yes",
      },
      exportImportOfHazardousWasteBaselConvention: {
        persistentOrganicPollutantsProductionAndUseTransboundaryMovements: "No",
        persistentOrganicPollutantsProductionAndUseRiskForImportingState: "Yes",
        hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein: "Yes",
        hazardousWasteTransboundaryMovementsOutsideOecdEuOrLiechtenstein: "Yes",
        hazardousWasteDisposal: "Yes",
        hazardousWasteDisposalRiskOfImport: "No",
        hazardousWasteDisposalOtherWasteImport: "Yes",
      },
    },
  },
};

const viewConfiguration = convertDataModel(lksgDataModel);
console.log(viewConfiguration);

export default defineComponent({
  name: "TestPage3",
  components: { MultiLayerDataTable },
  data() {
    return {
      viewConfiguration,
      datasets: [demoDataset],
    };
  },
});
</script>
