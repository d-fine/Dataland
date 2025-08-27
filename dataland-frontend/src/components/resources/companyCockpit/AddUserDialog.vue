<template>
  <div class="content-wrapper">
    <Card>
      <template #content>
        <b>Add user by email address</b>
        <div class="search-container">
          <InputText v-model="searchQuery" placeholder="Enter email address" class="search-input" />
          <Button label="SELECT" @click="selectUser" data-test="select-user-button" />
          <Message
            v-if="unknownUserError"
            severity="error"
            data-test="unknown-user-error"
            :pt="{
              root: {
                style: 'margin-top: var(--spacing-xxs); width: 20em; word-wrap: break-word;',
              },
            }"
          >
            {{ unknownUserError }}
          </Message>
        </div>
      </template>
    </Card>
    <Card class="selected-users-section">
      <template #content>
        <div class="selected-users-header">
          <h3>Selected Users</h3>
          <Tag :value="userCountText" severity="secondary" data-test="user-count-tag" />
        </div>
        <div v-if="hasSelectedUsers">
          <div v-for="user in selectedUsers" :key="user.userId" class="user-row">
            <Tag :value="user.initials" />
            <div class="user-info">
              <b>{{ user.name }}</b>
              <span class="email-row">{{ user.email }}</span>
            </div>
            <Button
              icon="pi pi-times"
              variant="text"
              @click="handleRemoveUser(user.userId)"
              rounded
              data-test="remove-user-button"
            />
          </div>
        </div>
        <div v-else>
          <p>No users selected</p>
        </div>
      </template>
    </Card>
  </div>
  <div class="dialog-actions">
    <Button
      label="ADD SELECTED USERS"
      icon="pi pi-plus"
      class="add-button"
      @click="handleAddUser"
      data-test="save-changes-button"
    />
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
import { AxiosError } from 'axios';
import Message from 'primevue/message';
import Card from 'primevue/card';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const props = defineProps<{
  companyId: string;
  role: CompanyRole;
  existingUsers: Array<{ userId: string; email: string; firstName: string; lastName: string }>;
}>();

const emit = defineEmits<{
  'users-added': [message?: string];
  'close-dialog': [];
}>();

const companyRolesControllerApi = apiClientProvider.apiClients.companyRolesController;
const selectedUsers = ref<User[]>([]);
const hasSelectedUsers = computed(() => selectedUsers.value.length > 0);
const searchQuery = ref('');
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
    .join('');
}

/**
 * Validates the email address and adds the user to the selected users list if valid.
 * @param email - The email address to validate and add
 */
async function validateAndAddUser(email: string): Promise<void> {
  if (!email) return;
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
      selectedUsers.value.some((u) => u.userId === user.userId) ||
      props.existingUsers.some((u) => u.userId === user.userId);

    if (alreadySelected) {
      unknownUserError.value = 'This user has already been selected.';
      return;
    }

    selectedUsers.value.push(user);
    searchQuery.value = '';
  } catch (error) {
    if (error instanceof AxiosError) {
      unknownUserError.value = error.response?.data?.errors?.[0]?.message;
    } else {
      unknownUserError.value = 'An unknown error occurred while validating the user.';
      console.error(error);
    }
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
    const originalUserIds = new Set(props.existingUsers.map((user) => user.userId));
    const usersToAdd = selectedUsers.value.filter((user) => !originalUserIds.has(user.userId));

    if (usersToAdd.length === 0) {
      emit('close-dialog');
      return;
    }

    await Promise.all(
      usersToAdd.map((user) =>
        companyRolesControllerApi.assignCompanyRole(props.role, props.companyId, user.userId.toString())
      )
    );

    emit('users-added', 'User(s) successfully added.');
  } catch (error) {
    if (error instanceof AxiosError) {
      unknownUserError.value = error.response?.data?.errors?.[0]?.message;
    } else {
      unknownUserError.value = 'An unknown error occurred while adding users.';
      console.error(error);
    }
  }
}
</script>

<style scoped>
.selected-users-header {
  position: sticky;
  top: 0;
  background-color: var(--p-surface-0);
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 0.1em solid var(--p-surface-300);
  margin-bottom: var(--spacing-xs);
}

.user-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xs);
}

.user-info {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-xs);
}

.selected-users-section {
  height: 20em;
  overflow-y: auto;
  position: relative;
}

.search-container {
  margin-top: var(--spacing-sm);
}

.search-input {
  width: 20em;
  margin-right: var(--spacing-xs);
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
