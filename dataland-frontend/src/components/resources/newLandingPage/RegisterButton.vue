<template>
  <button class="quotes__button" @click="register">{{ buttonText }}</button>
</template>

<script setup lang="ts">
import { inject } from "vue";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { registerAndRedirectToSearchPage } from "@/utils/KeycloakUtils";
import type Keycloak from "keycloak-js";

const { buttonText } = defineProps<{ buttonText: string }>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>("getKeycloakPromise");

/**
 * Sends the user to the keycloak register page (if not authenticated already)
 */
const register = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (!keycloak.authenticated) {
        registerAndRedirectToSearchPage(keycloak);
      }
    })
    .catch((error) => console.log(error));
};
</script>
