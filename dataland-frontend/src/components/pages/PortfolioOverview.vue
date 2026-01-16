<template>
  <TheContent class="min-h-screen relative">
    <PortfolioOverviewTabs :value="currentPortfolioId" :portfolioNames="portfolioNames" @update:value="onTabChange">
      <template #actions>
        <PrimeButton label="ADD NEW PORTFOLIO" @click="addNewPortfolio" icon="pi pi-plus" data-test="add-portfolio" />
      </template>
      <template #panel="{ portfolio }">
        <PortfolioDetails
          :portfolioId="portfolio.portfolioId"
          @update:portfolio-overview="getPortfolios"
          :data-test="`portfolio-${portfolio.portfolioName}`"
        />
      </template>
    </PortfolioOverviewTabs>
  </TheContent>
</template>

<script setup lang="ts">
import TheContent from '@/components/generics/TheContent.vue';
import PortfolioDetails from '@/components/resources/portfolio/PortfolioDetails.vue';
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
import PortfolioOverviewTabs from '@/components/resources/portfolio/PortfolioOverviewTabs.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { inject } from 'vue';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import { useDialog } from 'primevue/usedialog';
import type { BasePortfolioName } from '@clients/userservice';
import { usePortfolioOverview } from '@/components/resources/portfolio/usePortfolioOverview.ts';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialog = useDialog();
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const { currentPortfolioId, portfolioNames, getPortfolios, setCurrentPortfolioId, onTabChange } = usePortfolioOverview({
  sessionStorageKey: 'last-selected-portfolio-id',
  async fetchPortfolios(): Promise<BasePortfolioName[]> {
    const res = await apiClientProvider.apiClients.portfolioController.getAllPortfolioNamesForCurrentUser();
    return res.data;
  },
});

/**
 * Opens the PortfolioDialog, reloads all portfolios and
 * sets the newly created one as the active tab by updating currentPortfolioId.
 */
function addNewPortfolio(): void {
  dialog.open(PortfolioDialog, {
    props: {
      header: 'Add Portfolio',
      modal: true,
    },
    onClose(options) {
      const basePortfolioName = options?.data as BasePortfolioName;
      if (basePortfolioName) {
        void getPortfolios().then(() => {
          setCurrentPortfolioId(
            portfolioNames.value.find((p) => p.portfolioId === basePortfolioName.portfolioId)?.portfolioId
          );
        });
      }
    },
  });
}
</script>
