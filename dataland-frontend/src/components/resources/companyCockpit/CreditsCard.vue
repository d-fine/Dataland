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
        severity="info"
        :closable="true"
        @close="hideInfoBox"
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
        <Chip :label="creditsBalance" data-test="credits-balance-chip" />
      </div>
      <div class="dataland-info-text small">{{ displayLei }}</div>
    </template>
  </Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card';
import Divider from 'primevue/divider';
import { computed, defineProps, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import Chip from 'primevue/chip';
import Button from 'primevue/button';
import { useStorage } from '@vueuse/core';
import Message from 'primevue/message';
import type { CompanyInformation } from '@clients/backend';
import { getDisplayLei } from '@/utils/CompanyInformation.ts';
import { useQuery } from '@tanstack/vue-query';

const props = defineProps<{
  companyId: string;
}>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const showInfoMessage = useStorage<boolean>('showInfoMessageCredits', true);


const {
  data: creditsBalanceData,
  isPending: isCreditsPending,
  isError: isCreditsError,
  error: creditsError,
} = useQuery({
  queryKey: ['creditsBalance', props.companyId],
  enabled: computed(() => !!props.companyId),
  queryFn: async () => {
    const response = await apiClientProvider.apiClients.creditsController.getBalance(props.companyId);
    return response.data as number;
  },
});

const creditsBalance = computed(() => creditsBalanceData.value ?? 0);

const {
  data: companyInformationData,
  isPending: isCompanyPending,
  isError: isCompanyError,
  error: companyError,
} = useQuery({
  queryKey: ['companyInformation', props.companyId],
  enabled: computed(() => !!props.companyId),
  queryFn: async () => {
    const res = await apiClientProvider.backendClients.companyDataController.getCompanyInfo(props.companyId);
    return res.data as CompanyInformation;

  },
});

const companyInformation = computed<CompanyInformation | null>(() => companyInformationData.value ?? null);
const displayLei = computed(() => getDisplayLei(companyInformation.value));




function hideInfoBox(): void {
  showInfoMessage.value = false;
}

function showInfoBox(): void {
  showInfoMessage.value = true;
}
</script>

