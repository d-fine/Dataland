<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="2">
      <TheContent class="min-h-screen paper-section relative">
        <TabView v-model:activeIndex="currentIndex" @tab-change="onTabChange" class="col-12" :scrollable="true">
          <TabPanel v-for="portfolio in portfolios" :key="portfolio.portfolioId" :header="portfolio.portfolioName">
            <!-- Insert component to display a specific portfolio here -->
            <div v-if="isLoading" class="d-center-div">
              <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
            </div>
            <PortfolioDetails v-else :portfolio="portfolio"></PortfolioDetails>
          </TabPanel>
          <TabPanel>
            <template #header>
              <div class="p-tabview-nav"><i class="pi pi-plus pr-2 align-self-center"></i> New Portfolio</div>
            </template>
            <h1>New Portfolio dialog here</h1>
          </TabPanel>
        </TabView>
      </TheContent>
      <TheFooter :is-light-version="true" :sections="footerSections" />
    </DatasetsTabMenu>
  </AuthenticationWrapper>
</template>

<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import type { Content, Section } from '@/types/ContentTypes.ts';
import contentData from '@/assets/content.json';
import TheContent from '@/components/generics/TheContent.vue';
import type { BasePortfolio } from '@clients/userservice';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import TabView, { type TabViewChangeEvent } from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';
import PortfolioDetails from '@/components/resources/portfolio/PortfolioDetails.vue';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const currentIndex = ref(0);
const currentPortfolio = ref<BasePortfolio>();
const isLoading = ref(false);
const portfolios = ref<BasePortfolio[]>();

const content: Content = contentData;
const footerSections: Section[] | undefined = content.pages.find((page) => page.url === '/')?.sections;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

onMounted(() => {
  getPortfolios();
});

/**
 * If currentIndex changes, retrieve portfolio for new index.
 * For the watcher not to get batched with tabChange event, we need to set option flush: 'post'.
 */
watch(
  currentIndex,
  async (newIndex, oldIndex) => {
    if (newIndex == portfolios.value?.length) {
      addNewPortfolio();
      currentIndex.value = oldIndex;
      return;
    }
    await getPortfolio(newIndex);
  },
  { flush: 'post' }
);

/**
 * Retrieve all portfolios for the currently logged-in user.
 */
function getPortfolios(): void | undefined {
  apiClientProvider.apiClients.portfolioController
    .getAllPortfoliosForCurrentUser()
    .then((response) => {
      portfolios.value = response.data;
    })
    .catch((reason) => console.error(reason));
}

/**
 * Get portfolio for index
 * @param index index in 'portfolios' corresponding to portfolio to retrieve
 */
async function getPortfolio(index: number): Promise<void | undefined> {
  if (!portfolios.value) return;
  try {
    isLoading.value = true;
    const response = await apiClientProvider.apiClients.portfolioController.getPortfolio(
      portfolios.value[currentIndex.value].portfolioId
    );
    currentPortfolio.value = response.data;
  } catch (error) {
    console.log(`Error while loading portfolio for tabIndex ${index}:`);
    throw error;
  } finally {
    isLoading.value = false;
  }
}

/**
 * Called when currently active portfolio is changed.
 */
function onTabChange(event: TabViewChangeEvent): void {
  currentIndex.value = event.index;
}

/**
 *
 */
function addNewPortfolio(): void {
  console.log('Add new Portfolio');
}
</script>

<style scoped lang="scss"></style>
