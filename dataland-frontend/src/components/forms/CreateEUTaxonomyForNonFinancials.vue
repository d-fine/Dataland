<template>
  <Card class="col-12">
    <template #title>Create EU Taxonomy Dataset for a Non-Financial Company/Service</template>
    <template #content>
      <FormKit
        v-model="formInputsModel"
        :actions="false"
        type="form"
        id="createEuTaxonomyForNonFinancialsForm"
        @submit="postEuTaxonomyDataForNonFinancials"
        #default="{ state: { valid } }"
      >
        <FormKit
          type="text"
          name="companyId"
          label="Company ID"
          placeholder="Company ID"
          :inner-class="innerClass"
          :input-class="inputClass"
          :disabled="true"
          :model-value="companyID"
        />
        <FormKit type="group" name="data" label="data">
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
          <div title="capex">
            <h3>CapEx</h3>
            <FormKit type="group" name="capex" label="CapEx">
              <FormKit
                type="text"
                name="alignedPercentage"
                validation="number"
                label="Aligned %"
                :inner-class="innerClass"
                :input-class="inputClass"
              />
              <FormKit
                type="text"
                name="eligiblePercentage"
                validation="number"
                label="Eligible %"
                :inner-class="innerClass"
                :input-class="inputClass"
              />
              <FormKit
                type="text"
                name="totalAmount"
                validation="number"
                label="Total €"
                :inner-class="innerClass"
                :input-class="inputClass"
              />
            </FormKit>
          </div>
          <div title="opex">
            <h3>OpEx</h3>
            <FormKit type="group" name="opex" label="OpEx">
              <FormKit
                type="text"
                name="alignedPercentage"
                validation="number"
                label="Aligned %"
                :inner-class="innerClass"
                :input-class="inputClass"
              />
              <FormKit
                type="text"
                name="eligiblePercentage"
                validation="number"
                label="Eligible %"
                :inner-class="innerClass"
                :input-class="inputClass"
              />
              <FormKit
                type="text"
                name="totalAmount"
                validation="number"
                label="Total €"
                :inner-class="innerClass"
                :input-class="inputClass"
              />
            </FormKit>
          </div>
          <div title="revenue">
            <h3>Revenue</h3>
            <FormKit type="group" name="revenue" label="Revenue">
              <FormKit
                type="text"
                name="alignedPercentage"
                validation="number"
                label="Aligned %"
                :inner-class="innerClass"
                :input-class="inputClass"
              />
              <FormKit
                type="text"
                name="eligiblePercentage"
                validation="number"
                label="Eligible %"
                :inner-class="innerClass"
                :input-class="inputClass"
              />
              <FormKit
                type="text"
                name="totalAmount"
                validation="number"
                label="Total €"
                :inner-class="innerClass"
                :input-class="inputClass"
              />
            </FormKit>
          </div>
          <FormKit type="submit" :disabled="!valid" label="Post EU-Taxonomy Dataset" name="postEUData" />
        </FormKit>
      </FormKit>
      <template v-if="postEuTaxonomyDataForNonFinancialsProcessed">
        <SuccessUpload
          v-if="postEuTaxonomyDataForNonFinancialsResponse"
          msg="EU Taxonomy Data"
          :data="postEuTaxonomyDataForNonFinancialsResponse.data"
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
import Card from "primevue/card";
import { ApiClientProvider } from "@/services/ApiClients";

export default {
  name: "CreateEUTaxonomyForNonFinancials",
  components: { FailedUpload, Card, FormKit, SuccessUpload },

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
    postEuTaxonomyDataForNonFinancialsProcessed: false,
    messageCount: 0,
    formInputsModel: {},
    postEuTaxonomyDataForNonFinancialsResponse: null,
  }),
  inject: ["getKeycloakPromise"],
  props: {
    companyID: {
      type: String,
    },
  },
  methods: {
    async postEuTaxonomyDataForNonFinancials() {
      try {
        this.postEuTaxonomyDataForNonFinancialsProcessed = false;
        this.messageCount++;
        const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
          this.getKeycloakPromise()
        ).getEuTaxonomyDataForNonFinancialsControllerApi();
        this.postEuTaxonomyDataForNonFinancialsResponse =
          await euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedData(this.formInputsModel);
        this.$formkit.reset("createEuTaxonomyForNonFinancialsForm");
      } catch (error) {
        this.postEuTaxonomyDataForNonFinancialsResponse = null;
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForNonFinancialsProcessed = true;
      }
    },
  },
};
</script>
