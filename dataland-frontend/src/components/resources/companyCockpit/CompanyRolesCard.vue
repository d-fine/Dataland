<template>
  <Card
    data-test="company-roles-card"
    style="width: 70%; margin: 0 auto; margin-top: var(--spacing-xl); margin-bottom: var(--spacing-xl)"
  >
    <template #title>
      <i :class="group?.icon" />
      {{ group?.title }}
      <Button
        v-if="roleHasUsers"
        icon="pi pi-plus"
        variant="text"
        :label="`Add ${group?.title}`"
        @click="openAddUserDialog()"
        style="float: right"
      />
    </template>

    <template #subtitle>
      <Message severity="info" closable style="margin-top: var(--spacing-xs)">
        {{ group?.info }}
      </Message>
    </template>

    <template #content>
      <DataTable
        v-if="roleHasUsers"
        :key="allowedToEditRoles ? 'edit-on' : 'edit-off'"
        :value="rowsForRole"
        tableStyle="min-width: 50rem;"
      >
        <Column field="firstName" header="First Name" style="width: 20%" sortable />
        <Column field="lastName" header="Last Name" style="width: 20%" sortable />
        <Column field="email" header="Email" :style="{ width: allowedToEditRoles ? '25%' : '30%' }" />
        <Column field="userId" header="User ID" style="width: 30%" />
        <Column v-if="allowedToEditRoles" :exportable="false" header="" style="width: 5%; text-align: right">
          <template #body="slotProps">
            <Button
              icon="pi pi-ellipsis-v"
              variant="text"
              rounded
              @click="(e) => toggleRowMenu(e, slotProps.data.userId)"
            />
            <Menu
              :id="`row-menu-${slotProps.data.userId}`"
              :model="menuItemsFor(slotProps.data)"
              :popup="true"
              :ref="(el) => setRowMenuRef(slotProps.data.userId, el)"
            />
          </template>
        </Column>
      </DataTable>
      <Button
        v-else
        icon="pi pi-plus"
        variant="text"
        :label="`Add ${group?.title}`"
        @click="openAddUserDialog()"
        style="display: flex; margin: var(--spacing-xs) auto 0"
      />
    </template>
  </Card>

  <!-- Change Role Modal -->
  <Dialog
    v-model:visible="showChangeRoleDialog"
    v-if="allowedToEditRoles"
    header="Change User’s Role"
    :modal="true"
    :closable="true"
  >
    <span>
      Change role for:
      <span style="font-weight: var(--font-weight-medium)">
        {{ roleTargetText }}
      </span>
    </span>
    <Listbox
      v-model="selectedRole"
      :options="roleOptions"
      optionLabel="title"
      optionValue="role"
      style="margin-top: var(--spacing-md)"
    >
      <template #option="{ option }">
        <div style="display: flex; align-items: center; gap: 12px">
          <i :class="option.icon" style="font-size: 1.25rem; line-height: 1"></i>
          <div>
            <div style="font-weight: var(--font-weight-medium)">{{ option.title }}</div>
            <div style="font-size: var(--font-size-sm); opacity: 0.8">{{ option.description }}</div>
          </div>
        </div>
      </template>
    </Listbox>
    <template #footer>
      <Button label="Back" variant="outlined" @click="showChangeRoleDialog = false" />
      <Button
        label="Change Role"
        :disabled="selectedRole === props.role || selectedRole === null"
        @click="confirmChangeRole"
      />
    </template>
  </Dialog>

  <!-- Remove User Modal -->
  <Dialog
    v-model:visible="showRemoveUserDialog"
    :header="`Remove Company Role ${selectedUser?.firstName} ${selectedUser?.lastName}`"
    :modal="true"
    :closable="true"
  />
</template>

