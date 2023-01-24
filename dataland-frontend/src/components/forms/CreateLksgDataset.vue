<template>
  <Card class="col-12">
    <template #title
      >New Dataset - LkSG
      <hr />
    </template>
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            id="createLkSGForm"
            @submit="postEuTaxonomyDataForFinancials"
            #default="{ state: { valid } }"
          >
            <div class="uploadFormSection grid">
              <div id="topicLabel" class="col-3 topicLabel">
                <h4 id="general" class="anchor title">General</h4>
                <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                <p>Please input all relevant basic information about the dataset</p>
              </div>

              <div id="formFields" class="col-9 formFields">
                <FormKit type="group" name="general" label="general">
                  <div class="form-field-label">
                    <h5>Date</h5>
                    <em
                      class="material-icons info-icon"
                      aria-hidden="true"
                      title="Date"
                      v-tooltip.top="{
                        value: lksgQuestions['dataDate'],
                      }"
                      >info</em
                    >
                  </div>
                  <FormKit
                    type="date"
                    value="03-03-2018"
                    help="Enter date"
                    validation="required"
                    validation-visibility="live"
                  />
                  <div class="form-field-label">
                    <h5>LKSG in Scope</h5>
                    <em
                      class="material-icons info-icon"
                      aria-hidden="true"
                      title="lksgInScope"
                      v-tooltip.top="{
                        value: lksgQuestions['lksgInScope'],
                      }"
                      >info</em
                    >
                  </div>
                  <FormKit
                    type="radio"
                    name="lksgInScope"
                    :options="['Yes', 'No']"
                    :outer-class="{
                      'yes-no-radio': true,
                    }"
                    :inner-class="{
                      'formkit-inner': false,
                    }"
                    :input-class="{
                      'formkit-input': false,
                      'p-radiobutton': true,
                    }"
                  />
                  <div class="form-field-label">
                    <h5>Company Legal Form</h5>
                    <em
                      class="material-icons info-icon"
                      aria-hidden="true"
                      title="companyLegalForm"
                      v-tooltip.top="{
                        value: lksgQuestions['companyLegalForm'],
                      }"
                      >info</em
                    >
                  </div>
                  <FormKit type="text" name="companyLegalForm" validation="required" />
                  <div class="form-field-label">
                    <h5>VAT Identification Number</h5>
                    <em
                      class="material-icons info-icon"
                      aria-hidden="true"
                      title="VATidentificationNumber"
                      v-tooltip.top="{
                        value: lksgQuestions['VATidentificationNumber'],
                      }"
                      >info</em
                    >
                  </div>
                  <FormKit type="number" name="VATidentificationNumber" validation="required|number" step="1" />
                  <div class="form-field-label">
                    <h5>Number Of Employees</h5>
                    <em
                      class="material-icons info-icon"
                      aria-hidden="true"
                      title="numberOfEmployees"
                      v-tooltip.top="{
                        value: lksgQuestions['numberOfEmployees'],
                      }"
                      >info</em
                    >
                  </div>
                  <FormKit
                    type="number"
                    name="numberOfEmployees"
                    placeholder="Value"
                    validation="required|number|between:0,100"
                    step="1"
                    :inner-class="{
                      short: true,
                    }"
                  />
                  <div class="form-field-label">
                    <h5>Share Of Temporary Workers</h5>
                    <em
                      class="material-icons info-icon"
                      aria-hidden="true"
                      title="shareOfTemporaryWorkers"
                      v-tooltip.top="{
                        value: lksgQuestions['shareOfTemporaryWorkers'],
                      }"
                      >info</em
                    >
                  </div>
                  <FormKit
                    type="number"
                    name="shareOfTemporaryWorkers"
                    placeholder="Value %"
                    validation="required|number|between:0,100"
                    step="1"
                    :inner-class="{
                      short: true,
                    }"
                  />
                  <div class="form-field-label">
                    <h5>Total Revenue</h5>
                    <em
                      class="material-icons info-icon"
                      aria-hidden="true"
                      title="totalRevenue"
                      v-tooltip.top="{
                        value: lksgQuestions['totalRevenue'],
                      }"
                      >info</em
                    >
                  </div>
                  <div class="next-to-each-other">
                    <FormKit
                      type="number"
                      name="totalRevenue"
                      placeholder="Value"
                      validation="required|number"
                      step="1"
                    />
                    <FormKit type="select" name="unit" placeholder="Unit" :options="['CHF', 'USD']" />
                  </div>
                  <div class="form-field-label">
                    <h5>Total Revenue Currency</h5>
                    <em
                      class="material-icons info-icon"
                      aria-hidden="true"
                      title="totalRevenueCurrency"
                      v-tooltip.top="{
                        value: lksgQuestions['totalRevenueCurrency'],
                      }"
                      >info</em
                    >
                  </div>
                  <FormKit
                    type="text"
                    name="totalRevenueCurrency"
                    placeholder="Currency"
                    validation="required"
                    :inner-class="{
                      medium: true,
                    }"
                  />
                  <div class="form-field-label">
                    <h5>List Of Goods Or Services</h5>
                    <em
                      class="material-icons info-icon"
                      aria-hidden="true"
                      title="listOfGoodsOrServices"
                      v-tooltip.top="{
                        value: lksgQuestions['listOfGoodsOrServices'],
                      }"
                      >info</em
                    >
                    <PrimeButton label="Add" class="p-button-text" icon="pi pi-plus"></PrimeButton>
                  </div>
                  <FormKit
                    type="text"
                    name="listOfGoodsOrServices"
                    placeholder="Add comma (,) for more than one value"
                    validation="required"
                  />
                </FormKit>
                <div class="">
                  <span
                    class="form-list-item"
                    :key="item"
                    v-for="item in formInputsModel.social.general.listOfProductionSites[0].listOfGoodsOrServices"
                  >
                    {{ item }} <em @click="() => removeItemFromList(item)" class="material-icons">close</em>
                  </span>
                </div>
              </div>
            </div>
            ddddddddddddddd
            <div class="uploadFormSection grid">
              <div id="topicLabel" class="col-3 topicLabel">
                <h4 id="osh" class="anchor title">General</h4>
                <div class="p-badge badge-yellow"><span>OSH</span></div>
                <p>Please input all relevant basic information about the dataset</p>
              </div>

              <div id="formFields" class="col-9 formFields">
                <FormKit
                  type="date"
                  value="2011-01-01"
                  label="Birthday"
                  help="Enter your birth day"
                  validation="required|date_before:2010-01-01"
                  validation-visibility="live"
                />
                <FormKit
                  type="text"
                  name="companyId"
                  label="Company ID"
                  placeholder="Company ID"
                  :model-value="companyID"
                  disabled="true"
                />
                <FormKit type="group" name="data" label="data">
                  <FormKit
                    type="select"
                    name="financialServicesTypes"
                    multiple
                    validation="required"
                    label="Financial Services Types"
                    placeholder="Please choose"
                    :options="{
                      CreditInstitution: humanizeString('CreditInstitution'),
                      InsuranceOrReinsurance: humanizeString('InsuranceOrReinsurance'),
                      AssetManagement: humanizeString('AssetManagement'),
                      InvestmentFirm: humanizeString('InvestmentFirm'),
                    }"
                    help="Select all that apply by holding command (macOS) or control (PC)."
                  />
                  <FormKit type="group" name="assurance" label="Assurance">
                    <FormKit
                      type="select"
                      name="assurance"
                      label="Assurance"
                      placeholder="Please choose"
                      :options="{
                        None: humanizeString('None'),
                        LimitedAssurance: humanizeString('LimitedAssurance'),
                        ReasonableAssurance: humanizeString('ReasonableAssurance'),
                      }"
                    />
                  </FormKit>
                  <FormKit type="group" name="eligibilityKpis" label="Eligibility KPIs">
                    <template
                      v-for="fsType in [
                        'CreditInstitution',
                        'InsuranceOrReinsurance',
                        'AssetManagement',
                        'InvestmentFirm',
                      ]"
                      :key="fsType"
                    >
                      <div :name="fsType">
                        <FormKit type="group" :name="fsType">
                          <h4>Eligibility KPIs ({{ humanizeString(fsType) }})</h4>
                          <DataPointFormElement name="taxonomyEligibleActivity" label="Taxonomy Eligible Activity" />
                          <DataPointFormElement
                            name="taxonomyNonEligibleActivity"
                            label="Taxonomy Non Eligible Activity"
                          />
                          <DataPointFormElement name="derivatives" label="Derivatives" />
                          <DataPointFormElement name="banksAndIssuers" label="Banks and Issuers" />
                          <DataPointFormElement name="investmentNonNfrd" label="Investment non Nfrd" />
                        </FormKit>
                      </div>
                    </template>
                  </FormKit>
                </FormKit>
                <FormKit type="submit" :disabled="!valid" label="Post EU-Taxonomy Dataset" name="postEUData" />
              </div>
            </div>
          </FormKit>
        </div>

        <div id="jumpLinks" class="col-3 text-left jumpLinks">
          <h4 id="topicTitles" class="title">On this page</h4>

          <ul>
            <li><a href="#general">General</a></li>
            <li><a href="#childLabour">Child labour</a></li>
            <li><a href="#forcedLabourSlaveryAndDebtBondage">Forced labour, slavery and debt bondage</a></li>
            <li><a href="#evidenceCertificatesAndAttestations">Evidence, certificates and attestations</a></li>
            <li><a href="#socialAndEmployeeMatters">Social and employee matters</a></li>
            <li><a href="#environment">Environment</a></li>
            <li><a href="#osh">OSH</a></li>
            <li><a href="#riskManagement">Risk management</a></li>
            <li><a href="#grievanceMechanism">Grievance mechanism</a></li>
            <li><a href="#codeOfConduct">Code of Conduct</a></li>
            <li><a href="#grievanceMechanism">Grievance mechanism</a></li>
            <li><a href="#freedomOfAssociation">Freedom of association</a></li>
            <li><a href="#humanRights">Human rights</a></li>
            <li><a href="#waste">Waste</a></li>
          </ul>
        </div>
      </div>

      <template v-if="postEuTaxonomyDataForFinancialsProcessed">
        <SuccessUpload
          v-if="postEuTaxonomyDataForFinancialsResponse"
          msg="EU Taxonomy Data"
          :data="postEuTaxonomyDataForFinancialsResponse.data"
          :messageCount="messageCount"
        />
        <FailedUpload v-else msg="EU Taxonomy Data" :messageCount="messageCount" />
      </template>
    </template>
  </Card>
