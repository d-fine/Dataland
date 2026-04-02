<template>
  <div>Redirecting...</div>
</template>

<script setup lang="ts">
import { onMounted, inject } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';

const { register } = defineProps<{ register?: boolean }>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const REDIRECT_KEY = register ? 'dataland_register_redirect_pending' : 'dataland_login_redirect_pending';
const platformRedirectUri = `${globalThis.location.origin}/platform-redirect`;
const astroHomeUri = `${globalThis.location.origin}/`;

onMounted(async () => {
  const keycloak = await assertDefined(getKeycloakPromise)();

  if (keycloak.authenticated) {
    globalThis.location.replace(platformRedirectUri);
    return;
  }

  const wasPending = sessionStorage.getItem(REDIRECT_KEY);
  if (wasPending) {
    sessionStorage.removeItem(REDIRECT_KEY);
    globalThis.location.replace(astroHomeUri);
    return;
  }

  sessionStorage.setItem(REDIRECT_KEY, 'true');
  const action = register
    ? keycloak.register({ redirectUri: platformRedirectUri })
    : keycloak.login({ redirectUri: platformRedirectUri });
  action.catch((error) => console.error(error));
});
</script>
