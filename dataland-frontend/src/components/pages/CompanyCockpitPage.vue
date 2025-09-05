<template>
  <TheHeader v-if="!useMobileView" />
  <TheContent class="flex">
    <CompanyInfoSheet :company-id="companyId" :show-single-data-request-button="true" />
    <Tabs v-model:value="activeTab">
      <TabList
        v-if="isCompanyMemberOrAdmin"
        :pt="{
          tabList: {
            style: 'display: flex; justify-content: center;',
          },
        }"
      >
        <Tab value="datasets" data-test="datasetsTab">Datasets</Tab>
        <Tab value="users" data-test="usersTab">Users</Tab>
      </TabList>
      <TabPanels>
        <TabPanel value="datasets">
          <CompanyDatasetsPane :company-id="companyId" />
        </TabPanel>
        <TabPanel
          v-if="isCompanyMemberOrAdmin"
          value="users"
          style="background-color: var(--p-surface-50); padding: var(--spacing-xs)"
        >
          <CompanyRolesCard
            v-for="role in roles"
            :key="`${String(role)}-${refreshAllCards}`"
            :companyId="companyId"
            :role="role"
            :userRole="userRole"
            @users-changed="handleUsersChanged"
          />
        </TabPanel>
      </TabPanels>
    </Tabs>
    <Dialog v-model:visible="showSuccess" header="Success" :modal="true">
      <div style="text-align: center; padding: 8px 0">
        <i class="pi pi-check-circle" style="font-size: 2rem; color: var(--p-green-500)"></i>
        <div style="margin-top: 8px">{{ successMessage }}</div>
      </div>
      <template #footer>
        <Button label="OK" @click="showSuccess = false" />
      </template>
    </Dialog>
  </TheContent>
  <TheFooter />
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, inject } from 'vue';
import type { Ref } from 'vue';
import { useRouter } from 'vue-router';

import TheHeader from '@/components/generics/TheHeader.vue';
import TheContent from '@/components/generics/TheContent.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import CompanyInfoSheet from '@/components/general/CompanyInfoSheet.vue';
import CompanyDatasetsPane from '@/components/resources/companyCockpit/CompanyDatasetsPane.vue';
import CompanyRolesCard from '@/components/resources/companyCockpit/CompanyRolesCard.vue';

import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';

import { hasCompanyAtLeastOneCompanyOwner } from '@/utils/CompanyRolesUtils';
import { KEYCLOAK_ROLE_UPLOADER, KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils';
import { CompanyRole, type CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { DocumentMetaInfoDocumentCategoryEnum, type DocumentMetaInfoResponse } from '@clients/documentmanager';
import type Keycloak from 'keycloak-js';

const props = defineProps<{ companyId: string }>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const companyRoleAssignmentsRef = inject<Ref<CompanyRoleAssignmentExtended[] | undefined>>(
  'companyRoleAssignments',
  ref([])
);
const useMobileView = inject<Ref<boolean>>('useMobileView', ref(false));
const router = useRouter();

const activeTab = ref<'datasets' | 'users'>('datasets');
const isCompanyMemberOrAdmin = ref(false);
const isUserCompanyOwnerOrUploader = ref(false);
const isUserKeycloakUploader = ref(false);
const isAnyCompanyOwnerExisting = ref(false);
const isUserCompanyMember = ref(false);
const isUserDatalandAdmin = ref(false);
const userRole = ref<CompanyRole | null>(null);

const latestDocuments = reactive<Record<string, DocumentMetaInfoResponse[]>>({});
Object.values(DocumentMetaInfoDocumentCategoryEnum).forEach((category) => {
  latestDocuments[`latest${category}`] = [];
});

const roles = Object.values(CompanyRole);

/** Incrementing this value will force re-rendering of all CompanyRolesCard components. */
const refreshAllCards = ref(0);

const showSuccess = ref(false);
const successMessage = ref('');

/**
 * Handler for user changes in company roles.
 */
function handleUsersChanged(message?: string): void {
  successMessage.value = message ?? 'Changes saved.';
  showSuccess.value = true;
  refreshAllCards.value++;
}

/**
 * Sets user rights and roles for the current company.
 * Updates isAnyCompanyOwnerExisting, isUserCompanyOwnerOrUploader, and isUserKeycloakUploader.
 */
async function setUserRights(): Promise<void> {
  isAnyCompanyOwnerExisting.value = await hasCompanyAtLeastOneCompanyOwner(props.companyId, getKeycloakPromise);

  userRole.value =
    companyRoleAssignmentsRef.value?.find((assignment) => assignment.companyId === props.companyId)?.companyRole ||
    null;
  isUserCompanyOwnerOrUploader.value =
    userRole.value === CompanyRole.CompanyOwner || userRole.value === CompanyRole.DataUploader;
  isUserKeycloakUploader.value = await checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, getKeycloakPromise);
  isUserCompanyMember.value = userRole.value !== null;
  isUserDatalandAdmin.value = await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise);
  isCompanyMemberOrAdmin.value = isUserCompanyMember.value || isUserDatalandAdmin.value;
}

watch(
  () => props.companyId,
  async (newId, oldId) => {
    if (newId == oldId) return;
    await setUserRights();
  }
);

watch(activeTab, (val) => {
  const base = `/companies/${props.companyId}`;
  void router.replace({ path: val === 'users' ? `${base}/users` : base });
});

onMounted(async () => {
  await setUserRights();
  const path = router.currentRoute.value.path;
  if (path.endsWith('/users') && !isCompanyMemberOrAdmin.value) {
    activeTab.value = 'datasets';
    await router.replace({ path: `/companies/${props.companyId}` });
  } else {
    activeTab.value = path.endsWith('/users') ? 'users' : 'datasets';
  }
});
</script>

<style lang="scss" scoped>
.card-container {
  display: flex;
  gap: var(--spacing-xxxl);
  padding: var(--spacing-xl) var(--spacing-xxl);
  background-color: var(--p-surface-50);
  align-items: flex-start;

  > .left-pane {
    flex: 0 0 30%;
  }

  > .right-pane {
    flex: 1 1 0;
    display: flex;
    flex-direction: column;
    gap: var(--spacing-md);
  }

  @media (max-width: 768px) {
    flex-direction: column;

    > .left-pane,
    > .right-pane {
      flex: none;
      width: 100%;
    }
  }
}

.claim-pane {
  margin-bottom: var(--spacing-md);
}

.frameworks-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-xxl);

  /* collapse to one column on smaller screens */
  @media (max-width: 1024px) {
    grid-template-columns: repeat(1, 1fr);
  }
}

.card {
  &__separator {
    width: 100%;
    border-bottom: #e0dfde solid 1px;
    margin-top: 8px;
    margin-bottom: 24px;
  }

  &__subtitle {
    font-size: 16px;
    font-weight: 700;
    line-height: 21px;
    margin-top: 8px;
  }
}

.d-letters {
  letter-spacing: 0.05em;
}

.text-primary {
  color: var(--main-color);
}
</style>
