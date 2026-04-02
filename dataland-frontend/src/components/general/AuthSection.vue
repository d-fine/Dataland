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

const login = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => keycloak.login({ redirectUri: `${globalThis.location.origin}/platform-redirect` }))
    .catch((error) => console.error(error));
};

const register = (): void => {
  assertDefined(getKeycloakPromise)()
    .then((keycloak) => keycloak.register({ redirectUri: `${globalThis.location.origin}/platform-redirect` }))
    .catch((error) => console.error(error));
};
</script>
