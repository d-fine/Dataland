<template>
  <div class="col-12">
    <PrimeButton
      label="Login"
      class="uppercase p-button p-button-sm d-letters text-primary d-button justify-content-center bg-white-alpha-10 w-5rem ml-4"
      name="login_dataland_button"
      @click="login"
    />
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";

export default defineComponent({
  name: "UserAuthenticationButtons",
  components: { PrimeButton },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      authenticated: inject<boolean>("authenticated"),
    };
  },

  methods: {
    login() {
      this.getKeycloakPromise?.()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            const baseUrl = window.location.origin;
            const url = keycloak.createLoginUrl({
              redirectUri: `${baseUrl}/companies`,
            });
            location.assign(url);
          }
        })
        .catch((error) => console.log(error));
    },
  },
});
</script>
