<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initial-tab-index="2">
      <TheContent class="min-h-screen paper-section relative">
        <TabView v-model:activeIndex="currentIndex" @tab-change="onTabChange" :scrollable="true" data-test="portfolios">
          <TabPanel v-for="portfolio in portfolioNames" :key="portfolio.portfolioId" :header="portfolio.portfolioName">
            <PortfolioDetails
              :portfolioId="portfolio.portfolioId"
              @update:portfolio-overview="getPortfolios"
              :data-test="`portfolio-${portfolio.portfolioName}`"
            />
          </TabPanel>
          <TabPanel>
            <template #header>
              <div class="p-tabview-nav" @click="addNewPortfolio" data-test="addNewPortfolio">
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
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
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
import { useRoute, useRouter } from 'vue-router';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialog = useDialog();

const currentIndex = ref(0);
const portfolioNames = ref<BasePortfolioName[]>([]);

const content: Content = contentData;
const footerSections: Section[] | undefined = content.pages.find((page) => page.url === '/')?.sections;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const route = useRoute();
const router = useRouter();

onMounted(() => {
  void getPortfolios();
});

/**
 * If currentIndex changes, retrieve portfolio for new index.
 * For the watcher not to get batched with tabChange event, we need to set option flush: 'post'.
 */
watch(
  currentIndex,
  (newIndex) => {
    if (portfolioNames.value.length > 0 && newIndex < portfolioNames.value.length) {
      const name = encodeURI(portfolioNames.value[newIndex].portfolioName);
      void router.replace({ name: 'Portfolio Overview', params: { portfolioName: name } });
    }
  },
  { flush: 'post' }
);

/**
 * If current name changes, retrieve portfolio for new name.
 */
watch(
  () => route.params.portfolioName,
  (newName) => {
    const newIndex = portfolioNames.value.findIndex((p) => decodeURI(p.portfolioName) === newName);
    if (newIndex !== -1) {
      currentIndex.value = newIndex;
    }
  }
);

/**
 * If page is revisited, retrieve last watched portfolio.
 */
watch(portfolioNames, (newPortfolios) => {
  if (newPortfolios.length > 0) {
    let name = route.params.portfolioName as string | undefined;

    if (!name) {
      name = localStorage.getItem('lastPortfolioName') ?? '';
    }
    const matchedIndex = newPortfolios.findIndex((p) => decodeURI(p.portfolioName) === name);
    if (matchedIndex !== -1) {
      currentIndex.value = matchedIndex;
      void router.replace({ name: 'Portfolio Overview', params: { portfolioName: name } });
    } else {
      currentIndex.value = 0;
      const fallbackName = decodeURI(newPortfolios[0].portfolioName);
      void router.replace({ name: 'Portfolio Overview', params: { portfolioName: fallbackName } });
    }
  }
});

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

  const selectedPortfolio = portfolioNames.value[event.index];
  if (selectedPortfolio) {
    const name = encodeURI(selectedPortfolio.portfolioName);
    localStorage.setItem('lastPortfolioName', name);
    void router.push({ name: 'Portfolio Overview', params: { portfolioName: name } });
  }
}

/**
 * Opens the PortfolioDialog, reloads all portfolios and
 * sets the newly created one as the active tab by updating currentIndex.
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

<style scoped lang="scss">
:deep(.p-tabview-title) {
  max-width: 15em;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
