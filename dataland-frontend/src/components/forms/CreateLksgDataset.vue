<template>
  <Card class="col-12">
    <template #title
      >New Dataset - LkSG
      <hr />
    </template>
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="topicLabel" class="col-3 text-left topicLabel">
          <h4 id="topicTitle" class="title">General</h4>
          <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
          <p>Please input all relevant basic information about the dataset</p>
        </div>
        <div id="uploadForm" class="col-6 text-left uploadForm">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            id="createEuTaxonomyForFinancialsForm"
            @submit="postEuTaxonomyDataForFinancials"
            #default="{ state: { valid } }"
          >
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
              <FormKit
                type="radio"
                name="reportingObligation"
                label="Reporting Obligation"
                :options="['Yes', 'No']"
                :outer-class="{
                  'formkit-outer': false,
                }"
                :inner-class="{
                  'formkit-inner': false,
                }"
                :input-class="{
                  'formkit-input': false,
                  'p-radiobutton:': true,
                }"
              />
              <FormKit type="group" name="eligibilityKpis" label="Eligibility KPIs">
                <template
                  v-for="fsType in ['CreditInstitution', 'InsuranceOrReinsurance', 'AssetManagement', 'InvestmentFirm']"
                  :key="fsType"
                >
                  <div :name="fsType">
                    <FormKit type="group" :name="fsType">
                      <h4>Eligibility KPIs ({{ humanizeString(fsType) }})</h4>
                      <DataPointFormElement name="taxonomyEligibleActivity" label="Taxonomy Eligible Activity" />
                      <DataPointFormElement name="taxonomyNonEligibleActivity" label="Taxonomy Non Eligible Activity" />
                      <DataPointFormElement name="derivatives" label="Derivatives" />
                      <DataPointFormElement name="banksAndIssuers" label="Banks and Issuers" />
                      <DataPointFormElement name="investmentNonNfrd" label="Investment non Nfrd" />
                    </FormKit>
                  </div>
                </template>
              </FormKit>
              <FormKit type="group" name="creditInstitutionKpis" label="Credit Institution KPIs">
                <h4>Credit Institution KPIs</h4>
                <div name="creditInstitutionKpis">
                  <DataPointFormElement name="tradingPortfolio" label="Trading Portfolio" />
                  <DataPointFormElement name="interbankLoans" label="Interbank Loans" />
                  <DataPointFormElement
                    name="tradingPortfolioAndInterbankLoans"
                    label="Trading Portfolio and Interbank Loans (combined)"
                  />
                  <DataPointFormElement name="greenAssetRatio" label="Green asset ratio" />
                </div>
              </FormKit>
              <FormKit type="group" name="insuranceKpis" label="Insurance KPIs">
                <h4>Insurance KPIs</h4>
                <DataPointFormElement
                  name="taxonomyEligibleNonLifeInsuranceActivities"
                  label="Taxonomy Eligible non Life Insurance Activities"
                />
              </FormKit>
              <FormKit type="group" name="investmentFirmKpis" label="Investment Firm KPIs">
                <h4>Investment Firm KPIs</h4>
                <DataPointFormElement name="greenAssetRatio" label="Green asset ratio" />
              </FormKit>
              <FormKit type="submit" :disabled="!valid" label="Post EU-Taxonomy Dataset" name="postEUData" />
            </FormKit>
          </FormKit>
        </div>
        <div id="jumpLinks" class="col-3 text-left jumpLinks">
          <h4 id="topicTitle" class="title">On this page</h4>

          <ul>
            <li><a href="#general">General</a></li>
            <li><a href="#childLabour">Child labour</a></li>
            <li><a href="#forcedLabourSlaveryAndDebtBondage">Forced labour, slavery and debt bondage</a></li>
            <li><a href="#evidenceCertificatesAndAttestations">Evidence, certificates and attestations</a></li>
            <li><a href="#support">Theme Support</a></li>
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

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateLksgDataset",
  components: { DataPointFormElement, FailedUpload, FormKit, SuccessUpload, Card },

  data: () => ({
    innerClass: {
      "formkit-inner": false,
      "p-inputwrapper": true,
    },
    inputClass: {
      "formkit-input": false,
      "p-inputtext": true,
      "w-full": true,
    },
    postEuTaxonomyDataForFinancialsProcessed: false,
    messageCount: 0,
    formInputsModel: {},
    postEuTaxonomyDataForFinancialsResponse: null,
    humanizeString: humanizeString,
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
  },
});
</script>
