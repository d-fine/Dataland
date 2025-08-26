<template>
  <Card
    data-test="company-roles-card"
    style="width: 70%; margin: 0 auto; margin-top: var(--spacing-xl); margin-bottom: var(--spacing-xl)"
  >
    <template #title>
      <i :class="group?.icon" />
      {{ group?.title }}
      <Button
        v-if="!showInfoMessage"
        icon="pi pi-info-circle"
        variant="text"
        rounded
        @click="showInfoBox"
        title="Show Info"
      />
      <Button
        v-if="roleHasUsers && allowedToEditRoles"
        icon="pi pi-plus"
        variant="text"
        :label="`ADD ${group?.title.toUpperCase()}`"
        @click="openAddUserDialog"
        style="float: right"
      />
    </template>

    <template #subtitle>
      <Message
        v-if="showInfoMessage"
        severity="info"
        :closable="true"
        @close="hideInfoBox"
        style="margin-top: var(--spacing-xs)"
      >
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
        <Column field="email" header="Email" :style="{ width: emailColumnWidth }" />
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
        v-else-if="allowedToEditRoles"
        icon="pi pi-plus"
        variant="text"
        :label="`ADD ${group?.title.toUpperCase()}`"
        @click="openAddUserDialog"
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
      :options="groups"
      optionLabel="title"
      optionValue="role"
      :optionDisabled="isOptionDisabled"
      style="margin-top: var(--spacing-md)"
      :pt="{
        listContainer: {
          style: 'max-height: unset;',
        },
      }"
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
      <Button label="Change Role" :disabled="selectedRole === null" @click="confirmChangeRole" />
    </template>
  </Dialog>

  <!-- Remove User Modal -->
  <Dialog v-model:visible="showRemoveUserDialog" :header="`Remove Company Role`" :modal="true" :closable="true">
    <span>You're about to remove the user:</span><br />
    <span style="font-weight: var(--font-weight-medium)">{{ roleTargetText }}</span>
    <template #footer>
      <Button label="Remove User" @click="confirmRemoveUser" />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, defineProps, inject, onMounted, ref, watch } from 'vue';

import Button from 'primevue/button';
import Card from 'primevue/card';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import Dialog from 'primevue/dialog';
import Listbox from 'primevue/listbox';
import Menu from 'primevue/menu';
import Message from 'primevue/message';
import { useDialog } from 'primevue/usedialog';

