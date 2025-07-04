<template>
  <div class="user-menu-container" @click="toggleDropdown">
    <Button variant="outlined" severity="contrast" rounded icon="pi pi-user" />
    <img src="@/assets/images/elements/triangle_down.svg" class="d-triangle-down" alt="Open drop down menu icon" />
  </div>

  <PrimeMenu
    data-test="profileMenu"
    ref="menu"
    :model="menuItems"
    :popup="true"
    :pt="{
      root: {
        style: 'top: 4rem; right: 0',
      },
      item: ({ context }) => {
        return {
          style: context.disabled ? 'display: none' : '',
        };
      },
    }"
  />
</template>

<script setup lang="ts">
import router from '@/router';
import { KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakRoles';
import { logoutAndRedirectToUri } from '@/utils/KeycloakUtils';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import Button from 'primevue/button';
import PrimeMenu from 'primevue/menu';
import { type MenuItem } from 'primevue/menuitem';
import { computed, inject, onMounted, type Ref, ref, useTemplateRef } from 'vue';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const menu = useTemplateRef('menu');

const userIsReviewer = ref(false);

onMounted(() => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      userIsReviewer.value = keycloak.hasRealmRole(KEYCLOAK_ROLE_REVIEWER);
    })
    .catch((error) => console.log(error));
});

/**
 * Toggles Menu as Popup
 * @param event
 */
function toggleDropdown(event: Event): void {
  menu?.value?.toggle(event);
}

/**
 * Redirects the user to the keycloak user settings page
 */
function goToUserSettings(): void {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => keycloak.accountManagement())
    .catch((error) => console.log(error));
}

/**
 * Logs the user out and redirects him to the dataland homepage
 */
function logoutViaDropdown(): void {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => logoutAndRedirectToUri(keycloak, ''))
    .catch((error) => console.log(error));
}

const menuItems: Ref<MenuItem[]> = computed(() => [
  {
    label: 'USER SETTINGS',
    icon: 'pi pi-cog',
    id: 'profile-picture-dropdown-settings-button',
    command: goToUserSettings,
  },
  {
    label: 'API KEY',
    icon: 'pi pi-key',
    id: 'profile-api-generate-key-button',
    command: (): void => void router.push('/api-key'),
  },
  {
    label: 'DATA REQUEST',
    icon: 'pi pi-envelope',
    id: 'profile-picture-dropdown-data-request-button',
    command: (): void => void router.push('/bulkdatarequest'),
  },
  {
    label: 'QUALITY ASSURANCE',
    icon: 'pi pi-shield',
    id: 'profile-picture-dropdown-qa-services-anchor',
    command: (): void => void router.push('/qualityassurance'),
    disabled: !userIsReviewer.value,
  },
  {
    label: 'LOG OUT',
    icon: 'pi pi-sign-out',
    id: 'profile-picture-dropdown-logout-anchor',
    command: logoutViaDropdown,
  },
]);
</script>

<style scoped>
.user-menu-container {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}

.d-triangle-down {
  width: 0.625rem;
  margin-left: 0.5rem;
  margin-right: 1rem;
}
</style>
