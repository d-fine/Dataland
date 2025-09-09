<template>
  <AuthenticationWrapper>
    <TheHeader />
    <DatasetsTabMenu :initialTabIndex="2">
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
            <PrimeButton
              label="ADD NEW PORTFOLIO"
              @click="addNewPortfolio"
              icon="pi pi-plus"
              data-test="add-portfolio"
            />
          </div>
          <TabPanels>
            <TabPanel v-for="portfolio in portfolioNames" :key="portfolio.portfolioId" :value="portfolio.portfolioId">
              <PortfolioDetails
                :portfolioId="portfolio.portfolioId"
                @update:portfolio-overview="getPortfolios"
                :data-test="`portfolio-${portfolio.portfolioName}`"
              />
            </TabPanel>
            <TabPanel value="no-portfolios-available">
              <h1 v-if="!portfolioNames || portfolioNames.length == 0">No Portfolios available.</h1>
            </TabPanel>
          </TabPanels>
        </Tabs>
      </TheContent>
      <TheFooter :is-light-version="true" :sections="footerSections" />
    </DatasetsTabMenu>
  </AuthenticationWrapper>
</template>

<script setup lang="ts">
import contentData from '@/assets/content.json';
import DatasetsTabMenu from '@/components/general/DatasetsTabMenu.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import PortfolioDetails from '@/components/resources/portfolio/PortfolioDetails.vue';
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import type { Content, Section } from '@/types/ContentTypes.ts';
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
import { inject, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useSessionStorage } from '@vueuse/core';

/**
 * This component displays the portfolio overview page, allowing users to view and manage their portfolios.
 * It includes functionality to add new portfolios, and to switch between existing ones.
 */

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialog = useDialog();
const route = useRoute();

const SESSION_STORAGE_KEY = 'last-selected-portfolio-id';
const currentPortfolioId = useSessionStorage<string | undefined>(SESSION_STORAGE_KEY, undefined);
const portfolioNames = ref<BasePortfolioName[]>([]);

const content: Content = contentData;
const footerSections: Section[] | undefined = content.pages.find((page) => page.url === '/')?.sections;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

onMounted(() => {
  void getPortfolios().then(() => setCurrentPortfolioId());
});

/**
 * Retrieve all portfolios for the currently logged-in user.
 */
async function getPortfolios(): Promise<void> {
  try {
    const response = await apiClientProvider.apiClients.portfolioController.getAllPortfolioNamesForCurrentUser();
    portfolioNames.value = response.data;
    setCurrentPortfolioId();
  } catch (error) {
    console.log(error);
  }
}

/**
 * Sets the current portfolio ID based on the following priority:
 * 1. If a portfolioId is provided (e.g. after creating a new portfolio), use it if valid.
 * 2. If not, and a route parameter for portfolioName is present, use the corresponding portfolioId if valid.
 * 3. If not, and a session-stored portfolioId exists, use it if valid.
 * 4. If none of the above are valid, fall back to the first portfolio in the list.
 */
function setCurrentPortfolioId(portfolioId?: string): void {
  if (portfolioNames.value.length === 0) {
    currentPortfolioId.value = undefined;
    return;
  }

  if (portfolioId && portfolioNames.value.some((portfolio) => portfolio.portfolioId === portfolioId)) {
    currentPortfolioId.value = portfolioId;
    return;
  }

  const routePortfolio = portfolioNames.value.find(
    (portfolio) => portfolio.portfolioName === route.params.portfolioName
  )?.portfolioId;
  if (routePortfolio) {
    currentPortfolioId.value = routePortfolio;
    return;
  }

  if (
    currentPortfolioId.value &&
    portfolioNames.value.some((portfolio) => portfolio.portfolioId === currentPortfolioId.value)
  ) {
    return; // already valid, keep as is
  }

  currentPortfolioId.value = portfolioNames.value[0].portfolioId;
}

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
            portfolioNames.value.find((portfolio) => portfolio.portfolioId == basePortfolioName.portfolioId)
              ?.portfolioId
          );
        });
      }
    },
  });
}

/**
 * Handles the tab change event. It changes the currentPortfolioId and updates the route to keep the URL in sync.
 * @param value The value of the tab aka the portfolioId of the selected portfolio.
 */
function onTabChange(value: string | number): void {
  setCurrentPortfolioId(String(value));
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
