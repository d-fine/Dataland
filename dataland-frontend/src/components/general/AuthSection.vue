<template>
  <div class="header__authsection">
    <Button label="Log in" data-test="login-dataland-button" @click="login" variant="outlined" />
    <Button label="Try it free" data-test="signup-dataland-button" @click="register" />
  </div>
</template>

<script setup lang="ts">
import { inject } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import Button from 'primevue/button';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

/**
 * Sends the user to the keycloak login page (if not authenticated already)
 */
const login = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (keycloak.authenticated) return;
      keycloak.login().catch((error) => console.error(error));
    })
    .catch((error) => console.error(error));
};

/**
 * Sends the user to the keycloak register page (if not authenticated) and logs in otherwise
 */
const register = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => {
      if (keycloak.authenticated) return;
      keycloak.register().catch((error) => console.error(error));
    })
    .catch((error) => console.error(error));
};
</script>
<style scoped lang="scss">
@media only screen and (max-width: $bp-md) {
  .header {
    background-color: var(--p-pink-300);
    padding: 16px;
    margin: 0;
    width: 100%;
    border-radius: 0;
  }
}
</style>
