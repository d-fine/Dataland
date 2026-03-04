<template>
  <PrimeDialog
    v-model:visible="isVisible"
    :header="header"
    modal
    :closable="false"
    :dismissable-mask="!isLoading"
    style="min-width: 20rem; text-align: center"
    @hide="handleCancel"
  >
    <div v-if="errorMessage" data-test="confirmation-modal-error-message">
      <Message severity="error" class="my-3" style="max-width: 30rem; text-align: left">{{ errorMessage }}</Message>
    </div>
    <slot> {{ message }} </slot>
    <template #footer>
      <PrimeButton label="CANCEL" @click="handleCancel" variant="outlined" :disabled="isLoading" />
      <PrimeButton label="CONFIRM" @click="handleConfirm" :loading="isLoading" />
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

const handleConfirm = () => {
  emit('confirm');
};

const handleCancel = () => {
  if (!props.isLoading) {
    emit('cancel');
    isVisible.value = false;
  }
};
</script>
