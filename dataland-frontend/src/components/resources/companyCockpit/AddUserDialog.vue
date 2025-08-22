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
          <Button label="SELECT" class="select-button" @click="selectUser" />
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
          <div v-for="user in selectedUsers" :key="user.userId" class="user-row">
            <Tag :value="user.initials" />
            <div class="user-info">
              <b>{{ user.name }}</b>
              <div class="email-row">
                <span>{{ user.email }}</span>
                <Button icon="pi pi-times" variant="text" @click="handleRemoveUser(user.userId)" rounded />
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
      <Button label="SAVE CHANGES" icon="pi pi-save" class="add-button" @click="handleAddMembers" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject, type Ref } from 'vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import { type CompanyRole } from '@clients/communitymanager';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const companyId = computed(() => dialogRef?.value.data.companyId as string);
const role = computed(() => dialogRef?.value.data.role as CompanyRole);
const existingUsers: User[] = dialogRef?.value.data.existingUsers || [];
const companyRolesControllerApi = apiClientProvider.apiClients.companyRolesController;

interface User {
  email: string;
  userId: string;
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

const selectedUsers = ref<User[]>(
    existingUsers.length > 0
        ? existingUsers.map((user) => ({
          ...user,
          userId: user.userId,
          name: `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() || user.email,
          initials: generateInitials(`${user.firstName ?? ''} ${user.lastName ?? ''}`.trim() || user.email),
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
        name: `${response.data.firstName || ''} ${response.data.lastName || ''}`.trim() || email,
        initials: generateInitials(`${response.data.firstName || ''} ${response.data.lastName || ''}`.trim() || email),
        ...response.data,
        userId: response.data.id,
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

/**
 * Handles the selection of a user based on the search query.
 * It validates the email and adds the user to the selected users list.
 */
async function selectUser() {
  const user = await validateAndAddUser(searchQuery.value.trim());
  if (user) {
    selectedUsers.value.push(user);
    searchQuery.value = '';
    errorMessage.value = '';
  }
}

/**
 * Handles the removal of a user from the selected users list.
 * The actual API call will happen when "SAVE CHANGES" is clicked.
 * @param userId - The user ID to remove from the selection
 */
function handleRemoveUser(userId: string): void {
  selectedUsers.value = selectedUsers.value.filter((user) => user.userId !== userId);
}

/**
 * Handles the addition of selected users to the company role.
 * It iterates over the selected users and assigns the specified role to each user.
 */
async function handleAddMembers(): Promise<void> {
  try {

    console.log("EXISTING USERS", existingUsers);
    const originalUserIds = new Set(existingUsers.map((user) => user.userId));
    const currentUserIds = new Set(selectedUsers.value.map((user) => user.userId));
    const usersToAdd = selectedUsers.value.filter((user) => !originalUserIds.has(user.userId));
    const usersToRemove = existingUsers.filter((user) => !currentUserIds.has(user.userId));

    console.log("I GOT HERE")

    if (usersToAdd.length === 0 && usersToRemove.length === 0) {
      dialogRef?.value.close({ selectedUsers: selectedUsers.value });
      return;
    }

    console.log("I GOT HERE 2")

      for (const user of usersToAdd) {
        await companyRolesControllerApi.assignCompanyRole(role.value, companyId.value, user.userId.toString());
      }

      for (const user of usersToRemove) {
        console.log(user.userId)
        await companyRolesControllerApi.removeCompanyRole(role.value, companyId.value, user.userId.toString());

      }

    console.log("USERS TO REMOVE", usersToRemove);

    dialogRef?.value.close({ selectedUsers: selectedUsers.value });
  } catch {
    errorMessage.value = 'Failed to save changes. Please try again.';
  } finally {
    dialogRef?.value.close({ selectedUsers: selectedUsers.value });
  }
}

const handleCancel = () => dialogRef?.value.close();
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
