<template>
  <Tabs :value="currentTabId" @update:value="onTabChange">
    <TabList>
      <Tab v-for="tab in visibleTabs" :key="tab.id" :value="tab.id">
        {{ tab.label }}
      </Tab>
    </TabList>
    <TabPanels>
      <TabPanel :value="currentTabId">
        <slot />
      </TabPanel>
    </TabPanels>
  </Tabs>
</template>

<script setup lang="ts">
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import type Keycloak from 'keycloak-js';
import Tab from 'primevue/tab';
import TabList from 'primevue/tablist';
import TabPanel from 'primevue/tabpanel';
import TabPanels from 'primevue/tabpanels';
import Tabs from 'primevue/tabs';
import { inject, onMounted, ref, type Ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import router from '@/router';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';

interface TabInfo {
  id: string;
  label: string;
  route: string;
  isVisible: boolean;
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const route = useRoute();

// Ref is needed since App.vue is written in the Options API and we need to use the Composition API here.
const companyRoleAssignments = assertDefined(
  inject<Ref<Array<CompanyRoleAssignmentExtended>>>('companyRoleAssignments')
);

const tabs = ref<Array<TabInfo>>([
  { id: 'my-portfolios', label: 'MY PORTFOLIOS', route: '/portfolios', isVisible: true },
  { id: 'shared-portfolios', label: 'SHARED PORTFOLIOS', route: '/shared-portfolios', isVisible: false },
  { id: 'companies', label: 'COMPANIES', route: '/companies', isVisible: true },
  { id: 'my-company', label: 'MY COMPANY', route: '/companies', isVisible: false },
  { id: 'my-datasets', label: 'MY DATASETS', route: '/datasets', isVisible: true },
  { id: 'qa', label: 'QA', route: '/qualityassurance', isVisible: false },
  { id: 'my-data-requests', label: 'MY DATA REQUESTS', route: '/requests', isVisible: true },
  { id: 'my-data-requests-legacy', label: 'MY DATA REQUESTS LEGACY', route: '/requests-legacy', isVisible: true },
  {
    id: 'data-requests-for-my-companies',
    label: 'DATA REQUESTS FOR MY COMPANIES',
    route: '/companyrequests',
    isVisible: false,
  },
  { id: 'all-data-requests', label: 'ALL DATA REQUESTS', route: '/requestoverview', isVisible: false },
  {
    id: 'all-data-requests-legacy',
    label: 'ALL DATA REQUESTS LEGACY',
    route: '/requestoverview-legacy',
    isVisible: false,
  },
]);

const visibleTabs = computed(() => tabs.value.filter((tab) => tab.isVisible || tab.id === currentTabId.value));

const currentTabId = computed<TabInfo['id']>(() => {
  const myCompanyId = companyRoleAssignments.value?.[0]?.companyId;
  if (myCompanyId && route.path.includes(`/companies/${myCompanyId}`)) {
    return 'my-company';
  }

  return (route.meta.initialTabId as TabInfo['id']) ?? '';
});

onMounted(() => {
  setVisibilityForSharedPortfoliosTab();
  setVisibilityForTabWithQualityAssurance();
  configureCompanyRelatedTabs();
  setVisibilityForAdminTab();
});

watch(companyRoleAssignments, () => {
  configureCompanyRelatedTabs();
});

/**
 * Handles the tab change event.
 */
function onTabChange(newTab: string | number): void {
  const newId = String(newTab);
  const tab = getTabById(newId);
  router.push(tab.route).catch((err) => {
    console.error('Navigation error when changing tabs:', err);
  });
}

/**
 * Gets a tab by its ID.
 */
function getTabById(tabId: TabInfo['id']): TabInfo {
  return assertDefined(tabs.value.find((tab) => tab.id === tabId));
}

/**
 * Sets the visibility of the tab for Shared Portfolios.
 * If the user does have any shared portfolios, it is shown. Else it stays invisible.
 */
function setVisibilityForSharedPortfoliosTab(): void {
  apiClientProvider.apiClients.portfolioController
    .getAllSharedPortfolioNamesForCurrentUser()
    .then((sharedPortfolioNames) => {
      getTabById('shared-portfolios').isVisible = sharedPortfolioNames && sharedPortfolioNames.data.length > 0;
    })
    .catch((error) => console.log(error));
}

/**
 * Sets the visibility of the tab for Quality Assurance.
 * If the user does have the Keycloak-role "Reviewer", it is shown. Else it stays invisible.
 */
function setVisibilityForTabWithQualityAssurance(): void {
  checkIfUserHasRole(KEYCLOAK_ROLE_REVIEWER, getKeycloakPromise)
    .then((hasUserReviewerRights) => {
      getTabById('qa').isVisible = hasUserReviewerRights;
    })
    .catch((error) => console.log(error));
}

/**
 * Configures company-related tabs based on the current user's company role assignments.
 * - Shows and sets the route for the "My Company" tab when a company assignment exists.
 * - Shows the "Data requests for my companies" tab if the user is a company owner.
 */
function configureCompanyRelatedTabs(): void {
  const myCompanyTab = getTabById('my-company');
  const requestsForMyCompaniesTab = getTabById('data-requests-for-my-companies');

  const assignments = companyRoleAssignments.value ?? [];
  const firstAssignment = assignments[0];

  if (!firstAssignment) {
    myCompanyTab.isVisible = false;
    myCompanyTab.route = '/companies';
    requestsForMyCompaniesTab.isVisible = false;
    return;
  }

  myCompanyTab.isVisible = true;
  myCompanyTab.route = `/companies/${firstAssignment.companyId}`;

  requestsForMyCompaniesTab.isVisible = assignments.some(
    (roleAssignment) => roleAssignment.companyRole === CompanyRole.CompanyOwner
  );
}

/**
 * Sets the visibility of the all data requests tab.
 * Only Admins can see the tab.
 */
function setVisibilityForAdminTab(): void {
  checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise)
    .then((hasUserAdminRights) => {
      getTabById('all-data-requests').isVisible = hasUserAdminRights;
      getTabById('all-data-requests-legacy').isVisible = hasUserAdminRights;
    })
    .catch((error) => console.log(error));
}
</script>
