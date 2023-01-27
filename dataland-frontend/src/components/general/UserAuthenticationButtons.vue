<template>
  <div class="col-12">
    <PrimeButton
      label="Login to preview account"
      class="p-button-sm uppercase d-letters text-primary bg-white-alpha-10 w-15rem"
      name="login_dataland_button"
      @click="login"
    />
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";

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
    /**
     * Sends the user to the keycloak login page (if not authenticated already)
     */
    login() {
      assertDefined(this.getKeycloakPromise)()
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
