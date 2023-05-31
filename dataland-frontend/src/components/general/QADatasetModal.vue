<template>
  <div>
    <pre id="dataset-container">{{ datasetAsJson }}</pre>
  </div>
  <MiddleCenterDiv class="col-12">
    <div>
      <PrimeButton @click="setQualityStatusToApproved" label="Accept Dataset" />
    </div>
    <div>
      <PrimeButton @click="setQualityStatusToRejected" label="Reject Dataset" />
    </div>
  </MiddleCenterDiv>
</template>

<script lang="ts">
import {defineComponent, inject} from "vue";
import PrimeButton from "primevue/button";
import {DynamicDialogInstance} from "primevue/dynamicdialogoptions";
import {ApiClientProvider} from "@/services/ApiClients";
import {assertDefined} from "@/utils/TypeScriptUtils";
import Keycloak from "keycloak-js";
import MiddleCenterDiv from "@/components/wrapper/MiddleCenterDivWrapper.vue";

export default defineComponent( {
  components: {MiddleCenterDiv, PrimeButton},
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
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      dataSetToReview: object;
      dataId: string,
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
      const qaServiceControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
      ).getQaControllerApi();
      await qaServiceControllerApi.assignQualityStatus(this.dataId, "Accepted");
    },
    /**
     * Sets dataset to rejected
     */
    async setQualityStatusToRejected() {
      const qaServiceControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
      ).getQaControllerApi();
      await qaServiceControllerApi.assignQualityStatus(this.dataId, "Rejected");
    },
  }
});
</script>

<style>
pre#dataset-container {
  background: white;
  padding: 20px;
  border: 1px solid black;
}
</style>