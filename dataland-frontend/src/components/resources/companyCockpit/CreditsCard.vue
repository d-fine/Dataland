<template>
  <Card style="width: 70%; margin: var(--spacing-xl) auto" data-test="creditsBalance">
    <template #title>
      <div style="display: flex; align-items: center">
        <i
          class="pi pi-credit-card"
          style="margin-right: var(--spacing-xs); font-size: var(--font-size-lg); position: relative; top: 1px"
        ></i>
        <span>Credits</span>

        <Button
          icon="pi pi-info-circle"
          variant="text"
          data-test="info-icon"
          rounded
          @click="toggleInfoBox"
          title="Show Info"
          :style="{ visibility: showInfoMessage ? 'hidden' : 'visible' }"
        />
      </div>
    </template>
    <template #subtitle>
      <Message
        v-if="showInfoMessage"
        severity="info"
        :closable="true"
        @click="toggleInfoBox"
        style="margin-top: var(--spacing-xs); min-height: 3rem"
        data-test="info-message"
      >
        {{ 'Any questions regarding your credits? Contact info@dataland.com' }}
      </Message>
    </template>

    <template #content>
      <Divider />
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>Current Amount of Credits:</span>
        <Chip :label="creditsBalance !== null ? creditsBalance.toString() : '-'" data-test="credits-balance-chip" />
      </div>
      <div class="dataland-info-text small">{{ displayLei }}</div>
    </template>
  </Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card';
import Divider from 'primevue/divider';
import { computed, inject, onMounted, ref, watch } from 'vue';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import Chip from 'primevue/chip';
import Button from 'primevue/button';
import { useStorage } from '@vueuse/core';
import Message from 'primevue/message';
import { type AxiosResponse } from 'axios';
import { type CompanyInformation } from '@clients/backend';
import { getCompanyInformation, getDisplayLei } from '@/utils/CompanyInformation.ts';

const creditsBalance = ref<number | null>(null);
const props = defineProps<{
  companyId: string;
}>();

watch(
  () => props.companyId,
  async (newId) => {
    if (newId) {
      await getCreditsBalanceForCompany();
    }
  }
);

onMounted(async () => {
  await loadCompanyInformation();
  await getCreditsBalanceForCompany();
});

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const showInfoMessage = useStorage<boolean>(`showInfoMessageCredits`, true);
const companyInformation = ref<CompanyInformation | null>(null);
const displayLei = computed(() => getDisplayLei(companyInformation.value));

/**
 * Gets the current balance of credits for the company.
 */
async function getCreditsBalanceForCompany(): Promise<void> {
  if (!props.companyId) return;
  try {
    const response = (await apiClientProvider.apiClients.creditsController.getBalance(
      props.companyId
    )) as AxiosResponse<number>;
    creditsBalance.value = response.data;
  } catch (error) {
    console.error('Failed to get credit balance:', error);
  }
}

/**
 * Loads the company information from the backend.
 */
async function loadCompanyInformation(): Promise<void> {
  const result = await getCompanyInformation(props.companyId, apiClientProvider, assertDefined(getKeycloakPromise));
  companyInformation.value = result.companyInformation;
}

/**
 * Toggles the visibility of the information box. If the information box is currently displayed,
 * it will be hidden, and if it is hidden, it will be displayed.
 */
function toggleInfoBox(): void {
  showInfoMessage.value = !showInfoMessage.value;
}
</script>
