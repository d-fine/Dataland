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
import { inject, onMounted, ref, type Ref, watchEffect } from 'vue';
import router from '@/router';

interface TabInfo {
  label: string;
  route: string;
  isVisible: boolean;
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const currentTabIndex = ref<number>(0);

// Ref is needed since App.vue is written in the Options API and we need to use the Composition API here.
const companyRoleAssignments = inject<Ref<Array<CompanyRoleAssignmentExtended>>>('companyRoleAssignments');
const { initialTabIndex } = defineProps<{
  initialTabIndex: number;
}>();

const tabs = ref<Array<TabInfo>>([
  { label: 'COMPANIES', route: '/companies', isVisible: true },
  { label: 'MY DATASETS', route: '/datasets', isVisible: true },
  { label: 'MY PORTFOLIOS', route: '/portfolios', isVisible: true },
  { label: 'QA', route: '/qualityassurance', isVisible: false },
  { label: 'MY DATA REQUESTS', route: '/requests', isVisible: true },
  { label: 'DATA REQUESTS FOR MY COMPANIES', route: '/companyrequests', isVisible: false },
  { label: 'ALL DATA REQUESTS', route: '/requestoverview', isVisible: false },
]);

onMounted(() => {
  setVisibilityForTabWithQualityAssurance();
  setVisibilityForTabWithAccessRequestsForMyCompanies();
  setVisibilityForAdminTab();
  currentTabIndex.value = initialTabIndex ?? 0;
});

watchEffect(() => {
  setVisibilityForTabWithAccessRequestsForMyCompanies();
});

/**
 * Handles the tab change event.
 */
function onTabChange(newIndex: number | string): void {
  const route = tabs.value[newIndex as number].route;
  router.push(route).catch((err) => {
    console.error('Navigation error when changing tabs:', err);
  });
}

/**
 * Sets the visibility of the tab for Quality Assurance.
 * If the user does have the Keycloak-role "Reviewer", it is shown. Else it stays invisible.
 */
function setVisibilityForTabWithQualityAssurance(): void {
  checkIfUserHasRole(KEYCLOAK_ROLE_REVIEWER, getKeycloakPromise)
    .then((hasUserReviewerRights) => {
      tabs.value[3].isVisible = hasUserReviewerRights;
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
    tabs.value[5].isVisible = companyOwnershipAssignments.length > 0;
  }
}

/**
 * Sets the visibility of the all data requests tab.
 * Only Admins can see the tab.
 */
function setVisibilityForAdminTab(): void {
  checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise)
    .then((hasUserAdminRights) => {
      tabs.value[6].isVisible = hasUserAdminRights;
    })
    .catch((error) => console.log(error));
}
</script>
