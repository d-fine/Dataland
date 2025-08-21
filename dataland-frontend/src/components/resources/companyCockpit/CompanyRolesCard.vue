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
        <Column field="firstName" header="First Name" style="width: 20%" sortable />
        <Column field="lastName" header="Last Name" style="width: 20%" sortable />
        <Column field="email" header="Email" style="width: 25%" />
        <Column field="userId" header="User ID" style="width: 30%" />
        <Column :exportable="false" header="" style="width: 5%; text-align: right">
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
        :pt="{
          root: {
            style: 'display:flex; margin: var(--spacing-xs) auto 0;',
          },
        }"
      />
    </template>
  </Card>
  <Dialog v-model:visible="showChangeRoleDialog" header="Change User’s Role" :modal="true" :closable="true">
    <span>Change role for: {{ selectedUser?.firstName }} {{ selectedUser?.lastName }}, {{ selectedUser?.email }}</span>

    <!-- Role options -->
    <div class="space-y-2">
      <div
        v-for="opt in groups"
        :key="opt.role"
        class="p-3 border rounded-md flex items-start justify-between gap-3"
        :class="selectedRole === opt.role ? 'border-orange-400' : 'border-surface-300'"
        @click="selectedRole = opt.role"
        style="cursor: pointer"
        data-test="change-role-option"
      >
        <div class="flex items-start gap-3">
          <i :class="opt.icon" class="mt-1"></i>
          <div>
            <div class="font-medium">{{ opt.title }}</div>
            <div class="text-sm opacity-80">{{ opt.description }}</div>
          </div>
        </div>

        <RadioButton :inputId="`rb-${opt.role}`" name="role" :value="opt.role" v-model="selectedRole" />
      </div>
    </div>

    <!-- Footer actions -->
    <template #footer>
      <Button label="Back" variant="outlined" @click="showChangeRoleDialog = false" />
      <Button
        label="Change Role"
        :disabled="selectedRole === props.role || selectedRole === null"
        @click="confirmChangeRole"
      />
    </template>
  </Dialog>
  <Dialog
    v-model:visible="showRemoveUserDialog"
    :header="`Remove Company Role ${selectedUser?.firstName} ${selectedUser?.lastName}`"
    :modal="true"
    :closable="true"
  />
</template>

<script setup lang="ts">
import { defineProps, ref, computed, onMounted, inject } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Menu from 'primevue/menu';
import RadioButton from 'primevue/radiobutton';

import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import type { CompanyRoleAssignmentExtended } from '@clients/communitymanager';
import { CompanyRole } from '@clients/communitymanager';

import Card from 'primevue/card';
import Message from 'primevue/message';
import DownloadData from '@/components/general/DownloadData.vue';
import { useDialog } from 'primevue/usedialog';
import AddUserDialog from '@/components/resources/companyCockpit/AddUserDialog.vue';

type Group = {
  role: CompanyRole;
  title: string;
  icon: string;
  info: string;
  description: string;
};

const dialog = useDialog()

const props = defineProps<{
  companyId: string;
  role: CompanyRole;
}>();

// Injected dependencies
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const companyUserInformation = ref<CompanyRoleAssignmentExtended[]>([]);

const showAddUserDialog = ref(false);
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

const group = computed<Group | undefined>(() => groups.find((g) => g.role === props.role));

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
});


/**
 * Opens the PortfolioDownload with the current portfolio's data for downloading.
 * Once the dialog is closed, it reloads the portfolio data and shows the portfolio overview again.
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
  });
}

</script>
