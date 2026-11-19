<template>
  <TheContent class="min-h-screen relative">
    <PortfolioOverviewTabs
      :value="currentPortfolioId"
      :portfolioNames="portfolioNames ?? []"
      @update:value="onTabChange"
    >
      <template #actions>
        <PrimeButton label="ADD NEW PORTFOLIO" @click="addNewPortfolio" icon="pi pi-plus" data-test="add-portfolio" />
      </template>
      <template #panel="{ portfolio }">
        <PortfolioDetails :portfolioId="portfolio.portfolioId" :data-test="`portfolio-${portfolio.portfolioName}`" />
      </template>
      <template #empty>
        <PortfolioOverviewEmptyState :is-pending="isPending" :is-error="isError" />
      </template>
    </PortfolioOverviewTabs>
  </TheContent>
</template>

<script setup lang="ts">
import TheContent from '@/components/generics/TheContent.vue';
import PortfolioDetails from '@/components/resources/portfolio/PortfolioDetails.vue';
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
import PortfolioOverviewEmptyState from '@/components/resources/portfolio/PortfolioOverviewEmptyState.vue';
import PortfolioOverviewTabs from '@/components/resources/portfolio/PortfolioOverviewTabs.vue';
import { useGetAllPortfolioNamesForCurrentUserQuery } from '@/api-queries/user-service/portfolio/useGetAllPortfolioNamesForCurrentUserQuery';
import PrimeButton from 'primevue/button';
import { useDialog } from 'primevue/usedialog';
import type { BasePortfolioName } from '@clients/userservice';
import { usePortfolioOverview } from '@/components/resources/portfolio/usePortfolioOverview.ts';

const dialog = useDialog();

const { data: portfolioNames, isPending, isError } = useGetAllPortfolioNamesForCurrentUserQuery();

const { currentPortfolioId, selectPortfolio, onTabChange } = usePortfolioOverview({
  sessionStorageKey: 'last-selected-portfolio-id',
  portfolioNames,
});

/**
 * Opens the PortfolioDialog and selects the newly created portfolio once the dialog is closed.
 * The portfolio list itself is refreshed by the create mutation.
 */
function addNewPortfolio(): void {
  dialog.open(PortfolioDialog, {
    props: {
      header: 'Add Portfolio',
      modal: true,
    },
    onClose(options) {
      const basePortfolioName = options?.data as BasePortfolioName | undefined;
      if (basePortfolioName) {
        selectPortfolio(basePortfolioName.portfolioId);
      }
    },
  });
}
</script>
