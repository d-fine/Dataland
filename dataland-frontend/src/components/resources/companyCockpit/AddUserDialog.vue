<template>
  <div class="add-member-dialog">
    <div class="content-wrapper">
      <div class="search-section">
        <b>Add user by email address</b>
        <div class="search-container">
          <InputText
            v-model="searchQuery"
            placeholder="Enter email address"
            class="search-input"
            @input="handleSearchInput"
          />
          <Button
            label="SELECT"
            class="select-button"
            @click="selectUser"
            :loading="isSearching"
          />
        </div>
        <div v-if="errorMessage" class="error-message">
          <p>{{ errorMessage }}</p>
        </div>
      </div>

      <div class="selected-users-section">
        <div class="selected-users-header">
          <h3>Selected Users</h3>
          <Tag :value="userCountText" severity="secondary" />
        </div>
        <div v-if="hasSelectedUsers">
          <div
            v-for="user in selectedUsers"
            :key="user.ident"
            class="user-row"
          >
            <Tag :value="user.initials" />
            <div class="user-info">
              <b>{{ user.name }}</b>
              <div class="email-row">
                <span>{{ user.email }}</span>
                <Button
                  icon="pi pi-times"
                  variant="text"
                  @click="removeUser(user.ident)"
                  rounded
                />
              </div>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <p class="empty-message">No users selected</p>
        </div>
      </div>
    </div>

    <div class="dialog-actions">
      <Button label="CANCEL" variant="outlined" @click="handleCancel" />
      <Button
        label="SAVE CHANGES"
        icon="pi pi-save"
        class="add-button"
        @click="handleAddMembers"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject } from 'vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import { type CompanyRole } from '@clients/communitymanager';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialogRef = inject('dialogRef');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const companyId = computed(() => dialogRef.value.data.companyId as string);
const role = computed(() => dialogRef.value.data.role as CompanyRole);
const existingUsers: User[] = dialogRef.value.data.existingUsers || [];

interface User {
  email: string;
  ident: number;
  firstName?: string;
  lastName?: string;
  name: string;
  initials: string;
}

const searchQuery = ref('');
const isSearching = ref(false);
const generateInitials = (name: string) =>
  name
    .split(' ')
    .map((word) => word.charAt(0).toUpperCase())
    .join('')
    .substring(0, 2);

const selectedUsers = ref<User[]>(existingUsers.length > 0
  ? existingUsers.map(u => ({
    ...u,
    name: `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim() || u.email,
    initials: generateInitials(`${u.firstName ?? ''} ${u.lastName ?? ''}`.trim() || u.email)
  }))
  : []
);
const errorMessage = ref('');

const hasSelectedUsers = computed(() => selectedUsers.value.length > 0);
const userCountText = computed(() => {
  const count = selectedUsers.value.length;
  return `${count} User${count !== 1 ? 's' : ''}`;
});


const isUserAlreadySelected = (email: string) =>
  selectedUsers.value.some((user) => user.email.toLowerCase() === email.toLowerCase());

const validateAndAddUser = async (email: string) => {
  if (!apiClientProvider) {
    errorMessage.value = 'API client not initialized';
    return;
  }
  if (isUserAlreadySelected(email)) {
    errorMessage.value = 'User is already selected';
    return;
  }
  try {
    const userValidationControllerApi = apiClientProvider.apiClients.userValidationController;
    const response = await userValidationControllerApi.postEmailAddressValidation({ email });
    if (response.data) {
      const user = {
        email,
        ident: response.data.id,
        name: `${response.data.firstName || ''} ${response.data.lastName || ''}`.trim() || email,
        initials: generateInitials(`${response.data.firstName || ''} ${response.data.lastName || ''}`.trim() || email),
        ...response.data,
      };
      selectedUsers.value.push(user);
      searchQuery.value = '';
      errorMessage.value = '';
    } else {
      errorMessage.value = 'User not found';
    }
  } catch {
    errorMessage.value = 'User not found or email validation failed';
  } finally {
    isSearching.value = false;
  }
};

const handleSearchInput = () => {
  if (errorMessage.value) errorMessage.value = '';
};

async function selectUser() {
  const user = await validateAndAddUser(searchQuery.value.trim());
  if (user) {
    selectedUsers.value.push(user);
    searchQuery.value = '';
    errorMessage.value = '';
  }
}

const removeUser = (userId: number) => {
  selectedUsers.value = selectedUsers.value.filter((user) => user.id !== userId);
};

async function handleAddMembers(): Promise<void> {
  try {
    const companyRolesControllerApi = apiClientProvider.apiClients.companyRolesController;
    for (const user of selectedUsers.value) {
      await companyRolesControllerApi.assignCompanyRole(role.value, companyId.value, user.ident.toString());
    }
    dialogRef.value.close({ selectedUsers: selectedUsers.value });
  } catch {
    errorMessage.value = 'Failed to add users. Please try again.';
  }
}

const handleCancel = () => dialogRef.value.close();

</script>

<style scoped>
.selected-users-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.user-row {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid #e0e0e0;
}

.user-row:last-child {
  border-bottom: none;
}

.user-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.email-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.add-member-dialog {
  width: 70em;
  max-width: 90vw;
  display: flex;
  flex-direction: column;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.search-section {
  padding: var(--spacing-lg);
}

.search-container {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.search-input {
  width: 20em;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1.5rem;
  gap: 1rem;
}
</style>
