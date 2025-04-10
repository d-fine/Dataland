<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="2">
      <TheContent class="min-h-screen paper-section relative">
        <TabView v-model:activeIndex="currentIndex" @tab-change="onTabChange" :scrollable="true" data-test="portfolios">
          <TabPanel v-for="portfolio in portfolioNames" :key="portfolio.portfolioId" :header="portfolio.portfolioName">
            <PortfolioDetails :portfolioId="portfolio.portfolioId" />
          </TabPanel>
          <TabPanel>
            <template #header>
              <div class="p-tabview-nav" @click="addNewPortfolio">
                <span class="align-self-start"><i class="pi pi-plus pr-2" /> New Portfolio</span>
              </div>
            </template>
            <h1 v-if="!portfolioNames || portfolioNames.length == 0">No Portfolios available.</h1>
          </TabPanel>
        </TabView>
      </TheContent>
      <TheFooter :is-light-version="true" :sections="footerSections" />
    </DatasetsTabMenu>
  </AuthenticationWrapper>
</template>

<script setup lang="ts">
import contentData from '@/assets/content.json';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import PortfolioDialog from '@/components/general/PortfolioDialog.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import PortfolioDetails from '@/components/resources/portfolio/PortfolioDetails.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import type { Content, Section } from '@/types/ContentTypes.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type { BasePortfolioName } from '@clients/userservice';
import type Keycloak from 'keycloak-js';
import TabPanel from 'primevue/tabpanel';
import TabView, { type TabViewChangeEvent } from 'primevue/tabview';
import { useDialog } from 'primevue/usedialog';
import { inject, onMounted, ref, watch } from 'vue';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialog = useDialog();

const currentIndex = ref(0);
const portfolioNames = ref<BasePortfolioName[]>([]);

const content: Content = contentData;
const footerSections: Section[] | undefined = content.pages.find((page) => page.url === '/')?.sections;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

onMounted(() => {
  void getPortfolios();
});

/**
 * If currentIndex changes, retrieve portfolio for new index.
 * For the watcher not to get batched with tabChange event, we need to set option flush: 'post'.
 */
watch(
  currentIndex,
  (newIndex, oldIndex) => {
    if (portfolioNames.value.length == 0 || newIndex == portfolioNames.value.length) {
      currentIndex.value = oldIndex;
      return;
    }
  },
  { flush: 'post' }
);

/**
 * Retrieve all portfolios for the currently logged-in user.
 */
async function getPortfolios(): Promise<void> {
  return apiClientProvider.apiClients.portfolioController
    .getAllPortfolioNamesForCurrentUser()
    .then((response) => {
      portfolioNames.value = response.data;
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
 * Opens the PortfolioDialog. OnClose, it reloads all portfolios and
 */
function addNewPortfolio(): void {
  dialog.open(PortfolioDialog, {
    props: {
      header: 'Add Portfolio',
      modal: true,
    },
    onClose(options) {
      const portfolioName = options?.data as BasePortfolioName;
      if (portfolioName) {
        void getPortfolios().then(() => {
          currentIndex.value = portfolioNames.value.findIndex(
            (portfolio) => portfolio.portfolioId == portfolioName.portfolioId
          );
        });
      }
    },
  });
}
</script>

<style scoped lang="scss"></style>
