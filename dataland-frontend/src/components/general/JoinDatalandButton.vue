<template>
  <div class="col-12">
    <PrimeButton
      class="uppercase p-button pl-1 pr-1 pb-1 pt-1 justify-content-center h-3rem w-full"
      name="join_dataland_button"
      @click="register"
    >
      <span class="d-letters"> Create a preview account </span>
      <i class="material-icons pl-1" aria-hidden="true" alt="chevron_right">chevron_right</i>
    </PrimeButton>
  </div>
</template>

<script lang="ts">
import PrimeButton from "primevue/button";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  name: "JoinDatalandButton",
  components: { PrimeButton },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      authenticated: inject<boolean>("authenticated"),
    };
  },
  methods: {
    register() {
      assertDefined(this.getKeycloakPromise)()
        .then((keycloak) => {
          if (!keycloak.authenticated) {
            const baseUrl = window.location.origin;
            const url = keycloak.createRegisterUrl({
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
