<template>
  <div class="content-wrapper">
    <Card class="equal-height-card">
      <template #content>
        <b>Add user by email address</b>
        <div class="search-container">
          <div class="search-row">
            <InputText
              v-model="searchQuery"
              placeholder="Enter email address"
              class="search-input"
              data-test="email-input-field"
              @keydown.enter="selectUser(searchQuery, true)"
            />
            <Button label="SELECT" @click="selectUser(searchQuery, true)" data-test="select-user-button" />
          </div>
          <Message
            v-if="unknownUserError"
            severity="error"
            data-test="unknown-user-error"
            :pt="{
              root: {
                style: 'margin-top: var(--spacing-xxs); max-width:30rem',
              },
              text: {
                style: 'white-space: normal; overflow-wrap: anywhere; word-break: break-word;',
              },
            }"
          >
            {{ unknownUserError }}
          </Message>
        </div>

        <Listbox
          v-if="suggestedUsers.length > 0"
          :options="suggestedUsers"
          data-test="suggestion-listbox"
          style="margin-top: var(--spacing-sm); min-height: 4rem"
          :highlightOnSelect="false"
          :pt="{
            option: {
              style: 'cursor: default; height: 4rem;',
            },
            listContainer: { style: 'overflow:auto; max-height: 20rem;' },
          }"
        >
          <template #option="{ option }">
            <div class="user-row" data-test="suggested-user-row">
              <Tag :value="option.initials" />
              <div class="user-info">
                <b>{{ option.name }}</b>
                <span>{{ option.email }}</span>
              </div>
              <div class="select-container">
                <Tag
                  v-if="selectedUsers.some((user) => user.userId === option.userId)"
                  value="Selected"
                  severity="secondary"
                  data-test="selected-tag"
                />
                <Button
                  v-else
                  label="Select"
                  variant="text"
                  @click.stop="selectUser(option.email, false)"
                  rounded
                  data-test="select-suggested-user-button"
                />
              </div>
            </div>
          </template>
        </Listbox>
      </template>
    </Card>
    <Card class="selected-users-section equal-height-card">
      <template #content>
        <div class="selected-users-header">
          <h3>Selected Users</h3>
          <Tag :value="userCountText" severity="secondary" data-test="user-count-tag" />
        </div>

        <div v-if="hasSelectedUsers">
          <Listbox
            :options="selectedUsers"
            data-test="selected-users-listbox"
            style="min-height: 4rem; margin-top: 21px"
            :highlightOnSelect="false"
            :pt="{
              option: {
                style: 'cursor: default; height: 4rem;',
              },
              listContainer: { style: 'overflow:auto; max-height: 20rem;' },
            }"
          >
            <template #option="{ option }">
              <div class="user-row" data-test="selected-user-row">
                <Tag :value="option.initials" />
                <div class="user-info">
                  <b>{{ option.name }}</b>
                  <span>{{ option.email }}</span>
                </div>
                <Button
                  label="Remove"
                  variant="text"
                  @click="handleRemoveUser(option.userId)"
                  rounded
                  data-test="remove-selected-user-button"
                />
              </div>
            </template>
          </Listbox>
        </div>
        <div v-else>
          <Divider />
          <p>No users selected</p>
        </div>
      </template>
    </Card>
  </div>
  <div class="dialog-actions">
    <Button
      label="ADD SELECTED USERS"
      :disabled="!hasSelectedUsers"
      icon="pi pi-plus"
      class="add-button"
      @click="onAddSelectedUsersClick()"
      data-test="save-changes-button"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject, onMounted } from 'vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import type { CompanyRole } from '@clients/communitymanager';
import { AxiosError } from 'axios';
import Message from 'primevue/message';
import Card from 'primevue/card';
import Listbox from 'primevue/listbox';
import Divider from 'primevue/divider';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const props = defineProps<{
  companyId: string;
  role: CompanyRole;
  existingUsers: Array<{ userId: string; email: string; firstName: string; lastName: string }>;
  currentUserId: string | undefined;
}>();

