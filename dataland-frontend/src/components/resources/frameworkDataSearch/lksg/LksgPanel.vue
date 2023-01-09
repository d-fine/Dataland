<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="dataSet && !waitingForData">
    <DetailCompanyDataTable :dataSet="kpisDataObjects" :dataSetColumns="dataSetColumns" />
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { LksgData } from "@clients/backend";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import DetailCompanyDataTable from "@/components/general/DetailCompanyDataTable.vue";
import { LksgKpisImpactArea } from "@/components/resources/frameworkDataSearch/lksg/LksgModels";

export default defineComponent({
  name: "LksgPanel",
  components: { DetailCompanyDataTable },
  data() {
    return {
      waitingForData: true,
      dataSet: [
        {
          "betterWorkProgramCertificate": "No",
          "dataDate": "2023-07-19",
          "companyLegalForm": "Sole Trader",
          "vatIdentificationNumber": "BJ564339879",
          "numberOfEmployees": 189085,
          "shareOfTemporaryWorkers": 4.1555,
          "totalRevenue": 23691057301.85,
          "totalRevenueCurrency": "CHF",
          "responsibilitiesForFairWorkingConditions": "No",
          "responsibilitiesForOccupationalSafety": "Yes",
          "riskManagementSystem": "No",
          "grievanceHandlingMechanismUsedForReporting": "No",
          "codeOfConduct": "No",
          "codeOfConductTraining": "Yes",
          "legalProceedings": "Yes",
          "employeeUnder18Under15": "No",
          "employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge": "No",
          "forcedLabourAndSlaveryPrevention": "No",
          "forcedLabourAndSlaveryPreventionIdentityDocuments": "Yes",
          "forcedLabourAndSlaveryPreventionFreeMovement": "No",
          "forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets": "Yes",
          "forcedLabourAndSlaveryPreventionProvisionTraining": "No",
          "adequateLivingWage": "No",
          "regularWagesProcessFlow": "No",
          "fixedHourlyWages": "Yes",
          "workplaceAccidentsUnder10": "No",
          "freedomOfAssociation": "Yes",
          "discriminationForTradeUnionMembers": "No",
          "freedomOfOperationForTradeUnion": "Yes",
          "worksCouncil": "No",
          "diversityAndInclusionRole": "Yes",
          "equalOpportunitiesOfficer": "Yes",
          "riskOfHarmfulPollution": "Yes",
          "unlawfulEvictionAndTakingOfLand": "Yes",
          "useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights": "No",
          "mercuryAndMercuryWasteHandlingPolicy": "Yes",
          "chemicalHandling": "Yes",
          "environmentalManagementSystem": "No",
          "legalRestrictedWaste": "No",
          "mercuryAddedProductsHandling": "Yes",
          "mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure": "Yes",
          "persistentOrganicPollutantsProductionAndUse": "Yes",
          "persistentOrganicPollutantsProductionAndUseRiskOfDisposal": "Yes",
          "hazardousWasteDisposal": "No",
          "hazardousAndOtherWasteImport": "Yes",
          "riskManagementSystemCertification": "Yes",
          "amforiBsciAuditReport": "Yes",
          "fairLabourAssociationCertification": "No",
          "lksgInScope": "No",
          "oshMonitoring": "No",
          "oshPolicy": "Yes",
          "smetaSocialAuditConcept": "Yes",
          "iso45001Certification": "No",
          "iso14000Certification": "No",
          "sa8000Certification": "Yes",
          "iso37001Certification": "Yes",
          "iso37301Certification": "Yes",
          "oshPolicyFireProtection": "Yes",
          "oshPolicyWorkingHours": "Yes",
          "oshManagementSystem": "No",
          "oshPolicyWorkplaceErgonomics": "No",
          "oshPolicyTraining": "Yes",
          "oshPolicyPersonalProtectiveEquipment": "No",
          "oshPolicyAccidentsBehaviouralResponse": "Yes",
          "oshPolicyDisasterBehaviouralResponse": "Yes",
          "oshPolicyHandlingChemicalsAndOtherHazardousSubstances": "Yes",
          "equalOpportunitiesAndNondiscriminationPolicy": "No",
          "healthAndSafetyPolicy": "Yes",
          "complaintsAndGrievancesPolicy": "No"
          // "listOfProductionSites": [
          //   {
          //     "name": "Merseburg Gruppe",
          //     "isInHouseProductionOrIsContractProcessing": "No",
          //     "address": "Köttershof 0, 76326 Nord Joschua, Libyen",
          //     "listOfGoodsAndServices": [
          //       "Elegant Granite Chips",
          //       "Tasty Bronze Mouse"
          //     ]
          //   }
          // ]
        },
        {
          "betterWorkProgramCertificate": "No",
          "dataDate": "2023-01-01",
          "companyLegalForm": "Sole Trader",
          "vatIdentificationNumber": "BJ564339879",
          "numberOfEmployees": 189085,
          "shareOfTemporaryWorkers": 4.1555,
          "totalRevenue": 23691057301.85,
          "totalRevenueCurrency": "ABC",
          "responsibilitiesForFairWorkingConditions": "No",
          "responsibilitiesForOccupationalSafety": "Yes",
          "riskManagementSystem": "No",
          "grievanceHandlingMechanismUsedForReporting": "No",
          "codeOfConduct": "No",
          "codeOfConductTraining": "Yes",
          "legalProceedings": "Yes",
          "employeeUnder18Under15": "No",
          "employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge": "No",
          "forcedLabourAndSlaveryPrevention": "No",
          "forcedLabourAndSlaveryPreventionIdentityDocuments": "Yes",
          "forcedLabourAndSlaveryPreventionFreeMovement": "No",
          "forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets": "Yes",
          "forcedLabourAndSlaveryPreventionProvisionTraining": "No",
          "adequateLivingWage": "No",
          "regularWagesProcessFlow": "No",
          "fixedHourlyWages": "Yes",
          "workplaceAccidentsUnder10": "No",
          "freedomOfAssociation": "Yes",
          "discriminationForTradeUnionMembers": "No",
          "freedomOfOperationForTradeUnion": "Yes",
          "worksCouncil": "No",
          "diversityAndInclusionRole": "Yes",
          "equalOpportunitiesOfficer": "Yes",
          "riskOfHarmfulPollution": "Yes",
          "unlawfulEvictionAndTakingOfLand": "Yes",
          "useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights": "No",
          "mercuryAndMercuryWasteHandlingPolicy": "Yes",
          "chemicalHandling": "Yes",
          "environmentalManagementSystem": "No",
          "legalRestrictedWaste": "No",
          "mercuryAddedProductsHandling": "Yes",
          "mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure": "Yes",
          "persistentOrganicPollutantsProductionAndUse": "Yes",
          "persistentOrganicPollutantsProductionAndUseRiskOfDisposal": "Yes",
          "hazardousWasteDisposal": "No",
          "hazardousAndOtherWasteImport": "Yes",
          "riskManagementSystemCertification": "Yes",
          "amforiBsciAuditReport": "Yes",
          "fairLabourAssociationCertification": "No",
          "lksgInScope": "No",
          "oshMonitoring": "No",
          "oshPolicy": "Yes",
          "smetaSocialAuditConcept": "Yes",
          "iso45001Certification": "No",
          "iso14000Certification": "No",
          "sa8000Certification": "Yes",
          "iso37001Certification": "Yes",
          "iso37301Certification": "Yes",
          "oshPolicyFireProtection": "Yes",
          "oshPolicyWorkingHours": "Yes",
          "oshManagementSystem": "No",
          "oshPolicyWorkplaceErgonomics": "No",
          "oshPolicyTraining": "Yes",
          "oshPolicyPersonalProtectiveEquipment": "No",
          "oshPolicyAccidentsBehaviouralResponse": "Yes",
          "oshPolicyDisasterBehaviouralResponse": "Yes",
          "oshPolicyHandlingChemicalsAndOtherHazardousSubstances": "Yes",
          "equalOpportunitiesAndNondiscriminationPolicy": "No",
          "healthAndSafetyPolicy": "Yes",
          "complaintsAndGrievancesPolicy": "No"
          // "listOfProductionSites": [
          //   {
          //     "name": "Merseburg Gruppe",
          //     "isInHouseProductionOrIsContractProcessing": "No",
          //     "address": "Köttershof 0, 76326 Nord Joschua, Libyen",
          //     "listOfGoodsAndServices": [
          //       "Elegant Granite Chips",
          //       "Tasty Bronze Mouse"
          //     ]
          //   }
          // ]
        },
        {
          "betterWorkProgramCertificate": "No",
          "dataDate": "2021-00-00",
          "companyLegalForm": "Trader Trader",
          "vatIdentificationNumber": "BJ564339879",
          "numberOfEmployees": 189085,
          "shareOfTemporaryWorkers": 4.14,
          "totalRevenue": 23691057301.85,
          "totalRevenueCurrency": "CHF",
          "responsibilitiesForFairWorkingConditions": "No",
          "responsibilitiesForOccupationalSafety": "Yes",
          "riskManagementSystem": "No",
          "grievanceHandlingMechanismUsedForReporting": "No",
          "codeOfConduct": "No",
          "codeOfConductTraining": "Yes",
          "legalProceedings": "Yes",
          "employeeUnder18Under15": "No",
          "employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge": "No",
          "forcedLabourAndSlaveryPrevention": "No",
          "forcedLabourAndSlaveryPreventionIdentityDocuments": "Yes",
          "forcedLabourAndSlaveryPreventionFreeMovement": "No",
          "forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets": "Yes",
          "forcedLabourAndSlaveryPreventionProvisionTraining": "No",
          "adequateLivingWage": "No",
          "regularWagesProcessFlow": "No",
          "fixedHourlyWages": "Yes",
          "workplaceAccidentsUnder10": "No",
          "freedomOfAssociation": "Yes",
          "discriminationForTradeUnionMembers": "No",
          "freedomOfOperationForTradeUnion": "Yes",
          "worksCouncil": "No",
          "diversityAndInclusionRole": "Yes",
          "equalOpportunitiesOfficer": "Yes",
          "riskOfHarmfulPollution": "Yes",
          "unlawfulEvictionAndTakingOfLand": "Yes",
          "useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights": "No",
          "mercuryAndMercuryWasteHandlingPolicy": "Yes",
          "chemicalHandling": "Yes",
          "environmentalManagementSystem": "No",
          "legalRestrictedWaste": "No",
          "mercuryAddedProductsHandling": "Yes",
          "mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure": "Yes",
          "persistentOrganicPollutantsProductionAndUse": "Yes",
          "persistentOrganicPollutantsProductionAndUseRiskOfDisposal": "Yes",
          "hazardousWasteDisposal": "No",
          "hazardousAndOtherWasteImport": "Yes",
          "riskManagementSystemCertification": "Yes",
          "amforiBsciAuditReport": "Yes",
          "fairLabourAssociationCertification": "No",
          "lksgInScope": "No",
          "oshMonitoring": "No",
          "oshPolicy": "Yes",
          "smetaSocialAuditConcept": "Yes",
          "iso45001Certification": "No",
          "iso14000Certification": "No",
          "sa8000Certification": "Yes",
          "iso37001Certification": "Yes",
          "iso37301Certification": "Yes",
          "oshPolicyFireProtection": "Yes",
          "oshPolicyWorkingHours": "Yes",
          "oshManagementSystem": "No",
          "oshPolicyWorkplaceErgonomics": "No",
          "oshPolicyTraining": "Yes",
          "oshPolicyPersonalProtectiveEquipment": "No",
          "oshPolicyAccidentsBehaviouralResponse": "Yes",
          "oshPolicyDisasterBehaviouralResponse": "Yes",
          "oshPolicyHandlingChemicalsAndOtherHazardousSubstances": "Yes",
          "equalOpportunitiesAndNondiscriminationPolicy": "No",
          "healthAndSafetyPolicy": "Yes",
          "complaintsAndGrievancesPolicy": "No"
          // "listOfProductionSites": [
          //   {
          //     "name": "Merseburg Gruppe",
          //     "isInHouseProductionOrIsContractProcessing": "No",
          //     "address": "Köttershof 0, 76326 Nord Joschua, Libyen",
          //     "listOfGoodsAndServices": [
          //       "Elegant Granite Chips",
          //       "Tasty Bronze Mouse"
          //     ]
          //   }
          // ]
        },
      ],
      newDataSet: {},
      dataSetColumns: [] as string[],
      kpisDataObjects: [],
      LksgKpisImpactArea,
    };
  },
  props: {
    dataID: {
      type: String,
      default: "abcd9788-7736-450b-93a7-41b0c53a042a:da252d0d-00ec-4534-b3fb-d82b1f0cf487_21cc4170-ed47-4e44-8fc9-c7ad4565e2a8",
    },
  },
  mounted() {
    void this.getCompanyLksgDataset();
    const dateRegex = "^\\d{4}-\\d{2}-\\d{2}$";

    this.dataSet.forEach( oneDataObject => {
        if (oneDataObject.dataDate && oneDataObject.dataDate.match(dateRegex)) {
          this.dataSetColumns.push(oneDataObject.dataDate)
          this.newDataSet = {...this.newDataSet, [oneDataObject.dataDate]:oneDataObject}
        }
    })

    this.dataSet.forEach( el => {
      for (const [key, value] of Object.entries(el)) {

        if (!this.kpisDataObjects.some(e => e.kpi === key)) {
          let kpiDataObject = {
            kpi: `${key}`,
            group: this.LksgKpisImpactArea[key],

          };
          this.dataSetColumns.forEach( dataDate => {
            kpiDataObject = {...kpiDataObject, [dataDate]: this.newDataSet[`${dataDate}`][`${key}`]}

          })
          this.kpisDataObjects.push(kpiDataObject);

        } else {
          return;
        }
      }
    })
  },
  watch: {
    dataID() {
      void this.getCompanyLksgDataset();
    },
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  methods: {
    async getCompanyLksgDataset() {
      try {
        this.waitingForData = true;
        if (this.dataID != "loading") {
          const LksgDataControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)()
          ).getLksgDataControllerApi();
          // const companyAssociatedData =
          //   await LksgDataControllerApi.getCompanyAssociatedLksgData(
          //   assertDefined(this.dataID)
          // );
          // this.dataSet = companyAssociatedData.data.data;
          this.waitingForData = false;
        }
      } catch (error) {
        console.error(error);
      }
    },
    getSectionHeading(type: string): string {
      const mapping: { [key: string]: string } = {
        CreditInstitution: "Credit Institution",
        AssetManagement: "Asset Management",
        InsuranceOrReinsurance: "Insurance and Reinsurance",
        InvestmentFirm: "Investment Firm",
      };
      return mapping[type];
    },
  },
});
</script>

