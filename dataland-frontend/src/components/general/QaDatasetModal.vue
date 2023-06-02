<template>
  <div>
    <pre id="dataset-container">{{ datasetAsJson }}</pre>
  </div>
  <MiddleCenterDiv class="col-12">
    <div autofocus="autofocus" v-if="reviewSubmitted !== true">
      <PrimeButton @click="setQualityStatusToApproved" label="Accept Dataset" />
      <PrimeButton @click="setQualityStatusToRejected" label="Reject Dataset" />
    </div>
    <div v-if="reviewSubmitted">
      <SuccessMessage v-if="reviewSuccessful" success-message="Review successfully submitted." />
      <FailMessage v-else />
    </div>
  </MiddleCenterDiv>
</template>

<script lang="ts">
import { defineComponent, inject } from "vue";
import PrimeButton from "primevue/button";
import { DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";
import SuccessMessage from "@/components/messages/SuccessMessage.vue";
import FailMessage from "@/components/messages/FailMessage.vue";
import { TIME_DELAY_BETWEEN_SUBMIT_AND_RELOAD_IN_MS } from "@/utils/Constants";

export default defineComponent({
  components: { FailMessage, SuccessMessage, MiddleCenterDiv, PrimeButton },
  inject: ["dialogRef"],
  name: "QADatasetModal",
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
      dataSetToReview: null as unknown as object,
      dataId: "",
      reviewSubmitted: false,
      reviewSuccessful: false,
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      dataSetToReview: object;
      dataId: string;
    };
    this.dataSetToReview = dialogRefData.dataSetToReview;
    this.dataId = dialogRefData.dataId;
  },
  computed: {
    datasetAsJson(): string {
      return JSON.stringify(this.dataSetToReview, null, 2);
    },
  },

  methods: {
    /**
     * Sets dataset to accepted
     */
    async setQualityStatusToApproved() {
      try {
        this.reviewSubmitted = true;
        const qaServiceControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getQaControllerApi();
        await qaServiceControllerApi.assignQualityStatus(this.dataId, "Accepted");
        this.reviewSuccessful = true;
        setTimeout(() => {
          this.closeTheDialogAndReloadPage();
        }, TIME_DELAY_BETWEEN_SUBMIT_AND_RELOAD_IN_MS);
      } catch (error) {
        console.error(error);
      }
    },
    /**
     * Sets dataset to rejected
     */
    async setQualityStatusToRejected() {
      try {
        this.reviewSubmitted = true;
        const qaServiceControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getQaControllerApi();
        await qaServiceControllerApi.assignQualityStatus(this.dataId, "Rejected");
        this.reviewSuccessful = true;
        setTimeout(() => {
          this.closeTheDialogAndReloadPage();
        }, TIME_DELAY_BETWEEN_SUBMIT_AND_RELOAD_IN_MS);
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * Closes the dialog and refreshes the page afterwards.
     */
    closeTheDialogAndReloadPage() {
      const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
      dialogRefToDisplay.close();
    },
  },
});
</script>

<style>
pre#dataset-container {
  background: white;
  padding: 20px;
  border: 1px solid black;
}
</style>
