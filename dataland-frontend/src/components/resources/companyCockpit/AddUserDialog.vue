<template>
  <div class="add-member-dialog">
    <div class="content-wrapper">
      <!-- Search Section -->
      <div class="search-section">
        <label>Search for any Dataland user to add</label>
        <div class="search-container">
          <InputText
            v-model="searchQuery"
            placeholder="Search by first name, last name, email or ID number"
            class="search-input"
            @input="handleSearchInput"
            @keyup.enter="selectUser"
          />

          <Button
            label="SELECT"
            class="select-button"
            :disabled="!canSelect"
            @click="selectUser"
            :loading="isSearching"
          />
        </div>
      </div>
      <div class="selected-users-section">
        <div class="selected-users-header">
          <h3>Selected Users</h3>
          <Tag :value="userCountText" severity="secondary" />
        </div>

        <div v-if="hasSelectedUsers">
          <div v-for="user in selectedUsers" :key="user.id" class="user-row">
            <Tag :value="user.initials" />
            <div class="user-info">
              <b>{{ user.name }}</b>
              <div class="email-row">
                <span>{{ user.email }}</span>
                <Button
                  icon="pi pi-times"
                  variant="text"
                  @click="removeUser(user.id)"
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

    <!-- Dialog Actions -->
    <div class="dialog-actions">
      <Button label="CANCEL" class="p-button-text cancel-button" @click="handleCancel" />
      <Button label="ADD MEMBER" class="add-button" :disabled="!canAddMembers" @click="handleAddMembers" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject } from 'vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Tag from 'primevue/tag';

// Inject dialog reference
const dialogRef = inject('dialogRef');

// Reactive state
const searchQuery = ref('');
const isSearching = ref(false);
const selectedUsers = ref([
  {
    id: 1,
    name: 'Stella Stevens',
    email: 'stella.stevens@deutsche-bank.de',
    initials: 'SS',
  },
]);

// Mock data for demonstration
const mockUsers = ref([
  {
    id: 2,
    name: 'John Doe',
    email: 'john.doe@example.com',
    initials: 'JD',
  },
  {
    id: 3,
    name: 'Jane Smith',
    email: 'jane.smith@company.com',
    initials: 'JS',
  },
  {
    id: 4,
    name: 'Mike Johnson',
    email: 'mike.johnson@corp.com',
    initials: 'MJ',
  },
]);

// Computed properties
const canSelect = computed(() => {
  return searchQuery.value.trim().length > 0 && !isSearching.value;
});

const hasSelectedUsers = computed(() => {
  return selectedUsers.value.length > 0;
});

const canAddMembers = computed(() => {
  return selectedUsers.value.length > 0;
});

const userCountText = computed(() => {
  const count = selectedUsers.value.length;
  return `${count} User${count !== 1 ? 's' : ''}`;
});

const generateInitials = (name) => {
  return name
    .split(' ')
    .map((word) => word.charAt(0).toUpperCase())
    .join('')
    .substring(0, 2);
};

const isUserAlreadySelected = (email) => {
  return selectedUsers.value.some((user) => user.email.toLowerCase() === email.toLowerCase());
};

// Search functionality
const handleSearchInput = () => {
  console.log('Searching for:', searchQuery.value);
};

const selectUser = async () => {
  if (!canSelect.value) return;

  isSearching.value = true;

  try {
    await new Promise((resolve) => setTimeout(resolve, 500));

    const searchTerm = searchQuery.value.trim().toLowerCase();
    let foundUser = mockUsers.value.find(
      (user) => user.name.toLowerCase().includes(searchTerm) || user.email.toLowerCase().includes(searchTerm)
    );

    if (!foundUser) {
      foundUser = {
        id: Date.now(),
        name: searchQuery.value.trim(),
        email: `${searchQuery.value.toLowerCase().replace(/\s+/g, '.')}@example.com`,
        initials: generateInitials(searchQuery.value.trim()),
        color: generateUserColor(),
      };
    }

    if (isUserAlreadySelected(foundUser.email)) {
      console.warn('User already selected');
      return;
    }

    selectedUsers.value.push(foundUser);
    searchQuery.value = '';
  } catch (error) {
    console.error('Error selecting user:', error);
  } finally {
    isSearching.value = false;
  }
};

// User management
const removeUser = (userId) => {
  selectedUsers.value = selectedUsers.value.filter((user) => user.id !== userId);
};

// Dialog actions
const handleAddMembers = () => {
  dialogRef.value.close({
    selectedUsers: selectedUsers.value,
  });
};

const handleCancel = () => {
  dialogRef.value.close();
};
</script>

<style>
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
}

.user-info {
  display: flex;
  flex-direction: column;
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
  grid-template-columns: 1fr 1fr; /* links Suche, rechts User */
  gap: 2rem;
}

.search-section {
  background: #fff;
  padding: 1rem;
  border-radius: 8px;
}

.search-container {
  display: flex;
  align-items: center;
  gap: 0.5rem; /* Abstand zwischen Input und Button */
  margin-top: 0.5rem;
}

.search-input {
  flex: 1; /* Input nimmt die volle Breite */
  height: 40px; /* gleiche Höhe wie Button */
}

.select-button {
  height: 40px;
  white-space: nowrap;
}

.selected-users-section {
  background: #fff;
  padding: 1rem;
  border-radius: 8px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1.5rem;
  gap: 1rem;
}
</style>
