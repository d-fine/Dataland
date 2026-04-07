<template>
  <div>Redirecting...</div>
</template>

<script setup lang="ts">
import { onMounted, inject } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';

const { register } = defineProps<{ register?: boolean }>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

onMounted(async () => {
  const redirectKey = register ? 'dataland_register_redirect_pending' : 'dataland_login_redirect_pending';
  const platformRedirectUri = `${globalThis.location.origin}/platform-redirect`;
  const astroHomeUri = `${globalThis.location.origin}/`;

  const keycloak = await assertDefined(getKeycloakPromise)();

  if (keycloak.authenticated) {
    globalThis.location.replace(platformRedirectUri);
    return;
  }

  const wasPending = sessionStorage.getItem(redirectKey);
  if (wasPending) {
    sessionStorage.removeItem(redirectKey);
    globalThis.location.replace(astroHomeUri);
    return;
  }

  // First visit to /login or /register — redirect to Keycloak
  sessionStorage.setItem(redirectKey, 'true');
  const action = register
    ? keycloak.register({ redirectUri: platformRedirectUri })
    : keycloak.login({ redirectUri: platformRedirectUri });
  action.catch((error) => console.error(error));
});
</script>
