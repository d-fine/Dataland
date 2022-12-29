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
          :disabled="true"
          :model-value="companyID"
        />
        <FormKit type="group" name="data" label="data">
          <FormKit type="group" name="assurance" label="Assurance">
            <FormKit
              type="select"
              name="assurance"
              validation="required"
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
            validation="required"
            label="Reporting Obligation"
            :options="['Yes', 'No']"
          />
          <div title="capex">
            <h3>CapEx</h3>
            <FormKit type="group" name="capex" label="CapEx">
              <DataPointFormElement name="alignedPercentage" label="Aligned %" />
              <DataPointFormElement name="eligiblePercentage" label="Eligible %" />
              <DataPointFormElement name="totalAmount" label="Total Amount" />
            </FormKit>
          </div>
          <div title="opex">
            <h3>OpEx</h3>
            <FormKit type="group" name="opex" label="OpEx">
              <DataPointFormElement name="alignedPercentage" label="Aligned %" />
              <DataPointFormElement name="eligiblePercentage" label="Eligible %" />
              <DataPointFormElement name="totalAmount" label="Total Amount" />
            </FormKit>
          </div>
          <div title="revenue">
            <h3>Revenue</h3>
            <FormKit type="group" name="revenue" label="Revenue">
              <DataPointFormElement name="alignedPercentage" label="Aligned %" />
              <DataPointFormElement name="eligiblePercentage" label="Eligible %" />
              <DataPointFormElement name="totalAmount" label="Total Amount" />
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
import Card from "primevue/card";
import { ApiClientProvider } from "@/services/ApiClients";
import { humanizeString } from "@/utils/StringHumanizer";
import DataPointFormElement from "@/components/forms/DataPointFormElement.vue";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "CreateEUTaxonomyForNonFinancials",
  components: { DataPointFormElement, FailedUpload, Card, FormKit, SuccessUpload },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

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
    humanizeString: humanizeString,
  }),
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
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForNonFinancialsControllerApi();
        this.postEuTaxonomyDataForNonFinancialsResponse =
          await euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
            this.formInputsModel
          );
        this.$formkit.reset("createEuTaxonomyForNonFinancialsForm");
      } catch (error) {
        this.postEuTaxonomyDataForNonFinancialsResponse = null;
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForNonFinancialsProcessed = true;
      }
    },
  },
});
</script>
