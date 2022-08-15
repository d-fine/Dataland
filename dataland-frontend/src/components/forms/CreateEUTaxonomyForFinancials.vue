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
          :inner-class="innerClass"
          :input-class="inputClass"
          :model-value="companyID"
          disabled="true"
        />
        <FormKit type="group" name="data" label="data">
          <FormKit
            type="select"
            name="financialServicesType"
            validation="required"
            label="Financial Services Type"
            placeholder="Please choose"
            :inner-class="innerClass"
            :input-class="inputClass"
            :options="{
              CreditInstitution: 'Credit Institution',
              InsuranceOrReinsurance: 'Insurance or Reinsurance',
              AssetManagement: 'Asset Management',
            }"
          />
          <FormKit
            type="select"
            name="attestation"
            validation="required"
            label="Attestation"
            placeholder="Please choose"
            :inner-class="innerClass"
            :input-class="inputClass"
            :options="{
              None: 'None',
              LimitedAssurance: 'Limited Assurance',
              ReasonableAssurance: 'Reasonable Assurance',
            }"
          />
          <FormKit
            type="radio"
            name="reportingObligation"
            validation="required"
            label="Reporting Obligation"
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
            :options="['Yes', 'No']"
          />
          <FormKit type="group" name="eligibilityKpis" label="Eligibility KPIs">
            <h4>Eligibility KPIs</h4>
            <FormKit
              type="text"
              name="taxonomyEligibleActivity"
              validation="number"
              label="Taxonomy Eligible Activity"
              :inner-class="innerClass"
              :input-class="inputClass"
            />
            <FormKit
              type="text"
              name="derivatives"
              validation="number"
              label="Derivatives"
              :inner-class="innerClass"
              :input-class="inputClass"
            />
            <FormKit
              type="text"
              name="banksAndIssuers"
              validation="number"
              label="Banks and Issuers"
              :inner-class="innerClass"
              :input-class="inputClass"
            />
            <FormKit
              type="text"
              name="investmentNonNfrd"
              validation="number"
              label="Investment non Nfrd"
              :inner-class="innerClass"
              :input-class="inputClass"
            />
          </FormKit>
          <FormKit type="group" name="creditInstitutionKpis" label="Credit Institution KPIs">
            <h4>Credit Institution KPIs</h4>
            <FormKit
              type="text"
              name="tradingPortfolio"
              validation="number"
              label="Trading Portfolio"
              :inner-class="innerClass"
              :input-class="inputClass"
            />
            <FormKit
              type="text"
              name="interbankLoans"
              validation="number"
              label="Interbank Loans"
              :inner-class="innerClass"
              :input-class="inputClass"
            />
            <FormKit
              type="text"
              name="tradingPortfolioAndInterbankLoans"
              validation="number"
              label="Trading Portfolio and Interbank Loans (combined)"
              :inner-class="innerClass"
              :input-class="inputClass"
            />
          </FormKit>
          <FormKit type="group" name="insuranceKpis" label)="Insurance KPIs">
            <h4>Insurance KPIs</h4>
            <FormKit
              type="text"
              name="taxonomyEligibleNonLifeInsuranceActivities"
              validation="number"
              label="Taxonomy Eligible non Life Insurance Activities"
              :inner-class="innerClass"
              :input-class="inputClass"
            />
          </FormKit>
          <FormKit type="submit" :disabled="!valid" label="Post EU-Taxonomy Dataset" name="postEUData" />
        </FormKit>
      </FormKit>
      <template v-if="postEuTaxonomyDataForFinancialsProcessed">
        <SuccessUpload
          v-if="postEuTaxonomyDataForFinancialsResponse"
          msg="EU Taxonomy Data"
          :messageCount="messageCount"
          :data="postEuTaxonomyDataForFinancialsResponse.data"
        />
        <FailedUpload v-else msg="EU Taxonomy Data" :messageCount="messageCount" />
      </template>
    </template>
  </Card>
</template>
<script>
import SuccessUpload from "@/components/messages/SuccessUpload";
import { FormKit } from "@formkit/vue";
import FailedUpload from "@/components/messages/FailedUpload";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";

export default {
  name: "CreateEUTaxonomyForFinancials",
  components: { FailedUpload, FormKit, SuccessUpload, Card },

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
  }),
  props: {
    companyID: {
      type: String,
    },
  },
  inject: ["getKeycloakPromise"],
  methods: {
    async postEuTaxonomyDataForFinancials() {
      try {
        this.postEuTaxonomyDataForFinancialsProcessed = false;
        this.messageCount++;
        const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
          this.getKeycloakPromise()
        ).getEuTaxonomyDataForFinancialsControllerApi();
        this.postEuTaxonomyDataForFinancialsResponse =
          await euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedData1(this.formInputsModel);
        this.$formkit.reset("createEuTaxonomyForFinancialsForm");
      } catch (error) {
        this.postEuTaxonomyDataForFinancialsResponse = null;
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForFinancialsProcessed = true;
      }
    },
  },
};
</script>
