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
      <div style="margin-top: 8px; white-space: pre-line; text-align: left">
        Are you sure you want to remove this portfolio? You will no longer have access <br />
        to this portfolio unless the creator shares it with you again.
      </div>
      <div
        v-if="errorMessage"
        style="margin-top: 12px; color: var(--red); text-align: left"
        data-test="remove-error-message"
      >
        {{ errorMessage }}
      </div>
    </div>
    <template #footer>
      <PrimeButton
        label="CANCEL"
        variant="outlined"
        :disabled="isRemoving"
        @click="emit('close')"
        data-test="remove-cancel-button"
      />
      <PrimeButton
        label="REMOVE PORTFOLIO"
        :loading="isRemoving"
        :disabled="isRemoving"
        @click="removePortfolio"
        data-test="remove-confirmation-button"
      />
    </template>
  </PrimeDialog>
</template>

<script setup lang="ts">
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import { useDeleteCurrentUserFromSharingMutation } from '@/api-queries/user-service/portfolio/useDeleteCurrentUserFromSharingMutation';
import { ref } from 'vue';

const props = defineProps<{ visible: boolean; portfolioId: string }>();
const emit = defineEmits(['close', 'sharing-removed']);

const { mutate: deleteCurrentUserFromSharing, isPending: isRemoving } = useDeleteCurrentUserFromSharingMutation();
const errorMessage = ref<string | null>(null);

/**
 * Close the dialog when it is no longer visible (e.g. mask click / ESC)
 */
function onUpdateVisible(newValue: boolean): void {
  if (!newValue) {
    errorMessage.value = null;
    emit('close');
  }
}

/**
 * Remove the current user from sharing of the portfolio.
 * The mutation refreshes the list of shared portfolios.
 */
function removePortfolio(): void {
  if (isRemoving.value) return;

  errorMessage.value = null;
  deleteCurrentUserFromSharing(
    { portfolioId: props.portfolioId },
    {
      onSuccess: () => emit('sharing-removed'),
      onError: () => {
        errorMessage.value = 'Something went wrong while removing access. Please try again.';
      },
    }
  );
}
</script>