<script setup lang="ts">
import { defineProps, ref, computed, onMounted, inject, type Ref, unref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Menu from 'primevue/menu';
import Listbox from 'primevue/listbox';

import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import type { CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { CompanyRole } from '@clients/communitymanager';

import Card from 'primevue/card';
import Message from 'primevue/message';
import { useDialog } from 'primevue/usedialog';
import AddUserDialog from '@/components/resources/companyCockpit/AddUserDialog.vue';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils.ts';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles.ts';

type Group = {
  role: CompanyRole;
  title: string;
  icon: string;
  info: string;
  description: string;
};

const dialog = useDialog();

const props = defineProps<{
  companyId: string;
  role: CompanyRole;
  userRole?: CompanyRole;
}>();

// Injected dependencies
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const companyUserInformation = ref<CompanyRoleAssignmentExtended[]>([]);
const companyRoleAssignmentsRef = inject<Ref<CompanyRoleAssignmentExtended[] | undefined>>(
  'companyRoleAssignments',
  ref([])
);
const allowedToEditRoles = ref(false);

const showChangeRoleDialog = ref(false);
const showRemoveUserDialog = ref(false);

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

const roleHasUsers = computed(() => rowsForRole.value.length > 0);

type RowMenuRef = InstanceType<typeof Menu> | null;
const rowMenus = ref<Record<string, RowMenuRef>>({});

const selectedUser = ref<{ userId: string; email: string; firstName: string; lastName: string } | null>(null);
const selectedRole = ref<CompanyRole | null>(null);

const groups = [
  {
    role: CompanyRole.MemberAdmin,
    title: 'Admins',
    icon: 'pi pi-shield',
    info: 'The User Admin has the rights to add or remove other user admins and members. Admins manage other users and can control who has access to what data or features within Dataland.',
    description: 'NO TEXT YET!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!',
  },
  {
    role: CompanyRole.Member,
    title: 'Members',
    icon: 'pi pi-users',
    info: 'Members have the ability to request unlimited data. They are key users on Dataland, utilising the data available to make informed decisions or produce reports.',
    description: 'Unlimited access to data requests.',
  },
  {
    role: CompanyRole.CompanyOwner,
    title: 'Company Owners',
    icon: 'pi pi-crown',
    info: "Company owners have the highest level of access and can add other users as company owners. They are responsible for the governance of the company's profile on Dataland. The company owner is also the one accountable for the KYC process.",
    description: 'Highest authority with complete platform control.',
  },
  {
    role: CompanyRole.DataUploader,
    title: 'Uploaders',
    icon: 'pi pi-cloud-upload',
    info: 'Uploaders have the responsibility of ensuring all relevant data is uploaded to the platform for analysis and interpretation.',
    description: "Responsible for providing company's datasets.",
  },
] as const;

const roleTargetText = computed(() => {
  const u = selectedUser.value;
  if (!u) return '';
  const first = u.firstName?.trim();
  const last = u.lastName?.trim();
  if (first && last) return `${first} ${last}, ${u.email}`;
  return u.email ?? '';
});

const currentUserIsOwner = ref(false);
const group = computed<Group | undefined>(() => groups.find((g) => g.role === props.role));
// Hide owner entirely (filter) — or keep all and use optionDisabled (see below)
const roleOptions = computed(() =>
  currentUserIsOwner.value ? groups : groups.filter((o) => o.role !== CompanyRole.CompanyOwner)
);

/**
 * No content
 * @param id
 * @param el
 */
function setRowMenuRef(id: string, el: RowMenuRef): void {
  if (el) rowMenus.value[id] = el;
}

/**
 * No content
 * @param event
 * @param id
 */
function toggleRowMenu(event: MouseEvent, id: string): void {
  rowMenus.value[id]?.toggle(event);
}

/**
 * No content
 * @param row
 */
function menuItemsFor(row: {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
}): { label: string; command: () => void }[] {
  return [
    {
      label: 'Change User’s Role',
      command: (): void => {
        selectedUser.value = row;
        showChangeRoleDialog.value = true;
      },
    },
    {
      label: 'Remove User',
      command: (): void => {
        selectedUser.value = row;
        showRemoveUserDialog.value = true;
      },
    },
  ];
}

/**
 * No content
 */
async function confirmChangeRole(): Promise<void> {
  if (!selectedUser.value || selectedRole.value == null) return;
  try {
    // TODO: replace with your real API call
    // Example shape (adjust to your SDK):
    // await apiClientProvider.apiClients.companyRolesController.changeCompanyRole({
    //   companyId: props.companyId,
    //   userId: selectedUser.value.userId,
    //   newRole: selectedRole.value,
    // });

    // For now, just log and pretend success:
    console.log('Change role', {
      companyId: props.companyId,
      userId: selectedUser.value.userId,
      newRole: selectedRole.value,
    });

    showChangeRoleDialog.value = false;
    await getCompanyUserInformation(); // refresh table so the user moves to the other role group
  } catch (err) {
    console.error('Failed to change role:', err);
  }
}

/**
 * Uses the dataland API to retrieve information about the company users identified by the local
 * companyId object.
 */
async function getCompanyUserInformation(): Promise<void> {
  if (!props.companyId) return;
  try {
    const companyRolesControllerApi = apiClientProvider.apiClients.companyRolesController;
    const data = await companyRolesControllerApi.getExtendedCompanyRoleAssignments(
      props.role,
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
  const assignments = unref(companyRoleAssignmentsRef) ?? [];
  allowedToEditRoles.value =
    (await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise)) ||
    props.userRole.includes(CompanyRole.CompanyOwner) ||
    props.userRole.includes(CompanyRole.MemberAdmin);
  console.debug('Allowed to edit roles:', companyRoleAssignmentsRef.value, assignments, allowedToEditRoles.value);
});

/**
 * Opens the dialog to add a new user to the current company with the current role.
 */
function openAddUserDialog(): void {
  dialog.open(AddUserDialog, {
    props: {
      modal: true,
      header: 'Add Members',
      pt: {
        title: {
          style: {
            maxWidth: '15em',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          },
        },
      },
    },
    data: {
      companyId: props.companyId,
      role: props.role,
      existingUsers: rowsForRole.value,
    },
  });
}
</script>
