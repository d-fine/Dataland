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
  { label: 'MY PORTFOLIOS', route: '/portfolios', isVisible: true },
  { label: 'SHARED PORTFOLIOS', route: '/shared-portfolios', isVisible: false },
  { label: 'COMPANIES', route: '/companies', isVisible: true },
  { label: 'MY DATASETS', route: '/datasets', isVisible: true },
  { label: 'QA', route: '/qualityassurance', isVisible: false },
  { label: 'MY DATA REQUESTS', route: '/requests', isVisible: true },
  { label: 'MY DATA REQUESTS LEGACY', route: '/requests-legacy', isVisible: true },
  { label: 'DATA REQUESTS FOR MY COMPANIES', route: '/companyrequests', isVisible: false },
  { label: 'ALL DATA REQUESTS', route: '/requestoverview', isVisible: false },
  { label: 'ALL DATA REQUESTS LEGACY', route: '/requestoverview-legacy', isVisible: false },
]);

const currentTabIndex = computed(() => {
  return (route.meta.initialTabIndex as number) ?? -1;
});

onMounted(() => {
  void setVisibilityForSharedPortfoliosTab();
  setVisibilityForTabWithQualityAssurance();
  setVisibilityForTabWithAccessRequestsForMyCompanies();
  setVisibilityForAdminTab();
});

watchEffect(() => {
  setVisibilityForTabWithAccessRequestsForMyCompanies();
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
 * Sets the visibility of the tab for Shared Portfolios.
 * If the user does have any shared portfolios, it is shown. Else it stays invisible.
 */
async function setVisibilityForSharedPortfoliosTab(): Promise<void> {
  try {
    const sharedPortfolioNames =
      await apiClientProvider.apiClients.portfolioController.getAllSharedPortfolioNamesForCurrentUser();
    tabs.value[1]!.isVisible = sharedPortfolioNames && sharedPortfolioNames.data.length > 0;
  } catch (error) {
    console.log(error);
  }
}

/**
 * Sets the visibility of the tab for Quality Assurance.
 * If the user does have the Keycloak-role "Reviewer", it is shown. Else it stays invisible.
 */
function setVisibilityForTabWithQualityAssurance(): void {
  checkIfUserHasRole(KEYCLOAK_ROLE_REVIEWER, getKeycloakPromise)
    .then((hasUserReviewerRights) => {
      tabs.value[4]!.isVisible = hasUserReviewerRights;
    })
    .catch((error) => console.log(error));
}

/**
 * Sets the visibility of the tab for data access requests to companies of the current user.
 * If the user does have any company ownership, the tab is shown. Else it stays invisible.
 */
function setVisibilityForTabWithAccessRequestsForMyCompanies(): void {
  if (!companyRoleAssignments?.value?.length) return;
  const companyOwnershipAssignments = companyRoleAssignments.value.filter(
    (roleAssignment) => roleAssignment.companyRole == CompanyRole.CompanyOwner
  );
  if (companyOwnershipAssignments) {
    tabs.value[7]!.isVisible = companyOwnershipAssignments.length > 0;
  }
}

/**
 * Sets the visibility of the all data requests tab.
 * Only Admins can see the tab.
 */
function setVisibilityForAdminTab(): void {
  checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise)
    .then((hasUserAdminRights) => {
      tabs.value[8]!.isVisible = hasUserAdminRights;
      tabs.value[9]!.isVisible = hasUserAdminRights;
    })
    .catch((error) => console.log(error));
}
</script>
