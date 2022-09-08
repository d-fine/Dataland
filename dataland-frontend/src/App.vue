<template>
  <router-view />
  <DatalandFooter />
</template>

<script>
import Keycloak from "keycloak-js";
import { computed } from "vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";

export default {
  name: "app",
  components: { DatalandFooter },
  data() {
    return {
      keycloak: null,
      keycloakPromise: null,
      keycloakPromiseFinished: true,
      keycloakAuthenticated: null,
    };
  },
  methods: {
    init_keycloak() {
      if (this.keycloakPromiseFinished) {
        this.keycloakPromise = this.keycloak
          .init({
            onLoad: "check-sso",
            silentCheckSsoRedirectUri: window.location.origin + "/silent-check-sso.html",
          })
          .then((authenticated) => {
            this.keycloakAuthenticated = authenticated;
            return authenticated;
          })
          .catch((error) => {
            console.log("Error in init keycloak ", error);
            this.keycloakAuthenticated = false;
          })
          .then(() => {
            return this.keycloak;
          });
        this.keycloakPromise.finally(() => {
          this.keycloakPromiseFinished = true;
        });
        this.keycloakPromiseFinished = false;
      }
    },
  },
  provide() {
    return {
      getKeycloakPromise: () => {
        return this.keycloakPromise;
      },
      authenticated: computed(() => {
        return this.keycloakAuthenticated;
      }),
    };
  },
  created() {
    const initOptions = {
      realm: "datalandsecurity",
      url: "/keycloak",
      clientId: "dataland-public",
      onLoad: "login-required",
    };
    this.keycloak = new Keycloak(initOptions);
    this.init_keycloak();
  },
};
</script>

<style lang="scss">
@import "./assets/css/main.css";
@import "./assets/css/variables";

body {
  margin: unset;
}
</style>
