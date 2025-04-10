<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="2">
      <TheContent class="min-h-screen paper-section relative">
        <TabView
          v-model:activeIndex="currentIndex"
          @tab-change="onTabChange"
          :scrollable="true"
          :data-test="'portfolios'"
        >
          <TabPanel v-for="portfolio in portfolios" :key="portfolio.portfolioId" :header="portfolio.portfolioName">
            <PortfolioDetails :portfolioId="portfolio.portfolioId" />
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
import type { BasePortfolioName } from '@clients/userservice';
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
const portfolios = ref<BasePortfolioName[]>();

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
  (newIndex, oldIndex) => {
    if (newIndex == portfolios.value?.length) {
      addNewPortfolio();
      currentIndex.value = oldIndex;
      return;
    }
  },
  { flush: 'post' }
);

/**
 * Retrieve all portfolios for the currently logged-in user.
 */
function getPortfolios(): void | undefined {
  apiClientProvider.apiClients.portfolioController
    .getAllPortfolioNamesForCurrentUser()
    .then((response) => {
      portfolios.value = response.data;
    })
    .catch((reason) => console.error(reason));
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
