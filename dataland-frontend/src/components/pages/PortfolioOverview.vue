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
        <PrimeButton label="ADD NEW PORTFOLIO" @click="addNewPortfolio" icon="pi pi-plus" data-test="add-portfolio" />
      </div>
      <TabPanels>
        <TabPanel v-for="portfolio in portfolioNames" :key="portfolio.portfolioId" :value="portfolio.portfolioId">
          <PortfolioDetails
            :portfolioId="portfolio.portfolioId"
            :data-test="`portfolio-${portfolio.portfolioName}`"
          />
        </TabPanel>
        <TabPanel value="no-portfolios-available">
          <h1 v-if="!portfolioNames || portfolioNames.length == 0">No Portfolios available.</h1>
        </TabPanel>
      </TabPanels>
    </Tabs>
  </TheContent>
</template>

<script setup lang="ts">
import TheContent from '@/components/generics/TheContent.vue';
import PortfolioDetails from '@/components/resources/portfolio/PortfolioDetails.vue';
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type { BasePortfolioName } from '@clients/userservice';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import Tab from 'primevue/tab';
import TabList from 'primevue/tablist';
import TabPanel from 'primevue/tabpanel';
import TabPanels from 'primevue/tabpanels';
import Tabs from 'primevue/tabs';
import { useDialog } from 'primevue/usedialog';
import {inject, watch} from 'vue';
import { useSessionStorage } from '@vueuse/core';
import { useQuery, useQueryClient } from "@tanstack/vue-query";

/**
 * This component displays the portfolio overview page, allowing users to view and manage their portfolios.
 * It includes functionality to add new portfolios, and to switch between existing ones.
 */

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialog = useDialog();

const SESSION_STORAGE_KEY = 'last-selected-portfolio-id';
const currentPortfolioId = useSessionStorage<string | undefined>(SESSION_STORAGE_KEY, undefined);

const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const queryClient = useQueryClient();
const { data: portfolioNames } = useQuery<BasePortfolioName[]>({
  queryKey: ['portfolioNames'],
  queryFn: () =>
      apiClientProvider.apiClients.portfolioController
          .getAllPortfolioNamesForCurrentUser()
          .then((response) => response.data)
});

watch(
    portfolioNames,
    () => {
      if (
          !portfolioNames.value ||
          portfolioNames.value.length === 0
      ) {
        currentPortfolioId.value = undefined;
      } else if (!currentPortfolioId.value) {
        currentPortfolioId.value = portfolioNames.value[0].portfolioId;
      } else {
        const currentPortfolioIdExists = portfolioNames.value.some(
            (p) => p.portfolioId === currentPortfolioId.value
        );
        if (!currentPortfolioIdExists) {
          currentPortfolioId.value = portfolioNames.value[0].portfolioId;
        }
      }
    }
)

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
    async onClose(options) {
      const basePortfolioName = options?.data as BasePortfolioName;
      if (basePortfolioName) {
        await queryClient.invalidateQueries({ queryKey: ['portfolioNames'] });
        currentPortfolioId.value = basePortfolioName.portfolioId;
      }
    },
  });
}

/**
 * Handles the tab change event by changing the currentPortfolioId.
 * @param value The value of the tab aka the portfolioId of the selected portfolio.
 */
function onTabChange(value: string | number): void {
  currentPortfolioId.value = String(value);
}
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
