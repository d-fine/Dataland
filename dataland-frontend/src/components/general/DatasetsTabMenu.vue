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
import { useGetExtendedCompanyRoleAssignmentsQuery } from '@/api-queries/community-manager/company-roles/useGetExtendedCompanyRoleAssignmentsQuery';
import { useGetAllSharedPortfolioNamesForCurrentUserQuery } from '@/api-queries/user-service/portfolio/useGetAllSharedPortfolioNamesForCurrentUserQuery';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_JUDGE } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import type Keycloak from 'keycloak-js';
import Tab from 'primevue/tab';
import TabList from 'primevue/tablist';
import TabPanel from 'primevue/tabpanel';
import TabPanels from 'primevue/tabpanels';
import Tabs from 'primevue/tabs';
import { inject, onMounted, ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import router from '@/router';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';

interface TabInfo {
  id: string;
  label: string;
  route: string;
  isVisible: boolean;
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const route = useRoute();

const currentUserId = ref<string | undefined>(undefined);
const isUserJudge = ref(false);
const isUserAdmin = ref(false);

const { data: sharedPortfolioNames } = useGetAllSharedPortfolioNamesForCurrentUserQuery({
  enabled: computed(() => !!currentUserId.value),
});
const { data: currentUserRoleAssignments } = useGetExtendedCompanyRoleAssignmentsQuery({ userId: currentUserId });

const myCompanyId = computed(() => currentUserRoleAssignments.value?.[0]?.companyId);

const tabs = computed<Array<TabInfo>>(() => [
  { id: 'my-portfolios', label: 'MY PORTFOLIOS', route: '/portfolios', isVisible: true },
  {
    id: 'shared-portfolios',
    label: 'SHARED PORTFOLIOS',
    route: '/shared-portfolios',
    isVisible: (sharedPortfolioNames.value?.length ?? 0) > 0,
  },
  { id: 'companies', label: 'COMPANIES', route: '/companies', isVisible: true },
  {
    id: 'my-company',
    label: 'MY COMPANY',
    route: myCompanyId.value ? `/companies/${myCompanyId.value}` : '/companies',
    isVisible: !!myCompanyId.value,
  },
  { id: 'my-datasets', label: 'MY DATASETS', route: '/datasets', isVisible: true },
  { id: 'qa', label: 'QA', route: '/qualityassurance', isVisible: isUserJudge.value },
  { id: 'my-data-requests', label: 'MY DATA REQUESTS', route: '/requests', isVisible: true },
  { id: 'all-data-requests', label: 'ALL DATA REQUESTS', route: '/requestoverview', isVisible: isUserAdmin.value },
]);

const visibleTabs = computed(() => tabs.value.filter((tab) => tab.isVisible || tab.id === currentTabId.value));

const currentTabId = computed<TabInfo['id']>(() => {
  if (myCompanyId.value && route.path.includes(`/companies/${myCompanyId.value}`)) {
    return 'my-company';
  }

  return (route.meta.initialTabId as TabInfo['id']) ?? '';
});

onMounted(() => {
  resolveCurrentUser().catch((error) => console.log(error));
});

/**
 * Resolves the id and the Keycloak roles of the logged-in user, which enable the queries and role-based tabs.
 */
async function resolveCurrentUser(): Promise<void> {
  const keycloak = await assertDefined(getKeycloakPromise)();
  if (!keycloak.authenticated) return;
  currentUserId.value = keycloak.idTokenParsed?.sub;
  [isUserJudge.value, isUserAdmin.value] = await Promise.all([
    checkIfUserHasRole(KEYCLOAK_ROLE_JUDGE, getKeycloakPromise),
    checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise),
  ]);
}

/**
 * Handles the tab change event.
 */
function onTabChange(newTab: string | number): void {
  const tab = assertDefined(tabs.value.find((tab) => tab.id === String(newTab)));
  router.push(tab.route).catch((err) => {
    console.error('Navigation error when changing tabs:', err);
  });
}
</script>
