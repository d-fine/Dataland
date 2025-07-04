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
        await qaServiceControllerApi.changeQaStatus(this.dataId, this.qaStatus);
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
<style scoped>
.w-30 {
  width: 30vw;
}

.flex-direction-column {
  flex-direction: column;
}

.d-letters {
  letter-spacing: 0.05em;
}

.p-button {
  white-space: nowrap;
  cursor: pointer;
  font-weight: var(--button-fw);
  text-decoration: none;
  min-width: 10em;
  width: fit-content;
  justify-content: center;
  display: inline-flex;
  align-items: center;
  vertical-align: bottom;
  flex-direction: row;
  letter-spacing: 0.05em;
  font-family: inherit;
  transition: all 0.2s;
  border-radius: 0;
  text-transform: uppercase;
  font-size: 0.875rem;

  &:enabled:hover {
    color: white;
    background: hsl(from var(--btn-primary-bg) h s calc(l - 20));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 20));
  }

  &:enabled:active {
    background: hsl(from var(--btn-primary-bg) h s calc(l - 10));
    border-color: hsl(from var(--btn-primary-bg) h s calc(l - 10));
  }

  &:disabled {
    background-color: transparent;
    border: 0;
    color: var(--btn-disabled-color);
    cursor: not-allowed;
  }

  &:focus {
    outline: 0 none;
    outline-offset: 0;
    box-shadow: 0 0 0 0.2rem var(--btn-focus-border-color);
  }
}

.p-button {
  color: var(--btn-primary-color);
  background: var(--btn-primary-bg);
  border: 1px solid var(--btn-primary-bg);
  padding: var(--spacing-xs) var(--spacing-md);
  line-height: 1rem;
  margin: var(--spacing-xxs);

  &.p-button-sm {
    font-size: var(--font-size-sm);
    padding: var(--spacing-xs) var(--spacing-sm);
  }
}
</style>