const emit = defineEmits<{
  'users-added': [message?: string];
  'confirmation-is-required-for-add-users': [];
}>();

defineExpose({ handleAddUser });

const companyRolesControllerApi = apiClientProvider.apiClients.companyRolesController;
const emailAddressControllerApi = apiClientProvider.apiClients.emailAddressController;
const selectedUsers = ref<User[]>([]);
const hasSelectedUsers = computed(() => selectedUsers.value.length > 0);
const usersToAdd = ref<User[]>([]);
const searchQuery = ref('');
const unknownUserError = ref('');
const userCountText = computed(() => {
  const count = selectedUsers.value?.length;
  return `${count} User${count !== 1 ? 's' : ''}`;
});
const suggestedUsers = ref<User[]>([]);

onMounted(async () => {
  suggestedUsers.value = await getSuggestedUsers();
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
 */
async function validateAndAddUser(email: string, byInputText: boolean): Promise<void> {
  if (!email) return;
  unknownUserError.value = '';

  try {
    const emailAddressControllerApi = apiClientProvider.apiClients.emailAddressController;
    const response = await emailAddressControllerApi.postEmailAddressValidation({ email });

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
    if (byInputText) {
      searchQuery.value = '';
    }
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
async function selectUser(userEmail: string, byInputText: boolean): Promise<void> {
  await validateAndAddUser(userEmail.trim(), byInputText);
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
 * Handles the event when the "Add Selected Users" button is clicked.
 */
async function onAddSelectedUsersClick(): Promise<void> {
  if (isCurrentUserBeingAdded()) {
    emit('confirmation-is-required-for-add-users');
  } else {
    await handleAddUser();
  }
}

/**
 * Confirms if the current user is among the users to be added.
 */
function isCurrentUserBeingAdded(): boolean {
  const originalUserIds = new Set(props.existingUsers.map((user) => user.userId));
  usersToAdd.value = selectedUsers.value.filter((user) => !originalUserIds.has(user.userId));
  return usersToAdd.value.some((user) => user.userId === props.currentUserId);
}

/**
 * Handles the addition of selected users to the company role.
 * It iterates over the selected users and assigns the specified role to each user.
 */
async function handleAddUser(): Promise<void> {
  try {
    if (usersToAdd.value.length === 0) {
      return;
    }
    await Promise.all(
      usersToAdd.value.map((user) =>
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

/**
 * Fetches and returns a list of suggested users from the associated company's subdomains.
 * @return {Promise<User[]>} A promise that resolves to an array of `User` objects representing the suggested users.
 */
async function getSuggestedUsers(): Promise<User[]> {
  const suggestedUsersResponse = await emailAddressControllerApi.getUsersByCompanyAssociatedSubdomains(props.companyId);
  const suggestedKeyCloakUsers = suggestedUsersResponse.data;
  const suggestedUsers: User[] = [];
  for (const keycloakUser of suggestedKeyCloakUsers) {
    const name = `${keycloakUser.firstName || ''} ${keycloakUser.lastName || ''}`.trim() || keycloakUser.email || '';
    suggestedUsers.push({
      email: keycloakUser.email || '',
      userId: keycloakUser.id,
      firstName: keycloakUser.firstName,
      lastName: keycloakUser.lastName,
      name: name,
      initials: generateInitials(name),
    });
  }
  return suggestedUsers;
}
</script>

<style scoped>
.selected-users-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-row {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xs);
  width: 100%;
}

.user-info {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.content-wrapper {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-xs);
  align-items: start;
}

.content-wrapper > * {
  min-width: 0;
}

@media (max-width: 1440px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
}

.equal-height-card {
  min-height: 24rem;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.selected-users-section {
  overflow-y: auto;
  position: relative;
}

.search-container {
  margin-top: var(--spacing-sm);
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.search-row {
  display: flex;
  align-items: center;
}

.search-input {
  flex: 1 1 0%;
  min-width: 0;
  margin-right: var(--spacing-xs);
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
}

.select-container {
  width: 6rem;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}
</style>
