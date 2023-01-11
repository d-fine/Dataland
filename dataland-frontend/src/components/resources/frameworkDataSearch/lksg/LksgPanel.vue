<template>
  <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
    <p class="font-medium text-xl">Loading Lksg Data...</p>
    <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
  </div>
  <div v-if="dataSet && !waitingForData">
    <DetailCompanyDataTable
        :dataSet="kpisDataObjects"
        :kpisNames="LksgKpis"
        :dataSetColumns="dataSetColumns"
        :hintsForKpis="LksgQuestions"
    />
  </div>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { LksgData } from "@clients/backend";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import DetailCompanyDataTable from "@/components/general/DetailCompanyDataTable.vue";
import {
  LksgKpisImpactArea,
  LksgKpis,
  LksgQuestions,
} from "@/components/resources/frameworkDataSearch/lksg/LksgModels";

export default defineComponent({
  name: "LksgPanel",
  components: { DetailCompanyDataTable },
  data() {
    return {
      waitingForData: true,
      dataSet: [] as LksgData | undefined,
      newDataSet: {},
      dataSetColumns: [] as string[],
      kpisDataObjects: [],
      LksgKpisImpactArea,
      LksgKpis,
      LksgQuestions,
    };
  },
  props: {
    dataID: {
      type: String,
      default: "",
    },
  },
  mounted() {
    void this.getCompanyLksgDataset();
  },
  watch: {
    dataID() {
      void this.getCompanyLksgDataset();
    },
    dataSet() {
      void this.generateColsNames();
    },
    newDataSet() {
      void this.generateConvertedData();
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
          const companyAssociatedData = await LksgDataControllerApi.getCompanyAssociatedLksgData(
            assertDefined(this.dataID)
          );
          this.dataSet = companyAssociatedData.data.data;
          this.waitingForData = false;
        }
      } catch (error) {
        console.error(error);
      }
    },

    generateColsNames(): void {
      const dateRegex = "^\\d{4}-\\d{2}-\\d{2}$";

      if (this.dataSet && Array.isArray(this.dataSet)) {
        this.dataSet.forEach((oneDataObject: LksgData) => {
          if (oneDataObject.dataDate && oneDataObject.dataDate.match(dateRegex)) {
            this.dataSetColumns.push(oneDataObject.dataDate);
            this.newDataSet = { ...this.newDataSet, [oneDataObject.dataDate]: oneDataObject };
          }
        });
      } else if (this.dataSet?.dataDate && this.dataSet.dataDate.match(dateRegex)) {
        this.dataSetColumns.push(this.dataSet.dataDate);
        this.newDataSet = { ...this.newDataSet, [this.dataSet.dataDate]: this.dataSet };
      }
    },

    generateConvertedData(): void {
      console.log('kpisthis.dataSet', this.dataSet)
      if (this.dataSet && Array.isArray(this.dataSet)) {
        this.dataSet.forEach((el) => {
          for (const [key] of Object.entries(el)) {
            if (!this.kpisDataObjects.some((e) => e.kpi === key)) {
              let kpiDataObject = {
                kpi: `${key}`,
                group: this.LksgKpisImpactArea[key],
              };
              this.dataSetColumns.forEach((dataDate) => {
                kpiDataObject = { ...kpiDataObject, [dataDate]: this.newDataSet[dataDate][key] };
              });
              this.kpisDataObjects.push(kpiDataObject);
            } else {
              return;
            }
          }
        });
      } else if (this.dataSet) {
        for (const [key] of Object.entries(this.dataSet)) {
          if (!this.kpisDataObjects.some((e) => e.kpi === key)) {
            let kpiDataObject = {
              kpi: `${key}`,
              group: this.LksgKpisImpactArea[key],
            };

            this.dataSetColumns.forEach((dataDate) => {

              kpiDataObject = { ...kpiDataObject, [dataDate]: this.newDataSet[dataDate][key] };

            });
            this.kpisDataObjects.push(kpiDataObject);
          } else {
            return;
          }
        }

      }
    },
  },
});
</script>

