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
import { computed, defineProps } from 'vue';

import Chip from 'primevue/chip';
import Button from 'primevue/button';
import { useStorage } from '@vueuse/core';
import Message from 'primevue/message';
import type { CompanyInformation } from '@clients/backend';
import { getDisplayLei } from '@/utils/CompanyInformation.ts';
import { useCompanyCreditsQuery } from '@/queries/composables/useCompanyCreditsQuery.ts';
import { useCompanyInformationQuery } from '@/queries/composables/useCompanyInformationQuery.ts';

const props = defineProps<{
  companyId: string;
}>();

const showInfoMessage = useStorage<boolean>('showInfoMessageCredits', true);
const companyInformation = computed<CompanyInformation | null>(() => companyInformationData.value ?? null);
const displayLei = computed(() => getDisplayLei(companyInformation.value));

const {
  data: creditsBalance,
  isPending: isCreditsPending,
  isError: isCreditsError,
} = useCompanyCreditsQuery(props.companyId);

const {
  data: companyInformationData,
  isPending: isCompanyInformationPending,
  isError: isCompanyInformationError,
} = useCompanyInformationQuery(props.companyId);

function hideInfoBox(): void {
  showInfoMessage.value = false;
}

function showInfoBox(): void {
  showInfoMessage.value = true;
}
</script>
