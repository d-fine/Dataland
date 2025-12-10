<template>
  <Card
    style="width: 70%; margin: 0 auto; margin-top: var(--spacing-xl); margin-bottom: var(--spacing-xl)"
    data-test="creditsBalance"
  >
    <template #title>
      <div style="display: flex; align-items: center">
        <i
          class="pi pi-credit-card"
          style="margin-right: 0.5rem; font-size: var(--font-size-lg); position: relative; top: 1px"
        ></i>
        <span>Credits</span>

      <Button
          icon="pi pi-info-circle"
          variant="text"
          data-test="info-icon"
          rounded
          @click="showInfoBox"
          title="Show Info"
          :style="{ visibility: showInfoMessage ? 'hidden' : 'visible' }"
      />
      </div>
    </template>
    <template #subtitle>
      <Message
          v-if="showInfoMessage"
          severity="warn"
          :closable="true"
          @close="hideInfoBox"
          style="margin-top: var(--spacing-xs); min-height: 3rem"
          data-test="info-message"
      >
        {{ 'Credits may be deducted automatically when using actively monitored portfolios.' }}
      </Message>
    </template>

    <template #content>
      <Divider />
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>Current Amount of Credits:</span>
        <Chip :label="creditsBalance.value" />
      </div>
      <div class="dataland-info-text small">{{ props.companyId }}</div>
    </template>
  </Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card';
import Divider from 'primevue/divider';
import { defineProps, inject, onMounted, ref } from 'vue';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import Chip from 'primevue/chip';
import Button from "primevue/button";
import {useStorage} from "@vueuse/core";
import Message from "primevue/message";

const creditsBalance = ref<number | any>(0);
const props = defineProps<{
  companyId: string;
}>();

onMounted(async () => {
  await getCreditsBalanceForCompany();
});

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const showInfoMessage = useStorage<boolean>(`showInfoMessageCredits`, true);

/**
 * Gets the current balance of credits for the company.
 */
async function getCreditsBalanceForCompany(): Promise<void> {
  if (!props.companyId) return;
  try {
    const response = await apiClientProvider.apiClients.creditsController.getBalance(props.companyId);
    creditsBalance.value = response.data;
  } catch (error) {
    console.error('Failed to get credit balance:', error);
  }
}

/**
 * Hides the info box
 */
function hideInfoBox(): void {
  showInfoMessage.value = false;
}

/**
 * Shows the info box
 */
function showInfoBox(): void {
  showInfoMessage.value = true;
}

</script>

<style scoped></style>
