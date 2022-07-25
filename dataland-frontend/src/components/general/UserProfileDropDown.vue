<template>
  <div
    class="max-w-full max-h-full flex justify-content-center d-drop-down-toggle"
    id="profile-picture-dropdown-toggle"
    @click="toggleDropdownMenu"
  >
    <img
      ref="profile-picture"
      class="d-profile-picture"
      src="@/assets/images/logos/favicon-32x32.png"
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
    class="text-primary"
  >
    <template #item="{ item }">
      <a class="p-menuitem-link" role="menuitem" tabindex="0" @click="logoutViaDropdown" :id="item.id"
        ><span class="p-menuitem-icon text-primary" :class="item.icon"></span
        ><span class="p-menuitem-text text-primary">{{ item.label }}</span></a
      >
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
          label: "Logout",
          icon: "pi pi-sign-out",
          id: "profile-picture-dropdown-toggle",
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
  height: 2rem;
}

.d-triangle-down {
  height: 0.5rem;
  margin-top: 1rem;
  margin-left: 0.5rem;
}
</style>
