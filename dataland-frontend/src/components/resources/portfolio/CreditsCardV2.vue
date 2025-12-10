<template>
  <Card
      style="width: 70%; margin: 0 auto; margin-top: var(--spacing-xl); margin-bottom: var(--spacing-xl)"
      data-test="creditsBalance"
  >
    <template #title>Credits</template>
    <template #content>
      <Divider />
      Current Amount of Credits:
      <div class="dataland-info-text small">{{ props.companyId }}</div>

      <span v-if="isLoading">Loading...</span>

      <span v-else-if="isError" style="color: red">Failed to load credits</span>

      <span v-else>{{ data }}</span>
    </template>
  </Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card';
import Divider from 'primevue/divider';
import { defineProps, inject } from 'vue';
import { useQuery } from '@tanstack/vue-query'; // Import the hook
import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients.ts";
import { assertDefined } from "@/utils/TypeScriptUtils.ts";

const props = defineProps<{
  companyId: string;
}>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');


const { data, isLoading, isError } = useQuery({

  queryKey: ['credits', props.companyId],

  queryFn: async () => {
    if (!props.companyId) return 0;

    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

    return await apiClientProvider.apiClients.creditsController.getBalance(props.companyId);
  },
  staleTime: 1000 * 60,
});
</script>