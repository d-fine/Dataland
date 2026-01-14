<template>
  <TheContent class="min-h-screen relative">
    <Tabs :value="activePortfolioId" :scrollable="true" data-test="portfolios" @update:value="onTabChange">
      <div class="tabs-container">
        <TabList>
          <template v-if="isPending">
            <Tab value="loading-placeholder" disabled>
              <Skeleton width="8rem" height="1.2rem" />
            </Tab>
            <Tab value="loading-2" disabled>
              <Skeleton width="6rem" height="1.2rem" />
            </Tab>
            <Tab value="loading-3" disabled>
              <Skeleton width="7rem" height="1.2rem" />
            </Tab>
          </template>

          <template v-else>
            <Tab v-for="portfolio in portfolioNames" :key="portfolio.portfolioId" :value="portfolio.portfolioId">
              <div class="tabview-header" :title="portfolio.portfolioName" :data-test="portfolio.portfolioName">
                {{ portfolio.portfolioName }}
              </div>
            </Tab>
          </template>
        </TabList>

        <PrimeButton
          label="ADD NEW PORTFOLIO"
          @click="addNewPortfolio"
          icon="pi pi-plus"
          data-test="add-portfolio"
          :disabled="isPending"
        />
      </div>

      <TabPanels>
        <TabPanel v-if="isPending" value="loading-placeholder">
          <div class="flex flex-col gap-4">
            <Skeleton width="40%" height="2rem" class="mb-2" />
            <Skeleton width="100%" height="10rem" />
          </div>
        </TabPanel>

        <template v-else>
          <TabPanel v-for="portfolio in portfolioNames" :key="portfolio.portfolioId" :value="portfolio.portfolioId">
            <PortfolioDetails
              :portfolioId="portfolio.portfolioId"
              :data-test="`portfolio-${portfolio.portfolioName}`"
            />
          </TabPanel>
          <TabPanel value="no-portfolios-available">
            <h1 v-if="!portfolioNames || portfolioNames.length == 0">No Portfolios available.</h1>
          </TabPanel>
        </template>
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
import { computed, inject, watch } from 'vue';
import { useSessionStorage } from '@vueuse/core';
import { useQuery, useQueryClient, keepPreviousData } from '@tanstack/vue-query';
import Skeleton from 'primevue/skeleton';

/**
 * This component displays the portfolio overview page, allowing users to view and manage their portfolios.
 * It includes functionality to add new portfolios, and to switch between existing ones.
 */

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialog = useDialog();

const SESSION_STORAGE_KEY = 'last-selected-portfolio-id';
const userSelectedPortfolioId = useSessionStorage<string | undefined>(SESSION_STORAGE_KEY, undefined);

const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const queryClient = useQueryClient();
const {
  data: portfolioNames,
  isLoading,
  isPending,
} = useQuery<BasePortfolioName[]>({
  queryKey: ['portfolioNames'],
  placeholderData: keepPreviousData,
  staleTime: 1000 * 60 * 5, // 5 minutes
  gcTime: 1000 * 60 * 10, // Keep in Garbage Collection for 10 minutes
  queryFn: () =>
    apiClientProvider.apiClients.portfolioController
      .getAllPortfolioNamesForCurrentUser()
      .then((response) => response.data),
});

const activePortfolioId = computed((): string => {
  if (isPending.value) return 'loading-placeholder';
  const portfolios = portfolioNames.value || [];
  if (portfolios.length === 0) return 'no-portfolios-available';

  const currentSelection = userSelectedPortfolioId.value;
  const isSelectionValid = currentSelection && portfolios.some((p) => p.portfolioId === currentSelection);
  if (isSelectionValid) return currentSelection;

  return portfolios[0]?.portfolioId || 'no-portfolios-available';
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
    async onClose(options) {
      const basePortfolioName = options?.data as BasePortfolioName;
      if (basePortfolioName) {
        userSelectedPortfolioId.value = basePortfolioName.portfolioId;
        await queryClient.invalidateQueries({ queryKey: ['portfolioNames'] });
      }
    },
  });
}

/**
 * Handles the tab change event by changing the currentPortfolioId.
 * @param value The value of the tab aka the portfolioId of the selected portfolio.
 */
function onTabChange(value: string | number): void {
  userSelectedPortfolioId.value = String(value);
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
