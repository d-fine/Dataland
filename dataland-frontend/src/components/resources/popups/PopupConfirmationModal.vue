<template>
  <PrimeDialog
    v-model:visible="isVisible"
    :header="header"
    modal
    :dismissable-mask="props.dismissableMask ?? !isLoading"
    :closable="true"
    @hide="handleCancel"
    data-test="confirmation-modal"
    :style="{ width: '30rem' }"
    :breakpoints="{ '1199px': '75vw', '575px': '90vw' }"
  >
    <div class="flex flex-column gap-3 pt-2">
      <div v-if="isSuccess" class="flex justify-content-center text-green-500 mb-2">
        <i class="pi pi-check-circle text-5xl"></i>
      </div>

      <slot>
        <p class="m-0 text-left white-space-pre-line line-height-3 text-color-secondary">
          {{ message }}
        </p>
      </slot>

      <Message
        v-if="errorMessage"
        severity="error"
        :closable="false"
        class="m-0 mt-2"
        data-test="confirmation-modal-error-message"
      >
        {{ errorMessage }}
      </Message>
    </div>

    <template #footer v-if="!isSuccess">
      <div class="flex justify-content-end gap-2 w-full mt-2">
        <PrimeButton
          label="CANCEL"
          @click="handleCancel"
          severity="secondary"
          outlined
          :disabled="isLoading"
          data-test="cancel-confirmation-modal-button"
        />
        <PrimeButton
          label="CONFIRM"
          @click="handleConfirm"
          :loading="isLoading"
          data-test="ok-confirmation-modal-button"
        />
      </div>
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
  dismissableMask?: boolean;
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
