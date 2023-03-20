<template>
  <router-view />
</template>

<script lang="ts">
import Keycloak from "keycloak-js";
import { computed, defineComponent } from "vue";

export default defineComponent({
  name: "app",
  data() {
    return {
      keycloakPromise: undefined as undefined | Promise<Keycloak>,
      keycloakAuthenticated: false,
      keycloakInitOptions: {
        realm: "datalandsecurity",
        url: "/keycloak",
        clientId: "dataland-public",
        onLoad: "login-required",
      },
    };
  },
  methods: {
    /**
     * Sets up the keycloak and keycloakPromise objects that are passed down
     * to the other components to handle authentication
     */
    initKeycloak() {
      const keycloak = new Keycloak(this.keycloakInitOptions);
      this.keycloakPromise = keycloak
        .init({
          onLoad: "check-sso",
          silentCheckSsoRedirectUri: window.location.origin + "/silent-check-sso.html",
          pkceMethod: "S256",
        })
        .then((authenticated) => {
          this.keycloakAuthenticated = authenticated;
        })
        .catch((error) => {
          console.log("Error in init keycloak ", error);
          this.keycloakAuthenticated = false;
        })
        .then((): Keycloak => {
          return keycloak;
        });
    },
  },

  created() {
    this.initKeycloak();
  },

  provide() {
    return {
      getKeycloakPromise: (): Promise<Keycloak> => {
        if (this.keycloakPromise) return this.keycloakPromise;
        throw new Error("The keycloak promise has not yet been initialised. This should not be possible...");
      },
      authenticated: computed(() => {
        return this.keycloakAuthenticated;
      }),
    };
  },
});
</script>
