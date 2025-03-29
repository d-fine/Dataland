<template>
  <div
    class="flex align-items-center d-cursor-pointer"
    id="profile-picture-dropdown-toggle"
    @click="toggleDropdownMenu"
  >
    <img
      ref="profile-picture"
      class="d-profile-picture"
      :src="profilePictureSource"
      alt="User profile"
      referrerpolicy="no-referrer"
      @error="handleProfilePicError"
    />
    <img src="@/assets/images/elements/triangle_down.svg" class="d-triangle-down" alt="Open drop down menu icon" />
  </div>
  <PrimeMenu data-test="profileMenu" ref="menu" :model="dropdownMenuItems" :popup="true">
    <template #item="{ item }">
      <a
        v-if="hasRole(item.role)"
        class="p-menuitem-link"
        role="menuitem"
        tabindex="0"
        @click="item.clickAction()"
        :id="item.id"
      >
        <span class="p-menuitem-icon material-icons">{{ item.icon }}</span>
        <span class="p-menuitem-text">{{ item.label }}</span>
      </a>
    </template>
  </PrimeMenu>
</template>

<script lang="ts">
import PrimeMenu from 'primevue/menu';
import { defineComponent, inject, ref } from 'vue';
import type { Ref } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import defaultProfilePicture from '@/assets/images/elements/default_user_icon.svg';
import { logoutAndRedirectToUri } from '@/utils/KeycloakUtils';
import router from '@/router';
import { KEYCLOAK_ROLE_REVIEWER } from '@/utils/KeycloakRoles';

export default defineComponent({
  name: 'UserProfileDropDown',
  components: { PrimeMenu },
  emits: ['profilePictureLoadingError', 'profilePictureObtained'],
  setup() {
    const menu: Ref<typeof PrimeMenu | undefined> = ref();

    /**
     * Toggles the dropdown menu (shows/hides it) on a mouse click.
     * Used as an event handler by the dropdown-toggle UI element.
     * @param event the event of the click
     */
    function toggleDropdownMenu(event: Event): void {
      if (menu.value !== undefined) {
        menu.value.toggle(event);
      }
    }
    /**
     * Hides the dropdown menu.
     * Used as an event handler through the on scroll event.
     */
    function hideDropdownMenu(): void {
      assertDefined(menu.value).hide();
    }
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
      menu,
      toggleDropdownMenu,
      hideDropdownMenu,
    };
  },
  data() {
    return {
      dropdownMenuItems: [
        {
          label: 'USER SETTINGS',
          icon: 'settings',
          id: 'profile-picture-dropdown-settings-button',
          clickAction: this.goToUserSettings,
        },
        {
          label: 'API KEY',
          icon: 'key',
          id: 'profile-api-generate-key-button',
          clickAction: this.goToApiKeysPage,
        },
        {
          label: 'DATA REQUEST',
          icon: 'mail',
          id: 'profile-picture-dropdown-data-request-button',
          clickAction: this.goToDataRequest,
        },
        {
          label: 'QUALITY ASSURANCE',
          icon: 'add_moderator',
          id: 'profile-picture-dropdown-qa-services-anchor',
          clickAction: this.goToQualityAssurance,
          role: KEYCLOAK_ROLE_REVIEWER,
        },
        {
          label: 'LOG OUT',
          icon: 'logout',
          id: 'profile-picture-dropdown-logout-anchor',
          clickAction: this.logoutViaDropdown,
        },
      ],
      profilePictureSource: defaultProfilePicture,
      hasRole: ((role: string) => !role) as (role: string) => boolean,
    };
  },
  mounted() {
    window.addEventListener('scroll', this.hideDropdownMenu);
  },
  unmounted() {
    window.removeEventListener('scroll', this.hideDropdownMenu);
  },
  methods: {
    /**
     * Logs the user out and redirects him to the dataland homepage
     */
    logoutViaDropdown() {
      assertDefined(this.getKeycloakPromise)()
        .then((keycloak) => {
          logoutAndRedirectToUri(keycloak, '');
        })
        .catch((error) => console.log(error));
    },
    /**
     * Redirects the user to the keycloak user settings page
     */
    goToUserSettings() {
      assertDefined(this.getKeycloakPromise)()
        .then((keycloak) => {
          return keycloak.accountManagement();
        })
        .catch((error) => console.log(error));
    },
    /**
     * Redirects the user to the data-request/invite screen
     */
    async goToDataRequest() {
      await router.push('/bulkdatarequest');
    },
    /**
     * Redirects the user to the api-key management interface
     */
    async goToApiKeysPage() {
      await router.push('/api-key');
    },
    /**
     * Redirects the user to the QA Services page
     */
    async goToQualityAssurance() {
      await router.push('/qualityassurance');
    },
    /**
     * Called when the profile picture could not load. Propagates the event and sets the profile picture
     * to a default image
     */
    handleProfilePicError() {
      if (this.profilePictureSource !== defaultProfilePicture) {
        this.$emit('profilePictureLoadingError');
        this.profilePictureSource = defaultProfilePicture;
      }
    },
  },
  created() {
    assertDefined(this.getKeycloakPromise)()
      .then((keycloak) => {
        if (keycloak.authenticated) {
          this.hasRole = (role: string | undefined): boolean => (!role ? true : keycloak.hasRealmRole(role));

          if (keycloak.idTokenParsed?.picture) {
            const profilePictureUrl = keycloak.idTokenParsed.picture as string;
            this.$emit('profilePictureObtained', profilePictureUrl);
            this.profilePictureSource = profilePictureUrl;
          }
        }
      })
      .catch((error) => console.log(error));
  },
});
</script>

<style scoped>
.p-menuitem-link {
  background-color: #0b191f;
}

.d-profile-picture {
  border-radius: 50%;
  height: 2.5rem;
}

.d-triangle-down {
  width: 0.625rem;
  margin-left: 0.5rem;
  margin-right: 1rem;
}

.p-menu .p-menuitem:not(.p-highlight):not(.p-disabled).p-focus > .p-menuitem-content .p-menuitem-link .p-menuitem-text {
  color: #e67f3fff;
}
.p-menu .p-menuitem:not(.p-highlight):not(.p-disabled).p-focus > .p-menuitem-content .p-menuitem-link .p-menuitem-icon,
.p-menu .p-menuitem:not(.p-highlight):not(.p-disabled).p-focus > .p-menuitem-content .p-menuitem-link .p-submenu-icon {
  color: #e67f3fff;
}
</style>