</template>

<script lang="ts">
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import { FormKit } from "@formkit/vue";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import { humanizeString } from "@/utils/StringHumanizer";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import DataPointFormElement from "@/components/forms/DataPointFormElement.vue";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Tooltip from "primevue/tooltip";
import PrimeButton from "primevue/button";
import { lksgQuestions } from "@/components/resources/frameworkDataSearch/DataModelsTranslations";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateLksgDataset",
  components: { DataPointFormElement, FailedUpload, FormKit, SuccessUpload, Card, PrimeButton },
  directives: {
    tooltip: Tooltip,
  },

  data: () => ({
    postEuTaxonomyDataForFinancialsProcessed: false,
    messageCount: 0,
    formInputsModel: {
      social: {
        general: {
          dataDate: "2023-01-24",
          lksgInScope: "Yes",
          vatIdentificationNumber: "string",
          numberOfEmployees: 0,
          shareOfTemporaryWorkers: 0,
          totalRevenue: 0,
          totalRevenueCurrency: "string",
          listOfProductionSites: [
            {
              name: "string",
              isInHouseProductionOrIsContractProcessing: "Yes",
              address: "string",
              listOfGoodsOrServices: ["first", "second"],
            },
          ],
        },
        grievanceMechanism: {
          grievanceHandlingMechanism: "Yes",
          grievanceHandlingMechanismUsedForReporting: "Yes",
          legalProceedings: "Yes",
        },
        childLabour: {
          employeeUnder18: "Yes",
          employeeUnder15: "Yes",
          employeeUnder18Apprentices: "Yes",
          employmentUnderLocalMinimumAgePrevention: "Yes",
          employmentUnderLocalMinimumAgePreventionEmploymentContracts: "Yes",
          employmentUnderLocalMinimumAgePreventionJobDescription: "Yes",
          employmentUnderLocalMinimumAgePreventionIdentityDocuments: "Yes",
          employmentUnderLocalMinimumAgePreventionTraining: "Yes",
          employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: "Yes",
        },
        forcedLabourSlaveryAndDebtBondage: {
          forcedLabourAndSlaveryPrevention: "Yes",
          forcedLabourAndSlaveryPreventionEmploymentContracts: "Yes",
          forcedLabourAndSlaveryPreventionIdentityDocuments: "Yes",
          forcedLabourAndSlaveryPreventionFreeMovement: "Yes",
          forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets: "Yes",
          forcedLabourAndSlaveryPreventionTraining: "Yes",
          documentedWorkingHoursAndWages: "Yes",
          adequateLivingWage: "Yes",
          regularWagesProcessFlow: "Yes",
          fixedHourlyWages: "Yes",
        },
        osh: {
          oshMonitoring: "Yes",
          oshPolicy: "Yes",
          oshPolicyPersonalProtectiveEquipment: "Yes",
          oshPolicyMachineSafety: "Yes",
          oshPolicyDisasterBehaviouralResponse: "Yes",
          oshPolicyAccidentsBehaviouralResponse: "Yes",
          oshPolicyWorkplaceErgonomics: "Yes",
          oshPolicyHandlingChemicalsAndOtherHazardousSubstances: "Yes",
          oshPolicyFireProtection: "Yes",
          oshPolicyWorkingHours: "Yes",
          oshPolicyTrainingAddressed: "Yes",
          oshPolicyTraining: "Yes",
          oshManagementSystem: "Yes",
          oshManagementSystemInternationalCertification: "Yes",
          oshManagementSystemNationalCertification: "Yes",
          workplaceAccidentsUnder10: "Yes",
          oshTraining: "Yes",
        },
        freedomOfAssociation: {
          freedomOfAssociation: "Yes",
          discriminationForTradeUnionMembers: "Yes",
          freedomOfOperationForTradeUnion: "Yes",
          freedomOfAssociationTraining: "Yes",
          worksCouncil: "Yes",
        },
        humanRights: {
          diversityAndInclusionRole: "Yes",
          preventionOfMistreatments: "Yes",
          equalOpportunitiesOfficer: "Yes",
          riskOfHarmfulPollution: "Yes",
          unlawfulEvictionAndTakingOfLand: "Yes",
          useOfPrivatePublicSecurityForces: "Yes",
          useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: "Yes",
        },
        evidenceCertificatesAndAttestations: {
          iso26000: "Yes",
          sa8000Certification: "Yes",
          smetaSocialAuditConcept: "Yes",
          betterWorkProgramCertificate: "Yes",
          iso45001Certification: "Yes",
          iso14000Certification: "Yes",
          emasCertification: "Yes",
          iso37001Certification: "Yes",
          iso37301Certification: "Yes",
          riskManagementSystemCertification: "Yes",
          amforiBsciAuditReport: "Yes",
          initiativeClauseSocialCertification: "Yes",
          responsibleBusinessAssociationCertification: "Yes",
          fairLabourAssociationCertification: "Yes",
          fairWorkingConditionsPolicy: "Yes",
          fairAndEthicalRecruitmentPolicy: "Yes",
          equalOpportunitiesAndNondiscriminationPolicy: "Yes",
          healthAndSafetyPolicy: "Yes",
          complaintsAndGrievancesPolicy: "Yes",
          forcedLabourPolicy: "Yes",
          childLabourPolicy: "Yes",
          environmentalImpactPolicy: "Yes",
          supplierCodeOfConduct: "Yes",
        },
      },
      governance: {
        socialAndEmployeeMatters: {
          responsibilitiesForFairWorkingConditions: "Yes",
        },
        environment: {
          responsibilitiesForTheEnvironment: "Yes",
        },
        osh: {
          responsibilitiesForOccupationalSafety: "Yes",
        },
        riskManagement: {
          riskManagementSystem: "Yes",
        },
        codeOfConduct: {
          codeOfConduct: "Yes",
          codeOfConductRiskManagementTopics: "Yes",
          codeOfConductTraining: "Yes",
        },
      },
      environmental: {
        waste: {
          mercuryAndMercuryWasteHandling: "Yes",
          mercuryAndMercuryWasteHandlingPolicy: "Yes",
          chemicalHandling: "Yes",
          environmentalManagementSystem: "Yes",
          environmentalManagementSystemInternationalCertification: "Yes",
          environmentalManagementSystemNationalCertification: "Yes",
          legalRestrictedWaste: "Yes",
          legalRestrictedWasteProcesses: "Yes",
          mercuryAddedProductsHandling: "Yes",
          mercuryAddedProductsHandlingRiskOfExposure: "Yes",
          mercuryAddedProductsHandlingRiskOfDisposal: "Yes",
          mercuryAndMercuryCompoundsProductionAndUse: "Yes",
          mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: "Yes",
          persistentOrganicPollutantsProductionAndUse: "Yes",
          persistentOrganicPollutantsProductionAndUseRiskOfExposure: "Yes",
          persistentOrganicPollutantsProductionAndUseRiskOfDisposal: "Yes",
          persistentOrganicPollutantsProductionAndUseTransboundaryMovements: "Yes",
          persistentOrganicPollutantsProductionAndUseRiskForImportingState: "Yes",
          hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein: "Yes",
          hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein: "Yes",
          hazardousWasteDisposal: "Yes",
          hazardousWasteDisposalRiskOfImport: "Yes",
          hazardousAndOtherWasteImport: "Yes",
        },
      },
    },
    postEuTaxonomyDataForFinancialsResponse: null,
    humanizeString: humanizeString,
    lksgQuestions,
  }),
  props: {
    companyID: {
      type: String,
    },
  },
  methods: {
    async postEuTaxonomyDataForFinancials(): Promise<void> {
      try {
        this.postEuTaxonomyDataForFinancialsProcessed = false;
        this.messageCount++;
        const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForFinancialsControllerApi();
        this.postEuTaxonomyDataForFinancialsResponse =
          await euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(
            this.formInputsModel
          );
        this.$formkit.reset("createEuTaxonomyForFinancialsForm");
      } catch (error) {
        this.postEuTaxonomyDataForFinancialsResponse = null;
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForFinancialsProcessed = true;
      }
    },
    removeItemFromList(item: string) {
      this.formInputsModel.social.general.listOfProductionSites[0].listOfGoodsOrServices =
        this.formInputsModel.social.general.listOfProductionSites[0].listOfGoodsOrServices.filter((el) => el !== item);
    },
  },
});
</script>
<style scoped lang="scss">
.anchor {
  scroll-margin-top: 300px;
}
.formkit-icon {
  max-width: 5em;
}
</style>
