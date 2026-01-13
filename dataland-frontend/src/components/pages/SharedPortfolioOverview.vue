<template>
  <TheContent class="min-h-screen relative">
    <Tabs
      :value="currentPortfolioId || 'no-portfolios-available'"
      :scrollable="true"
      data-test="portfolios"
      @update:value="onTabChange"
    >
      <div class="tabs-container">
        <TabList>
          <Tab v-for="portfolio in portfolioNames" :key="portfolio.portfolioId" :value="portfolio.portfolioId">
            <div class="tabview-header" :title="portfolio.portfolioName" :data-test="portfolio.portfolioName">
              {{ portfolio.portfolioName }}
            </div>
          </Tab>
        </TabList>
      </div>
      <TabPanels>
        <TabPanel v-for="portfolio in portfolioNames" :key="portfolio.portfolioId" :value="portfolio.portfolioId">
          <SharedPortfolioDetails
            :portfolioId="portfolio.portfolioId"
            @update:portfolio-overview="getPortfolios"
            :data-test="`portfolio-${portfolio.portfolioName}`"
          />
        </TabPanel>
        <TabPanel value="no-portfolios-available">
          <h1 v-if="!portfolioNames || portfolioNames.length === 0">No Portfolios available.</h1>
        </TabPanel>
      </TabPanels>
    </Tabs>
  </TheContent>
</template>

<script setup lang="ts">
import TheContent from '@/components/generics/TheContent.vue';
import SharedPortfolioDetails from '@/components/resources/portfolio/SharedPortfolioDetails.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { inject } from 'vue';
import type Keycloak from 'keycloak-js';
import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';
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

<style scoped>
.tabs-container {
  display: flex;
  flex-direction: row;
  gap: var(--spacing-md);
  align-items: center;
  justify-content: space-between;
  padding-right: var(--spacing-md);
}

.tabview-header {
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-base);
  max-width: 15rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