import type Keycloak from 'keycloak-js';
import type { CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { CompanyRole } from '@clients/communitymanager';

import AddUserDialog from '@/components/resources/companyCockpit/AddUserDialog.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { checkIfUserHasRole } from '@/utils/KeycloakUtils.ts';
import { KEYCLOAK_ROLE_ADMIN } from '@/utils/KeycloakRoles.ts';
import { useStorage } from '@vueuse/core';

type Group = {
  role: CompanyRole;
  title: string;
  icon: string;
  info: string;
  description: string;
};

type TableRow = { userId: string; email: string; firstName: string; lastName: string };
type RowMenuRef = InstanceType<typeof Menu> | null;
type MenuItem = { label: string; command: () => void };

const props = defineProps<{
  companyId: string;
  role: CompanyRole;
  userRole?: CompanyRole | null;
}>();

const dialog = useDialog();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const companyUserInformation = ref<CompanyRoleAssignmentExtended[]>([]);

const showChangeRoleDialog = ref(false);
const showRemoveUserDialog = ref(false);

const selectedUser = ref<TableRow | null>(null);
const selectedRole = ref<CompanyRole | null>(null);

const rowMenus = ref<Record<string, RowMenuRef>>({});
const isGlobalAdmin = ref(false);
const showInfoMessage = useStorage<boolean>(`showInfoMessage-${props.role}`, true);

const groups: Group[] = [
  {
    role: CompanyRole.MemberAdmin,
    title: 'Admins',
    icon: 'pi pi-shield',
    info: 'The User Admin has the rights to add or remove other user admins and members. Admins manage other users and can control who has access to what data or features within Dataland.',
    description: 'Manage users and roles.',
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
];

const group = computed(() => groups.find((g) => g.role === props.role));

const rowsForRole = computed<TableRow[]>(() =>
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

const allowedToEditRoles = computed(
  () => isGlobalAdmin.value || props.userRole === CompanyRole.CompanyOwner || props.userRole === CompanyRole.MemberAdmin
);

const emailColumnWidth = computed(() => (allowedToEditRoles.value ? '25%' : '30%'));

const roleTargetText = computed(() => {
  const u = selectedUser.value;
  if (!u) return '';
  const first = u.firstName?.trim();
  const last = u.lastName?.trim();
  return first && last ? `${first} ${last}, ${u.email}` : (u.email ?? '');
});

/**
 * Determines if a role option should be disabled in the role selection list.
 * @param opt - The role option to check, containing a CompanyRole.
 */
function isOptionDisabled(opt: { role: CompanyRole }): boolean {
  return (
    opt.role === props.role ||
    (props.userRole !== CompanyRole.CompanyOwner && !isGlobalAdmin.value && opt.role === CompanyRole.CompanyOwner)
  );
}

/**
 * Hides the info box
 */
function hideInfoBox(): void {
  showInfoMessage.value = false;
}

/**
 * Shows the info box
 */
function showInfoBox(): void {
  showInfoMessage.value = true;
}

/**
 * Sets the Menu ref for a specific row by user ID.
 */
function setRowMenuRef(id: string, el: RowMenuRef): void {
  if (el) rowMenus.value[id] = el;
}

/**
 * Toggles the row menu for a specific user.
 */
function toggleRowMenu(event: MouseEvent, id: string): void {
  rowMenus.value[id]?.toggle(event);
}

/**
 * Returns the menu items for a given table row.
 * @returns An array of menu items.
 */
function menuItemsFor(row: TableRow): MenuItem[] {
  return [
    {
      label: 'Change User’s Role',
      command: (): void => {
        selectedUser.value = row;
        selectedRole.value = null; // preselect current role
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
 * Fetches the company user information for the current role and company.
 */
async function getCompanyUserInformation(): Promise<void> {
  if (!props.companyId) return;
  try {
    const api = apiClientProvider.apiClients.companyRolesController;
    const res = await api.getExtendedCompanyRoleAssignments(props.role, props.companyId, undefined);
    companyUserInformation.value = res.data;
  } catch (error) {
    console.error('Failed to load company users:', error);
  }
}

/**
 * Confirms and processes the role change for the selected user.
 */
async function confirmChangeRole(): Promise<void> {
  if (!selectedUser.value || selectedRole.value == null) return;
  try {
    const api = apiClientProvider.apiClients.companyRolesController;
    await api.assignCompanyRole(selectedRole.value, props.companyId, selectedUser.value.userId);
    showChangeRoleDialog.value = false;
    await getCompanyUserInformation();
  } catch (err) {
    console.error('Failed to change role:', err);
  }
}

/**
 * Confirms and processes the removal of the selected user from the role.
 */
async function confirmRemoveUser(): Promise<void> {
  if (!selectedUser.value) return;
  try {
    const api = apiClientProvider.apiClients.companyRolesController;
    await api.removeCompanyRole(props.role, props.companyId, selectedUser.value.userId);
    showRemoveUserDialog.value = false;
    await getCompanyUserInformation();
  } catch (err) {
    console.error('Failed to remove user:', err);
  }
}

const emit = defineEmits<{ (e: 'users-changed'): void }>();

/**
 * Opens the Add User dialog for the current role and company.
 */
function openAddUserDialog(): void {
  dialog.open(AddUserDialog, {
    props: {
      modal: true,
      header: `Add ${group.value?.title}`,
      pt: {
        root: {
          style: {
            backgroundColor: 'var(--p-surface-50)',
          },
        },
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
    onClose: () => {
      emit('users-changed');
    },
  });
}

watch([(): string => props.companyId, (): CompanyRole => props.role], async function () {
  await getCompanyUserInformation();
});

onMounted(async () => {
  isGlobalAdmin.value = await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise);
  await getCompanyUserInformation();
});
</script>
