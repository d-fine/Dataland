<template>
  <PrimeButton
    class="uppercase p-button pl-1 pr-1 pb-1 pt-1 justify-content-center h-3rem w-full"
    name="join_dataland_button"
    @click="register"
  >
    <span class="d-letters"> Create an account </span>
    <i class="material-icons pl-1" aria-hidden="true" alt="chevron_right">chevron_right</i>
  </PrimeButton>
</template>

<script lang="ts">
import PrimeButton from 'primevue/button';
import { defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';

export default defineComponent({
  name: 'JoinDatalandButton',
  components: { PrimeButton },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  methods: {
    /**
     * Redirects the user to the dataland registration page
     */
    register() {
      assertDefined(this.getKeycloakPromise)()
        .then(async (keycloak) => {
          if (!keycloak.authenticated) {
            const baseUrl = window.location.origin;
            const url = await keycloak.createRegisterUrl({
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
