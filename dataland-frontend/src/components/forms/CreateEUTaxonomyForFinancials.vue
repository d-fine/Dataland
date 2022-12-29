<template>
  <Card class="col-12">
    <template #title>Create EU Taxonomy Dataset for a Financial Company/Service</template>
    <template #content>
      <FormKit
        v-model="formInputsModel"
        :actions="false"
        type="form"
        id="createEuTaxonomyForFinancialsForm"
        @submit="postEuTaxonomyDataForFinancials"
        #default="{ state: { valid } }"
      >
        <h4>General Information</h4>
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
  name: "CreateEUTaxonomyForFinancials",
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
