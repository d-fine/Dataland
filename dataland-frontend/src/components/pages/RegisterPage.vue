<template>
  <div>Redirecting to registration...</div>
</template>

<script setup lang="ts">
import { onMounted, inject } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import { registerAndRedirectToRedirectPage } from '@/utils/KeycloakUtils';
import { useRouter } from 'vue-router';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const router = useRouter();

onMounted(async () => {
  const keycloak = await assertDefined(getKeycloakPromise)();
  if (keycloak.authenticated) {
    void router.replace({ path: '/platform-redirect' });
  } else {
    await registerAndRedirectToRedirectPage(keycloak);
  }
});
</script>
