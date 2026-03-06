<template>
  <PrimeDialog
    v-model:visible="isVisible"
    :header="header"
    modal
    :closable="false"
    :dismissable-mask="!isLoading"
    style="min-width: 20rem; text-align: center"
    data-test="confirmation-modal"
    @hide="handleCancel"
  >
    <div v-if="isSuccess" class="text-green-500 mb-3">
      <i class="pi pi-check-circle" style="font-size: 3rem"></i>
    </div>

    <div style="max-width: 30rem; margin: 8px auto 0; white-space: normal; text-align: left; word-break: break-word">
      <slot>
        {{ message }}
      </slot>
    </div>

    <div v-if="errorMessage" data-test="confirmation-modal-error-message">
      <Message severity="error" class="my-3" style="max-width: 30rem; text-align: left">
        {{ errorMessage }}
      </Message>
    </div>

    <!-- Hide footer when isSuccess = true -->
    <template #footer v-if="!isSuccess">
      <PrimeButton
        label="CANCEL"
        @click="handleCancel"
        variant="outlined"
        :disabled="isLoading"
        data-test="cancel-confirmation-modal-button"
      />
      <PrimeButton
        label="CONFIRM"
        @click="handleConfirm"
        :loading="isLoading"
        data-test="ok-confirmation-modal-button"
      />
    </template>
  </PrimeDialog>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import PrimeDialog from 'primevue/dialog';
import PrimeButton from 'primevue/button';
import Message from 'primevue/message';

const props = defineProps<{
  visible: boolean;
  header: string;
  message: string;
  errorMessage?: string;
  isLoading?: boolean;
  isSuccess?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'confirm'): void;
  (e: 'cancel'): void;
}>();

const isVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val),
});

const handleConfirm = (): void => {
  emit('confirm');
};

const handleCancel = (): void => {
  if (!props.isLoading) {
    emit('cancel');
    isVisible.value = false;
  }
};
</script>
