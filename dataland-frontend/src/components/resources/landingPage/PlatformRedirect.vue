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
  const apiClientProvider = new ApiClientProvider(Promise.resolve(keycloak));
  const userPortfoliosResponse =
    await apiClientProvider.apiClients.portfolioController.getAllPortfolioNamesForCurrentUser();
  const userPortfolios = userPortfoliosResponse.data;
  if (userPortfolios.length > 0) {
    void router.replace({ path: '/portfolios' });
  } else {
    void router.replace({ path: '/companies' });
  }
});
</script>
