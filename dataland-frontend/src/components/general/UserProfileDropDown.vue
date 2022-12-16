<template>
  <div
    class="flex align-items-center d-drop-down-toggle"
    id="profile-picture-dropdown-toggle"
    @click="toggleDropdownMenu"
  >
    <img
      ref="profile-picture"
      class="d-profile-picture"
      :src="profilePictureSource"
      alt="User profile picture"
      referrerpolicy="no-referrer"
      @error="handleProfilePicError"
    />
    <img src="@/assets/images/elements/triangle_down.svg" class="d-triangle-down" alt="Open drop down menu icon" />
  </div>
  <PrimeMenu
    ref="menu"
    :model="dropdownMenuItems"
    :popup="true"
    style="transform: translate(0px, 1rem)"
    class="text-primary surface-900 p-0"
  >
    <template #item="{ item }">
      <a class="p-menuitem-link" role="menuitem" tabindex="0" @click="item.clickAction()" :id="item.id">
        <span class="p-menuitem-icon material-icons text-primary font-semibold">{{ item.icon }}</span>
        <span class="p-menuitem-text text-primary font-semibold">{{ item.label }}</span>
      </a>
    </template>
  </PrimeMenu>
</template>

<script lang="ts">
import PrimeMenu from "primevue/menu";
import { defineComponent, inject, ref } from "vue";
import type { Ref } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import defaultProfilePicture from "@/assets/images/elements/default_user_icon.svg";

export default defineComponent({
  name: "UserProfileDropDown",
  components: { PrimeMenu },
  emits: ["profilePictureLoadingError", "profilePictureObtained"],
  setup() {
    const menu: Ref<PrimeMenu | undefined> = ref();
    function toggleDropdownMenu(event: Event): void {
      if (menu.value !== undefined) {
        menu.value.toggle(event);
      }
    }
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      authenticated: inject<boolean>("authenticated"),
      menu,
      toggleDropdownMenu,
    };
  },

  data() {
    return {
      dropdownMenuItems: [
        {
          label: "USER SETTINGS",
          icon: "settings",
          id: "profile-picture-dropdown-settings-button",
          clickAction: this.gotoUserSettings,
        },
        {
          label: "DATA REQUEST",
          icon: "mail",
          id: "profile-picture-dropdown-data-request-button",
          clickAction: this.gotoDataRequest,
        },
        {
          label: "LOG OUT",
          icon: "logout",
          id: "profile-picture-dropdown-toggle",
          clickAction: this.logoutViaDropdown,
        },
      ],
      profilePictureSource: defaultProfilePicture,
    };
  },

  methods: {
    logoutViaDropdown() {
      assertDefined(this.getKeycloakPromise)()
        .then((keycloak) => {
          if (keycloak.authenticated) {
            const baseUrl = window.location.origin;
            const url = keycloak.createLogoutUrl({ redirectUri: `${baseUrl}` });
            location.assign(url);
          }
        })
        .catch((error) => console.log(error));
    },
    gotoUserSettings() {
      assertDefined(this.getKeycloakPromise)()
        .then((keycloak) => {
          if (keycloak.authenticated) {
            return keycloak.accountManagement();
          }
        })
        .catch((error) => console.log(error));
    },
    gotoDataRequest() {
      this.$router.push("requests")
    },
    handleProfilePicError() {
      if (this.profilePictureSource !== defaultProfilePicture) {
        this.$emit("profilePictureLoadingError");
        this.profilePictureSource = defaultProfilePicture;
      }
    },
  },
  created() {
    assertDefined(this.getKeycloakPromise)()
      .then((keycloak) => {
        if (keycloak.authenticated && keycloak.idTokenParsed?.picture) {
          const profilePictureUrl = keycloak.idTokenParsed.picture as string;
          this.$emit("profilePictureObtained", profilePictureUrl);
          this.profilePictureSource = profilePictureUrl;
        }
      })
      .catch((error) => console.log(error));
  },
});
</script>

<style scoped>
.d-drop-down-toggle {
  cursor: pointer;
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
</style>
