<template>
  <TheContent class="min-h-screen relative">
    <PortfolioOverviewTabs :value="currentPortfolioId" :portfolioNames="portfolioNames" @update:value="onTabChange">
      <template #panel="{ portfolio }">
        <SharedPortfolioDetails
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
import SharedPortfolioDetails from '@/components/resources/portfolio/SharedPortfolioDetails.vue';
import PortfolioOverviewTabs from '@/components/resources/portfolio/PortfolioOverviewTabs.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { inject } from 'vue';
import type Keycloak from 'keycloak-js';
import type { BasePortfolioName } from '@clients/userservice';
import { usePortfolioOverview } from '@/components/resources/portfolio/usePortfolioOverview.ts';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const { currentPortfolioId, portfolioNames, getPortfolios, onTabChange } = usePortfolioOverview({
  sessionStorageKey: 'last-selected-shared-portfolio-id',
  async fetchPortfolios(): Promise<BasePortfolioName[]> {
    const res = await apiClientProvider.apiClients.portfolioController.getAllSharedPortfolioNamesForCurrentUser();
    return res.data;
  },
});
</script>
