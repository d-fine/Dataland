<template>
  <PrimeDialog
    :visible="props.visible"
    header="Remove Portfolio Access?"
    modal
    :dismissable-mask="true"
    @update:visible="onUpdateVisible"
    style="min-width: 20rem; text-align: center"
    data-test="remove-sharing-modal"
  >
    <div style="text-align: center; padding: 8px 0">
      <div style="margin-top: 8px; white-space: pre-line">
        Are you sure you want to remove this portfolio? You will no longer have access to this portfolio unless the
        creator shares it with you again.
      </div>
    </div>
    <template #footer>
      <PrimeButton label="CANCEL" variant="outlined" @click="emit('close')" data-test="remove-cancel-button" />
      <PrimeButton label="REMOVE PORTFOLIO" @click="removePortfolio" data-test="remove-confirmation-button" />
    </template>
  </PrimeDialog>
</template>

<script setup lang="ts">
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import { inject } from 'vue';
import type Keycloak from 'keycloak-js';

const props = defineProps<{ visible: boolean; portfolioId: string }>();
const emit = defineEmits(['close']);
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

/**
 * Close the dialog when it is no longer visible
 */
function onUpdateVisible(newValue: boolean): void {
  if (!newValue) emit('close');
}

/**
 * Remove the current user from sharing of the portfolio
 */
async function removePortfolio(): Promise<void> {
  await apiClientProvider.apiClients.portfolioController.deleteCurrentUserFromSharing(props.portfolioId);
  emit('close');
}
</script>
