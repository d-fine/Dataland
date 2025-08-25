<template>
  <div class="add-member-dialog">
    <div class="content-wrapper">
      <div class="search-section">
        <b>Add user by email address</b>
        <div class="search-container">
          <InputText v-model="searchQuery" placeholder="Enter email address" class="search-input" />
          <Button label="SELECT" @click="selectUser" :pt="{ root: { style: 'margin-left: var(--spacing-xxs);' } }" />
          <Message
            v-if="unknownUserError"
            severity="error"
            data-test="unknown-user-error"
            :pt="{
              root: {
                style: 'margin-top: var(--spacing-xxs); max-width: 20em; word-wrap: break-word;',
              },
            }"
          >
            {{ unknownUserError }}
          </Message>
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
      <Button label="SAVE CHANGES" icon="pi pi-save" class="add-button" @click="handleAddUser" />
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
import { AxiosError } from 'axios';
import Message from 'primevue/message';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const companyId = computed(() => dialogRef?.value.data.companyId as string);
const role = computed(() => dialogRef?.value.data.role as CompanyRole);
const existingUsers: User[] = dialogRef?.value.data.existingUsers || [];
const companyRolesControllerApi = apiClientProvider.apiClients.companyRolesController;
const selectedUsers = ref<User[]>([]);
const hasSelectedUsers = computed(() => selectedUsers.value.length > 0);
const searchQuery = ref('');
const isSearching = ref(false);
const unknownUserError = ref('');
const userCountText = computed(() => {
  const count = selectedUsers.value?.length;
  return `${count} User${count !== 1 ? 's' : ''}`;
});

type User = {
  email: string;
  userId: string;
  firstName?: string;
  lastName?: string;
  name: string;
  initials: string;
};

/**
 * Generates initials from a given name.
 * @param name - The full name of the user
 * @returns The initials derived from the name
 */
function generateInitials(name: string): string {
  return name
    .split(' ')
    .map((word) => word.charAt(0).toUpperCase())
    .join('')
    .substring(0, 2);
}

/**
 * Validates the email address and adds the user to the selected users list if valid.
 * @param email - The email address to validate and add
 */
async function validateAndAddUser(email: string): Promise<void> {
  if (!email) return;

  isSearching.value = true;
  unknownUserError.value = '';

  try {
    const userValidationControllerApi = apiClientProvider.apiClients.userValidationController;
    const response = await userValidationControllerApi.postEmailAddressValidation({ email });

    const user = {
      email,
      name: `${response.data.firstName || ''} ${response.data.lastName || ''}`.trim() || email,
      initials: generateInitials(`${response.data.firstName || ''} ${response.data.lastName || ''}`.trim() || email),
      ...response.data,
      userId: response.data.id,
    };

    const alreadySelected =
      selectedUsers.value.some((u) => u.userId === user.userId) || existingUsers.some((u) => u.userId === user.userId);

    if (alreadySelected) {
      unknownUserError.value = 'This user has already been selected.';
      return;
    }

    selectedUsers.value.push(user);
    searchQuery.value = '';
  } catch (error) {
    console.log('ERROR: ', error);
    if (error instanceof AxiosError) {
      unknownUserError.value = error.response?.data?.errors?.[0]?.message;
    } else {
      unknownUserError.value = 'An unknown error occurred while validating the user.';
      console.error(error);
    }
  } finally {
    isSearching.value = false;
  }
}

/**
 * Handles the selection of a user based on the search query.
 * It validates the email and adds the user to the selected users list.
 */
async function selectUser(): Promise<void> {
  await validateAndAddUser(searchQuery.value.trim());
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
async function handleAddUser(): Promise<void> {
  try {
    const originalUserIds = new Set(existingUsers.map((user) => user.userId));
    const usersToAdd = selectedUsers.value.filter((user) => !originalUserIds.has(user.userId));

    if (usersToAdd.length === 0) {
      dialogRef?.value.close({ selectedUsers: selectedUsers.value });
      return;
    }

    for (const user of usersToAdd) {
      await companyRolesControllerApi.assignCompanyRole(role.value, companyId.value, user.userId.toString());
    }

    dialogRef?.value.close({ selectedUsers: selectedUsers.value });
  } catch {
    unknownUserError.value = 'Failed to save changes. Please try again.';
  } finally {
    dialogRef?.value.close({ selectedUsers: selectedUsers.value });
  }
}

</script>

<style scoped>
.add-member-dialog {
  background-color: var(--p-surface-100);
  padding: var(--spacing-lg);
  max-height: 30em;
}

.selected-users-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-row {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.search-section {
  background-color: var(--p-surface-0);
  padding: var(--spacing-lg);
  border-radius: 8px;
}

.selected-users-section {
  background-color: var(--p-surface-0);
  padding: var(--spacing-lg);
  border-radius: 8px;
  min-height: 20em;
}

.search-container {
  display: block;
  margin-top: var(--spacing-sm);
}

.search-input {
  width: 20em;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
}
</style>