<template>
  <div>Redirecting...</div>
</template>

<script setup lang="ts">
import { onMounted, inject } from 'vue';
import { useRouter } from 'vue-router';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients.ts';

const router = useRouter();
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

onMounted(async () => {
  const keycloak = await assertDefined(getKeycloakPromise)();
  sessionStorage.removeItem('dataland_login_redirect_pending');
  sessionStorage.removeItem('dataland_register_redirect_pending');
  if (!keycloak.authenticated) {
    router.replace({ path: '/' }).catch((error) => console.error(error));
    return;
  }
  const apiClientProvider = new ApiClientProvider(Promise.resolve(keycloak));
  const userPortfoliosResponse =
    await apiClientProvider.apiClients.portfolioController.getAllPortfolioNamesForCurrentUser();
  const userPortfolios = userPortfoliosResponse.data;
  if (userPortfolios.length > 0) {
    router.replace({ path: '/portfolios' }).catch((error) => console.error(error));
  } else {
    router.replace({ path: '/companies' }).catch((error) => console.error(error));
  }
});
</script>
