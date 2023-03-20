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
      resolvedKeycloakPromise: undefined as undefined | Keycloak, // TODO
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
    initKeycloak(): Promise<Keycloak> {
      const keycloak = new Keycloak(this.keycloakInitOptions);
      return keycloak
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

  async created() {
    this.keycloakPromise = this.initKeycloak();
    const resolvedKeycloakPromise = await this.keycloakPromise
    if(resolvedKeycloakPromise) {
      setInterval(() => {
        console.log("run")
        resolvedKeycloakPromise.updateToken(5).then((refreshed) => {
          if (refreshed) {
            console.log('Token refreshed' + refreshed);
          } else {
            console.log('Token not refreshed, valid for ')
             //   + Math.round(resolvedKeycloakPromise.tokenParsed.exp + resolvedKeycloakPromise.timeSkew - new Date().getTime() / 1000) + ' seconds');
          }
        }).catch(() => {
          console.error('Failed to refresh token');
        });
      }, 6000)
    }
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
