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
      {{ creditsBalance.value }}
    </template>
  </Card>
</template>

<script setup lang="ts">
import Card from 'primevue/card';
import Divider from 'primevue/divider';
import {defineProps, inject, onMounted, ref} from 'vue';
import type Keycloak from "keycloak-js";
import {ApiClientProvider} from "@/services/ApiClients.ts";
import {assertDefined} from "@/utils/TypeScriptUtils.ts";

const creditsBalance = ref<number | any>(0);
const props = defineProps<{
  companyId: string;
}>();


onMounted(async () => {
  await getCreditsBalanceForCompany();
  console.log(creditsBalance.value);
});

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

/**
 * Gets the current balance of credits for the company.
 */
async function getCreditsBalanceForCompany(): Promise<void> {
  if (!props.companyId) return;
  try {
  creditsBalance.value = apiClientProvider.apiClients.creditsController.getBalance(props.companyId);
  } catch (error) {
    console.error("Failed to get credit balance:", error);
  }
}

</script>

<style scoped></style>
