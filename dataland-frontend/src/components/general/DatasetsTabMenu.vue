<template>
  <Tabs :value="currentTabIndex" @update:value="onTabChange">
    <TabList>
      <Tab
        v-for="tab in tabs"
        :key="tab.label"
        :value="tabs.indexOf(tab)"
        :disabled="!(tabs.indexOf(tab) == currentTabIndex || (tab.isVisible ?? true))"
        :pt="{
          root: ({ props }) => {
            return {
              style: {
                display: props.disabled ? 'none' : '',
              },
            };
          },
        }"
      >
        {{ tab.label }}
      </Tab>
    </TabList>
    <TabPanels>
      <TabPanel
        v-for="tab in tabs"
        :key="tab.label"
        :value="tabs.indexOf(tab)"
        :disabled="!(tabs.indexOf(tab) == currentTabIndex || (tab.isVisible ?? true))"
      >
        <slot v-if="tabs.indexOf(tab) == currentTabIndex" />
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
import { inject, onMounted, ref, type Ref, computed, watchEffect } from 'vue';
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
const companyRoleAssignments = inject<Ref<Array<CompanyRoleAssignmentExtended>>>('companyRoleAssignments');

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

const currentTabIndex = computed(() => {
  return (route.meta.initialTabIndex as number) ?? -1;
});

onMounted(() => {
  setVisibilityForSharedPortfoliosTab();
  setVisibilityForTabWithQualityAssurance();
  configureCompanyRelatedTabs();
  setVisibilityForAdminTab();
});

watchEffect(() => {
  configureCompanyRelatedTabs();
});

/**
 * Handles the tab change event.
 */
function onTabChange(newIndex: number | string): void {
  const tab = tabs.value[newIndex as number];
  if (!tab) return;
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
  const firstAssignment = companyRoleAssignments?.value?.[0];
  const myCompanyTab = getTabById('my-company');

  if (firstAssignment) {
    myCompanyTab.isVisible = true;
    myCompanyTab.route = `/companies/${firstAssignment.companyId}`;
  } else {
    myCompanyTab.isVisible = false;
    myCompanyTab.route = `/companies`;
    return;
  }

  const companyOwnershipAssignments = companyRoleAssignments.value.filter(
    (roleAssignment) => roleAssignment.companyRole == CompanyRole.CompanyOwner
  );
  if (companyOwnershipAssignments) {
    getTabById('data-requests-for-my-companies').isVisible = companyOwnershipAssignments.length > 0;
  }
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
