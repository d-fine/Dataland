<template>
  <TheContent class="flex" data-test="company-cockpit-root">
    <CompanyInfoSheet :company-id="companyId" :show-single-data-request-button="true" />
    <Tabs v-model:value="activeTab">
      <TabList
        v-if="isUserCompanyMemberOrAdmin"
        :pt="{
          tabList: {
            style: 'display: flex; justify-content: center;',
          },
        }"
      >
        <Tab value="datasets" data-test="datasetsTab">Datasets</Tab>
        <Tab value="users" data-test="usersTab">Users</Tab>
        <Tab value="credits" v-if="canViewCredits" data-test="creditsTab">Credits</Tab>
      </TabList>
      <TabPanels>
        <TabPanel value="datasets">
          <CompanyDatasetsPane :company-id="companyId" />
        </TabPanel>
        <TabPanel v-if="isUserCompanyMemberOrAdmin" value="users">
          <div class="tab-layout">
            <CompanyRolesCard
              v-for="role in roles"
              :key="role"
              :companyId="companyId"
              :role="role"
              :userRole="userRole"
              @users-changed="showSuccess = true"
            />
          </div>
        </TabPanel>
        <TabPanel v-if="canViewCredits" value="credits">
          <div class="tab-layout">
            <CreditsCard :companyId="companyId" />
          </div>
        </TabPanel>
      </TabPanels>
    </Tabs>
    <SuccessDialog :visible="showSuccess" message="Changes successfully saved." @close="showSuccess = false" />
  </TheContent>
</template>
<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { whenever } from '@vueuse/core';

import TheContent from '@/components/generics/TheContent.vue';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import CompanyDatasetsPane from '@/components/resources/companyCockpit/CompanyDatasetsPane.vue';
import CompanyRolesCard from '@/components/resources/companyCockpit/CompanyRolesCard.vue';
import CreditsCard from '@/components/resources/companyCockpit/CreditsCard.vue';
import SuccessDialog from '@/components/general/SuccessDialog.vue';

import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';

import { useGetExtendedCompanyRoleAssignmentsQuery } from '@/api-queries/community-manager/company-roles/useGetExtendedCompanyRoleAssignmentsQuery';
import { useGetCompanyRightsQuery } from '@/api-queries/community-manager/company-rights/useGetCompanyRightsQuery';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { CompanyRole } from '@clients/communitymanager';
import type Keycloak from 'keycloak-js';

const props = defineProps<{ companyId: string }>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const router = useRouter();

const roles = Object.values(CompanyRole);
const activeTab = ref<'datasets' | 'users' | 'credits'>('datasets');
const showSuccess = ref(false);

const isKeycloakResolved = ref(false);
const currentUserId = ref<string | undefined>(undefined);
const isUserDatalandAdmin = ref(false);

const { data: currentUserRoleAssignments, isFetched: areRoleAssignmentsFetched } =
  useGetExtendedCompanyRoleAssignmentsQuery({ userId: currentUserId });
const { data: companyRights, isFetched: areCompanyRightsFetched } = useGetCompanyRightsQuery({
  companyId: () => props.companyId,
});

const userRole = computed<CompanyRole | null>(
  () =>
    currentUserRoleAssignments.value?.find((assignment) => assignment.companyId === props.companyId)?.companyRole ??
    null
);
const isUserCompanyMember = computed(() => userRole.value !== null);
const isUserCompanyMemberOrAdmin = computed(() => isUserCompanyMember.value || isUserDatalandAdmin.value);
const isCompanyDatalandMember = computed(() => companyRights.value?.includes('Member') ?? false);
const canViewCredits = computed(
  () => isUserDatalandAdmin.value || (isUserCompanyMember.value && isCompanyDatalandMember.value)
);

const areRightsLoaded = computed(
  () =>
    isKeycloakResolved.value &&
    (!currentUserId.value || areRoleAssignmentsFetched.value) &&
    areCompanyRightsFetched.value
);

/**
 * Resolves the id and the Dataland admin role of the logged-in user from Keycloak.
 */
async function resolveCurrentUser(): Promise<void> {
  const keycloak = await assertDefined(getKeycloakPromise)();
  if (keycloak.authenticated) {
    currentUserId.value = keycloak.idTokenParsed?.sub;
    isUserDatalandAdmin.value = await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise);
  }
  isKeycloakResolved.value = true;
}

/**
 * Opens the tab matching the current route once the rights of the user are known.
 * Redirects to the datasets tab if the user is not allowed to see the requested tab.
 */
function selectInitialTab(): void {
  const path = router.currentRoute.value.path;
  const onCreditsTab = path.endsWith('/credits');
  const onUsersTab = path.endsWith('/users');

  if ((!canViewCredits.value && onCreditsTab) || (!isUserCompanyMemberOrAdmin.value && onUsersTab)) {
    activeTab.value = 'datasets';
    void router.replace({ path: `/companies/${props.companyId}` });
  } else if (onCreditsTab) {
    activeTab.value = 'credits';
  } else if (onUsersTab) {
    activeTab.value = 'users';
  } else {
    activeTab.value = 'datasets';
  }
}

whenever(areRightsLoaded, selectInitialTab, { once: true });

watch(activeTab, async (val) => {
  const base = `/companies/${props.companyId}`;
  try {
    if (val === 'users') {
      await router.replace({ path: `${base}/users` });
    } else if (val === 'credits') {
      await router.replace({ path: `${base}/credits` });
    } else {
      await router.replace({ path: base });
    }
  } catch (err) {
    console.error('Navigation failed', err);
  }
});

onMounted(() => {
  void resolveCurrentUser();
});
</script>

<style lang="scss" scoped>
.tab-layout {
  background-color: var(--p-surface-50);
  padding: var(--spacing-xs);
}
</style>
