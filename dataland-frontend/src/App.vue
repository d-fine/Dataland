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
      keycloak: undefined,
      keycloakPromise: undefined,
      keycloakAuthenticated: false,
    };
  },
  methods: {
    initKeycloak() {
      const initOptions = {
        realm: "datalandsecurity",
        url: "/keycloak",
        clientId: "dataland-public",
        onLoad: "login-required",
      };
      const keycloak = new Keycloak(initOptions);
      const keycloakPromise = keycloak
        .init({
          onLoad: "check-sso",
          silentCheckSsoRedirectUri: window.location.origin + "/silent-check-sso.html",
          pkceMethod: "S256",
        })
        .then((authenticated): boolean => {
          this.keycloakAuthenticated = authenticated;
          return authenticated;
        })
        .catch((error) => {
          console.log("Error in init keycloak ", error);
          this.keycloakAuthenticated = false;
        })
        .then((): Keycloak => {
          return keycloak;
        });

      this.keycloak = keycloak;
      this.keycloakPromise = keycloakPromise;
    },
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
  created() {
    this.initKeycloak();
  },
});
</script>
