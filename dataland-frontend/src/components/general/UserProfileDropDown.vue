<template>
  <div
    @click="toggleDropdownMenu"
    class="flex align-items-center d-drop-down-toggle"
    name="profile-picture-dropdown-toggle"
  >
    <img
      ref="profile-picture"
      class="d-profile-picture"
      src="@/assets/images/elements/default_user_icon.svg"
      alt="User profile picture"
      referrerpolicy="no-referrer"
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

<script>
import PrimeMenu from "primevue/menu";
export default {
  name: "UserProfileDropDown",
  inject: ["authenticated", "getKeycloakInitPromise"],
  components: { PrimeMenu },

  data() {
    return {
      dropdownMenuItems: [
        {
          label: "USER SETTINGS",
          icon: "settings",
          clickAction: this.gotoUserSettings,
        },
        {
          label: "LOG OUT",
          icon: "logout",
          id: "profile-picture-dropdown-toggle",
          clickAction: this.logoutViaDropdown,
        },
      ],
    };
  },

  methods: {
    toggleDropdownMenu(event) {
      this.$refs.menu.toggle(event);
    },
    logoutViaDropdown() {
      this.getKeycloakInitPromise()
        .then((keycloak) => {
          if (keycloak.authenticated) {
            keycloak.logout({ redirectUri: "/" });
          }
        })
        .catch((error) => console.log("error: " + error));
    },
    gotoUserSettings() {
      this.getKeycloakInitPromise()
        .then((keycloak) => {
          if (keycloak.authenticated) {
            keycloak.accountManagement();
          }
        })
        .catch((error) => console.log("error: " + error));
    },
  },
  created() {
    this.getKeycloakInitPromise()
      .then((keycloak) => {
        if (keycloak.authenticated && keycloak.idTokenParsed.picture) {
          this.$refs["profile-picture"].src = keycloak.idTokenParsed.picture;
        }
      })
      .catch((error) => console.log("error: " + error));
  },
};
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
