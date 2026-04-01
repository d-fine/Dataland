<template>
  <div>Redirecting to login...</div>
</template>

<script setup lang="ts">
import { onMounted, inject } from 'vue';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import { loginAndRedirectToRedirectPage } from '@/utils/KeycloakUtils';
import { useRouter } from 'vue-router';

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const router = useRouter();

onMounted(async () => {
  const keycloak = await assertDefined(getKeycloakPromise)();
  if (keycloak.authenticated) {
    void router.replace({ path: '/platform-redirect' });
  } else {
    await loginAndRedirectToRedirectPage(keycloak);
  }
});
</script>
