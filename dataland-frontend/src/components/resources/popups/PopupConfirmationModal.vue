<template>
  <PrimeDialog
    v-model:visible="isVisible"
    :header="header"
    modal
    :closable="false"
    :dismissable-mask="!isLoading"
    data-test="confirmation-modal"
    class="w-full"
  >
    <div class="flex flex-column align-items-center">
      <div v-if="isSuccess" class="mb-3 text-green-500">
        <i class="pi pi-check-circle text-4xl"></i>
      </div>

      <div class="w-full" style="max-width: 30rem">
        <slot>
          <p class="m-0 mt-2 text-left white-space-pre-line">
            {{ message }}
          </p>
        </slot>
      </div>

      <div
        v-if="errorMessage"
        class="w-full mt-3"
        style="max-width: 30rem"
        data-test="confirmation-modal-error-message"
      >
        <Message severity="error" class="w-full text-left">
          {{ errorMessage }}
        </Message>
      </div>
    </div>

    <!-- Hide footer when isSuccess = true -->
    <template #footer v-if="!isSuccess">
      <PrimeButton
        v-if="showCancelButton"
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
  showCancelButton?: boolean;
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
