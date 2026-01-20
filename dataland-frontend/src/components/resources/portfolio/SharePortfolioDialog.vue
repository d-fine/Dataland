<template>
  <div class="content-wrapper">
    <Card class="equal-height-card">
      <template #content>
        <b>Search user by email address</b>
        <div class="search-container">
          <div class="search-row">
            <InputText
              v-model="searchQuery"
              placeholder="Enter email address"
              class="search-input"
              data-test="email-input-field"
              @keydown.enter="selectUser(searchQuery)"
            />
            <Button label="SELECT" @click="selectUser(searchQuery)" data-test="select-user-button" />
          </div>
          <Message
            v-if="errorMessage"
            severity="error"
            data-test="error-message"
            :pt="{
              root: { style: 'margin-top: var(--spacing-xxs); max-width:30rem' },
              text: { style: 'white-space: normal; overflow-wrap: anywhere; word-break: break-word;' },
            }"
          >
            {{ errorMessage }}
          </Message>
        </div>
      </template>
    </Card>

    <Card class="equal-height-card">
      <template #content>
        <div class="selected-users-header">
          <h3>Users with Access</h3>
          <Tag :value="userCountText" severity="secondary" data-test="user-count-tag" />
        </div>

        <template v-if="!isLoadingUsers">
          <div v-if="usersWithAccess.length > 0">
            <Listbox
              :options="usersWithAccess"
              data-test="users-with-access-listbox"
              style="min-height: 4rem; margin-top: 21px"
              :highlightOnSelect="false"
              :pt="{
                option: { style: 'cursor: default; height: 4rem;' },
                listContainer: { style: 'overflow:auto; max-height: 20rem;' },
              }"
            >
              <template #option="{ option }">
                <div class="user-row" data-test="user-row">
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
                    data-test="remove-user-button"
                  />
                </div>
              </template>
            </Listbox>
          </div>
          <div v-else>
            <Divider />
            <p>Portfolio not shared with anyone</p>
          </div>
        </template>
      </template>
    </Card>
  </div>
  <div class="dialog-actions">
    <Button
      label="SAVE CHANGES"
      icon="pi pi-check"
      class="save-button"
      :disabled="isSaving"
      :loading="isSaving"
      @click="handleSaveChanges()"
      data-test="save-changes-button"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject, onMounted, type Ref } from 'vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Tag from 'primevue/tag';
import Card from 'primevue/card';
import Listbox from 'primevue/listbox';
import Divider from 'primevue/divider';
import Message from 'primevue/message';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import { AxiosError } from 'axios';
import { PortfolioUserAccessRightPortfolioAccessRoleEnum } from '@clients/userservice';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');

type User = {
  email: string;
  userId: string;
  name: string;
  initials: string;
};

const usersWithAccess = ref<User[]>([]);
const searchQuery = ref('');
const errorMessage = ref('');
const isLoadingUsers = ref(false);
const isSaving = ref(false);
const portfolioId = ref<string>('');

const userCountText = computed(() => {
  const count = usersWithAccess.value.length;
  return `${count} User${count === 1 ? '' : 's'}`;
});

onMounted(async () => {
  const data = dialogRef?.value.data;
  if (data?.portfolioId) {
    portfolioId.value = data.portfolioId as string;
    await loadUsersWithAccess();
  }
});

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
 * Loads users who currently have access to the portfolio.
 */
async function loadUsersWithAccess(): Promise<void> {
  isLoadingUsers.value = true;
  try {
    const response = await apiClientProvider.apiClients.portfolioController.getPortfolioAccessRights(portfolioId.value);
    usersWithAccess.value = response.data
      .filter((user) => user.portfolioAccessRole === PortfolioUserAccessRightPortfolioAccessRoleEnum.Reader)
      .map((user) => ({
        email: user.userEmail ?? '',
        userId: user.userId,
        name: user.userEmail ?? user.userId,
        initials: generateInitials(user.userEmail ?? user.userId),
      }));
  } catch (error) {
    console.error('Error loading users with access:', error);
    errorMessage.value = 'Failed to load users with access.';
  } finally {
    isLoadingUsers.value = false;
  }
}

/**
 * Validates and adds a user by email to the users with access list.
 * @param email - The email address to validate and add
 */
async function selectUser(email: string): Promise<void> {
  if (!email) return;
  errorMessage.value = '';

  try {
    const response = await apiClientProvider.apiClients.emailAddressController.postEmailAddressValidation({
      email: email,
    });
    const user: User = {
      email: email,
      userId: response.data.id,
      name: `${response.data.firstName || ''} ${response.data.lastName || ''}`.trim() || email,
      initials: generateInitials(`${response.data.firstName || ''} ${response.data.lastName || ''}`.trim() || email),
    };

    if (usersWithAccess.value.some((u) => u.userId === user.userId)) {
      errorMessage.value = 'This user already has access to the portfolio.';
      return;
    }

    usersWithAccess.value.push(user);
    searchQuery.value = '';
  } catch (error) {
    if (error instanceof AxiosError) {
      errorMessage.value = error.response?.data?.errors?.[0]?.message || 'Failed to validate email address.';
    } else {
      errorMessage.value = 'An unknown error occurred.';
      console.error(error);
    }
  }
}

/**
 * Removes a user from the users with access list.
 * @param userId - The user ID to remove
 */
function handleRemoveUser(userId: string): void {
  usersWithAccess.value = usersWithAccess.value.filter((user) => user.userId !== userId);
}

/**
 * Saves the updated list of users with access by calling the patchSharing endpoint.
 */
async function handleSaveChanges(): Promise<void> {
  isSaving.value = true;
  try {
    // as unknown as Set<string> cast required to ensure proper json is created
    const sharedUserIds = usersWithAccess.value.map((user) => user.userId) as unknown as Set<string>;
    await apiClientProvider.apiClients.portfolioController.patchSharing(portfolioId.value, { sharedUserIds });
    dialogRef?.value.close({ saved: true });
  } catch (error) {
    if (error instanceof AxiosError) {
      errorMessage.value = error.response?.data?.errors?.[0]?.message || 'Failed to save changes.';
    } else {
      errorMessage.value = 'An unknown error occurred while saving.';
      console.error(error);
    }
  } finally {
    isSaving.value = false;
  }
}
</script>

<style scoped>
.content-wrapper {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-xs);
  align-items: start;
}

.equal-height-card {
  min-height: 24rem;
  height: 100%;
  display: flex;
  flex-direction: column;
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

.dialog-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1440px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
}
</style>
