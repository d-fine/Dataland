<template>
  <Card
    class="mb-4"
    :pt="{
      root: {
        style: 'width: 70%; margin: 0 auto; margin-top: 2rem; margin-bottom: 2rem;',
      },
    }"
  >
    <template #title>
      <i :class="group?.icon" />
      {{ group?.title }}
    </template>

    <template #subtitle>
      <Message severity="info" closable>
        {{ group?.info }}
      </Message>
    </template>

    <template #content>
      <DataTable :value="rowsForRole" tableStyle="min-width: 50rem;">
        <Column field="firstName" header="First Name" style="width: 20%" />
        <Column field="lastName" header="Last Name" style="width: 20%" />
        <Column field="email" header="Email" style="width: 25%" />
        <Column field="userId" header="User ID" style="width: 35%" />
      </DataTable>
    </template>
  </Card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { ref, computed, onMounted, inject } from 'vue';

import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import type { CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { CompanyRole } from '@clients/communitymanager';

import Card from 'primevue/card';
import Message from 'primevue/message';

const props = defineProps<{
  companyId: string;
  role: CompanyRole;
}>();

// Injected dependencies
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;

const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const companyUserInformation = ref<CompanyRoleAssignmentExtended[]>([]);

const roleGroups = [
  {
    role: CompanyRole.MemberAdmin,
    title: 'Admins',
    icon: 'pi pi-shield',
    info: 'The User Admin has the power to add or remove other user admins and members. Admins are managing other users and can control who has access to what data or features within the platform.',
  },
  {
    role: CompanyRole.Member,
    title: 'Members',
    icon: 'pi pi-users',
    info: 'Members have the ability to request unlimited data. They are key users of the platform, utilising the data available to make informed decisions or produce reports.',
  },
  {
    role: CompanyRole.CompanyOwner,
    title: 'Company Owners',
    icon: 'pi pi-crown',
    info: "Company onwers have the most power and can add other users as company owners. They are responsible for the governance of the company's profile on Dataland. The company owner is also the one accountable for the KYC process.",
  },
  {
    role: CompanyRole.DataUploader,
    title: 'Uploaders',
    icon: 'pi pi-cloud-upload',
    info: 'Uploaders are tasked with the provision of ESG data for the company. Uploaders have the responsibility of ensuring all relevant ESG data is uploaded to the platform for analysis and interpretation.',
  },
] as const;

const group = computed(() => roleGroups.find((g) => g.role === props.role));

const rowsForRole = computed(() =>
  companyUserInformation.value
    .filter((u) => u.companyId === props.companyId && u.companyRole === props.role)
    .map((u) => ({
      firstName: u.firstName ?? '',
      lastName: u.lastName ?? '',
      email: u.email,
      userId: u.userId,
    }))
);

/**
 * Uses the dataland API to retrieve information about the company users identified by the local
 * companyId object.
 */
async function getCompanyUserInformation(): Promise<void> {
  if (!props.companyId) return;
  try {
    const companyRolesControllerApi = apiClientProvider.apiClients.companyRolesController;
    const data = await companyRolesControllerApi.getExtendedCompanyRoleAssignments(
      undefined,
      props.companyId,
      undefined
    );
    companyUserInformation.value = data.data;
  } catch (error) {
    console.error(error);
  }
}

onMounted(async () => {
  await getCompanyUserInformation();
});
</script>