<!--{-->
<!--dataDate: "2021-00-00",-->
<!--companyLegalForm: "Trader Trader",-->
<!--vatIdentificationNumber: "BJ564339879",-->
<!--numberOfEmployees: 189085,-->
<!--shareOfTemporaryWorkers: 4.14,-->
<!--totalRevenue: 23691057301.85,-->
<!--totalRevenueCurrency: "CHF",-->
<!--betterWorkProgramCertificate: "No",-->
<!--responsibilitiesForFairWorkingConditions: "No",-->
<!--responsibilitiesForOccupationalSafety: "Yes",-->
<!--riskManagementSystem: "No",-->
<!--grievanceHandlingMechanismUsedForReporting: "No",-->
<!--codeOfConduct: "No",-->
<!--codeOfConductTraining: "Yes",-->
<!--legalProceedings: "Yes",-->
<!--employeeUnder18Under15: "No",-->
<!--employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: "No",-->
<!--forcedLabourAndSlaveryPrevention: "No",-->
<!--forcedLabourAndSlaveryPreventionIdentityDocuments: "Yes",-->
<!--forcedLabourAndSlaveryPreventionFreeMovement: "No",-->
<!--forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets: "Yes",-->
<!--forcedLabourAndSlaveryPreventionProvisionTraining: "No",-->
<!--adequateLivingWage: "No",-->
<!--regularWagesProcessFlow: "No",-->
<!--fixedHourlyWages: "Yes",-->
<!--workplaceAccidentsUnder10: "No",-->
<!--freedomOfAssociation: "Yes",-->
<!--discriminationForTradeUnionMembers: "No",-->
<!--freedomOfOperationForTradeUnion: "Yes",-->
<!--worksCouncil: "No",-->
<!--diversityAndInclusionRole: "Yes",-->
<!--equalOpportunitiesOfficer: "Yes",-->
<!--riskOfHarmfulPollution: "Yes",-->
<!--unlawfulEvictionAndTakingOfLand: "Yes",-->
<!--useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: "No",-->
<!--mercuryAndMercuryWasteHandlingPolicy: "Yes",-->
<!--chemicalHandling: "Yes",-->
<!--environmentalManagementSystem: "No",-->
<!--legalRestrictedWaste: "No",-->
<!--mercuryAddedProductsHandling: "Yes",-->
<!--mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: "Yes",-->
<!--persistentOrganicPollutantsProductionAndUse: "Yes",-->
<!--persistentOrganicPollutantsProductionAndUseRiskOfDisposal: "Yes",-->
<!--hazardousWasteDisposal: "No",-->
<!--hazardousAndOtherWasteImport: "Yes",-->
<!--riskManagementSystemCertification: "Yes",-->
<!--amforiBsciAuditReport: "Yes",-->
<!--fairLabourAssociationCertification: "No",-->
<!--lksgInScope: "No",-->
<!--oshMonitoring: "No",-->
<!--oshPolicy: "Yes",-->
<!--smetaSocialAuditConcept: "Yes",-->
<!--iso45001Certification: "No",-->
<!--iso14000Certification: "No",-->
<!--sa8000Certification: "Yes",-->
<!--iso37001Certification: "Yes",-->
<!--iso37301Certification: "Yes",-->
<!--oshPolicyFireProtection: "Yes",-->
<!--oshPolicyWorkingHours: "Yes",-->
<!--oshManagementSystem: "No",-->
<!--oshPolicyWorkplaceErgonomics: "No",-->
<!--oshPolicyTraining: "Yes",-->
<!--oshPolicyPersonalProtectiveEquipment: "No",-->
<!--oshPolicyAccidentsBehaviouralResponse: "Yes",-->
<!--oshPolicyDisasterBehaviouralResponse: "Yes",-->
<!--oshPolicyHandlingChemicalsAndOtherHazardousSubstances: "Yes",-->
<!--equalOpportunitiesAndNondiscriminationPolicy: "No",-->
<!--healthAndSafetyPolicy: "Yes",-->
<!--complaintsAndGrievancesPolicy: "No",-->
<!--// "listOfProductionSites": [-->
<!--//   {-->
<!--//     "name": "Merseburg Gruppe",-->
<!--//     "isInHouseProductionOrIsContractProcessing": "No",-->
<!--//     "address": "Köttershof 0, 76326 Nord Joschua, Libyen",-->
<!--//     "listOfGoodsAndServices": [-->
<!--//       "Elegant Granite Chips",-->
<!--//       "Tasty Bronze Mouse"-->
<!--//     ]-->
<!--//   }-->
<!--// ]-->
<!--},-->
