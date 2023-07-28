<template>
  <div>
    <pre id="dataset-container">{{ datasetAsJson }}</pre>
  </div>
  <MiddleCenterDiv class="col-12">
    <div v-if="reviewSubmitted">
      <SuccessMessage v-if="reviewSuccessful" success-message="Review successfully submitted." />
      <FailMessage v-else message="The resource you tried to access is not available. Please close the data pop-up." />
    </div>
    <div autofocus="autofocus" v-else>
      <PrimeButton @click="setQaStatusTo(QaStatus.Accepted)" label="Accept Dataset" id="accept-button" />
      <PrimeButton @click="setQaStatusTo(QaStatus.Rejected)" label="Reject Dataset" id="reject-button" />
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
import { TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS } from "@/utils/Constants";
import { QaStatus } from "@clients/qaservice";

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
      QaStatus,
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
     * Sets dataset quality status to the given status
     * @param qaStatus the QA status to be assigned
     */
    async setQaStatusTo(qaStatus: QaStatus) {
      try {
        this.reviewSubmitted = true;
        const qaServiceControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getQaControllerApi();
        await qaServiceControllerApi.assignQaStatus(this.dataId, qaStatus);
        this.reviewSuccessful = true;
        setTimeout(() => {
          this.closeTheDialogAndReloadPage();
        }, TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS);
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

#accept-button {
  color: var(--green-700);
  background: var(--green-100);
  border: 1px solid var(--green-700);
}

#reject-button {
  color: var(--red-700);
  background: var(--red-100);
  border: 1px solid var(--red-700);
}
</style>
