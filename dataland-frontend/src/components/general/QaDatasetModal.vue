<template>
  <MiddleCenterDiv class="w-30 flex-direction-column">
    <div>{{ message }}</div>
    <div v-if="!reviewSubmitted" class="text-center px-7 py-4">
      <p class="font-medium text-xl">Submitting...</p>
      <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
    </div>
    <div v-if="reviewSubmitted" class="col-12 text-center">
      <SuccessMessage
        v-if="reviewSuccessful"
        data-test="qaReviewSubmittedMessage"
        success-message="Review successfully submitted."
        :closable="false"
      />
      <FailMessage
        v-else
        message="The resource you tried to access is not available. Please close the data pop-up."
        :closable="false"
      />
      <PrimeButton class="uppercase p-button p-button-sm" @click="closeTheDialog()">
        <span class="d-letters pl-2">CLOSE</span>
      </PrimeButton>
    </div>
  </MiddleCenterDiv>
</template>

<script lang="ts">
import { defineComponent, inject } from 'vue';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import PrimeButton from 'primevue/button';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import MiddleCenterDiv from '@/components/wrapper/MiddleCenterDivWrapper.vue';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import { QaStatus } from '@clients/backend';

export default defineComponent({
  components: { PrimeButton, FailMessage, SuccessMessage, MiddleCenterDiv },
  inject: ['dialogRef'],
  name: 'QADatasetModal',
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data() {
    return {
      dataId: '',
      qaStatus: QaStatus.Pending as QaStatus,
      message: '',
      reviewSubmitted: false,
      reviewSuccessful: false,
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      dataId: string;
      qaStatus: QaStatus;
      message: string;
    };
    this.dataId = dialogRefData.dataId;
    this.qaStatus = dialogRefData.qaStatus;
    this.message = dialogRefData.message;

    void this.setQaStatus();
  },
  methods: {
    /**
     * Sets dataset quality status to the given status
     */
    async setQaStatus() {
      try {
        const qaServiceControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).apiClients
          .qaController;
        await qaServiceControllerApi.assignQaStatus(this.dataId, this.qaStatus);
        this.reviewSubmitted = true;
        this.reviewSuccessful = true;
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * Closes the dialog and refreshes the page afterwards.
     */
    closeTheDialog() {
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
