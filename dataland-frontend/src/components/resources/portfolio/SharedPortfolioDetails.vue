<template>
  <PortfolioDetailsBase
      :portfolio-id="portfolioId"
      empty-text="Currently there are no companies in this portfolio or no companies match your filters."
  >
    <template
        #actions="{
        enrichedPortfolio,
        monitoredTagAttributes,
        resetFilters,
        openDownload,
        reload,
      }"
    >
      <Button
          @click="openRemoveModal()"
          data-test="remove-portfolio"
          label="REMOVE PORTFOLIO"
          icon="pi pi-times"
      />
      <Button
          @click="openDownload"
          data-test="download-portfolio"
          label="DOWNLOAD PORTFOLIO"
          icon="pi pi-download"
      />
      <Tag v-bind="monitoredTagAttributes" data-test="is-monitored-tag" />
      <Tag
          :value="`Shared by ${enrichedPortfolio?.userId ?? ''}`"
          icon="pi pi-share-alt"
          severity="info"
          data-test="shared-by-tag"
      />
      <Button
          class="reset-button-align-right"
          data-test="reset-filter"
          @click="resetFilters"
          variant="text"
          label="RESET"
      />
    </template>

    <template #dialogs="{ reload }">
      <PortfolioRemoveSharing
          :visible="isRemoveDialogVisible"
          :portfolio-id="portfolioId"
          @close="() => closeRemoveDialog(reload)"
      />
    </template>
  </PortfolioDetailsBase>
</template>

<script setup lang="ts">
import PortfolioDetailsBase from '@/components/resources/portfolio/PortfolioDetailsBase.vue';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import { ref } from 'vue';
import PortfolioRemoveSharing from '@/components/resources/portfolio/PortfolioRemoveSharing.vue';

const props = defineProps<{
  portfolioId: string;
}>();

const emit = defineEmits(['update:portfolio-overview']);

const isRemoveDialogVisible = ref(false);

/**
 * Opens the remove dialog for the current portfolio.
 */
function openRemoveModal(): void {
  isRemoveDialogVisible.value = true;
}

/**
 * Closes the remove dialog, reloads the portfolio data and notifies the overview.
 */
function closeRemoveDialog(reload: () => void): void {
  isRemoveDialogVisible.value = false;
  reload();
  emit('update:portfolio-overview');
}
</script>

<style scoped>
label {
  margin-left: 0.5em;
}

.reset-button-align-right {
  margin-left: auto;
}
</style>
