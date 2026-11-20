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
        <Chip :label="displayBalance" data-test="credits-balance-chip" />
      </div>
      <div class="dataland-info-text small">{{ displayLei }}</div>
    </template>
  </Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card';
import Divider from 'primevue/divider';
import { computed } from 'vue';
import Chip from 'primevue/chip';
import Button from 'primevue/button';
import { useStorage } from '@vueuse/core';
import Message from 'primevue/message';
import { useGetBalanceQuery } from '@/api-queries/accounting-service/credits/useGetBalanceQuery';
import { useGetCompanyInfoQuery } from '@/api-queries/backend/company-data/useGetCompanyInfoQuery';
import { getDisplayLei } from '@/utils/CompanyInformation.ts';

const props = defineProps<{
  companyId: string;
}>();

const { data: creditsBalance } = useGetBalanceQuery({ companyId: () => props.companyId });
const { data: companyInformation } = useGetCompanyInfoQuery({ companyId: () => props.companyId });

const showInfoMessage = useStorage<boolean>(`showInfoMessageCredits`, true);
const displayLei = computed(() => getDisplayLei(companyInformation.value));
const displayBalance = computed(() =>
  creditsBalance.value === undefined || creditsBalance.value === null ? '-' : creditsBalance.value.toString()
);

/**
 * Toggles the visibility of the information box. If the information box is currently displayed,
 * it will be hidden, and if it is hidden, it will be displayed.
 */
function toggleInfoBox(): void {
  showInfoMessage.value = !showInfoMessage.value;
}
</script>
