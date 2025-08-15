<template>
  <Card
    data-test="company-roles-card"
    :pt="{
      root: {
        style: 'width: 70%; margin: 0 auto; margin-top: var(--spacing-xl); margin-bottom: var(--spacing-xl);',
      },
    }"
  >
    <template #title>
      <i :class="group?.icon" />
      {{ group?.title }}
      <Button
        v-if="roleHasUsers"
        icon="pi pi-plus"
        variant="text"
        :label="`Add ${group?.title}`"
        @click="showAddUserDialog = true"
        :pt="{
          root: {
            style: 'float:right;',
          },
        }"
      />
    </template>

    <template #subtitle>
      <Message
        severity="info"
        closable
        :pt="{
          root: {
            style: 'margin-top: var(--spacing-xs);',
          },
        }"
      >
        {{ group?.info }}
      </Message>
    </template>

    <template #content>
      <DataTable v-if="roleHasUsers" :value="rowsForRole" tableStyle="min-width: 50rem;">
        <Column field="firstName" header="First Name" style="width: 20%" />
        <Column field="lastName" header="Last Name" style="width: 20%" />
        <Column field="email" header="Email" style="width: 25%" />
        <Column field="userId" header="User ID" style="width: 35%" />
      </DataTable>
      <Button
        v-else
        icon="pi pi-plus"
        variant="text"
        :label="`Add ${group?.title}`"
        @click="showAddUserDialog = true"
        :pt="{
          root: {
            style: 'display:flex; margin: var(--spacing-xs) auto 0;',
          },
        }"
      />
    </template>
  </Card>
</template>

<script setup lang="ts">
import { defineProps, ref, computed, onMounted, inject } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';

import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import type { CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { type CompanyRole } from '@clients/communitymanager';

import Card from 'primevue/card';
import Message from 'primevue/message';

const props = defineProps<{
  companyId: string;
  group: {
    role: CompanyRole;
    title: string;
    icon: string;
    info: string;
  };
}>();

// Injected dependencies
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const companyUserInformation = ref<CompanyRoleAssignmentExtended[]>([]);
const showAddUserDialog = ref(false);

const rowsForRole = computed(() =>
  companyUserInformation.value
    .filter((u) => u.companyId === props.companyId && u.companyRole === props.group.role)
    .map((u) => ({
      firstName: u.firstName ?? '',
      lastName: u.lastName ?? '',
      email: u.email,
      userId: u.userId,
    }))
);

const roleHasUsers = computed(() => rowsForRole.value.length > 0);

/**
 * Uses the dataland API to retrieve information about the company users identified by the local
 * companyId object.
 */
async function getCompanyUserInformation(): Promise<void> {
  if (!props.companyId) return;
  try {
    const companyRolesControllerApi = apiClientProvider.apiClients.companyRolesController;
    const data = await companyRolesControllerApi.getExtendedCompanyRoleAssignments(
      props.group.role,
      props.companyId,
      undefined
    );
    companyUserInformation.value = data.data;
  } catch (error) {
    console.error('Failed to load company users:', error);
  }
}

onMounted(async () => {
  await getCompanyUserInformation();
});
</script>
