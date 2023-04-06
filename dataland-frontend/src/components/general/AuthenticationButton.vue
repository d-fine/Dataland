<template>
  <PrimeButton
    label="Login to preview account"
    :class="customClassForButton"
    name="login_dataland_button"
    @click="login"
  />
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { loginAndRedirectToSearchPage } from "@/utils/KeycloakUtils";

export default defineComponent({
  name: "AuthenticationButton",
  components: { PrimeButton },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      authenticated: inject<boolean>("authenticated"),
    };
  },

  props: {
    customClassForButton: {
      type: String,
      default: "p-button-sm uppercase d-letters text-primary bg-white-alpha-10 w-15rem",
    },
  },

  methods: {
    /**
     * Sends the user to the keycloak login page (if not authenticated already)
     */
    login() {
      assertDefined(this.getKeycloakPromise)()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            loginAndRedirectToSearchPage(keycloak);
          }
        })
        .catch((error) => console.log(error));
    },
  },
});
</script>
