<template>
  <PrimeDialog
    :visible="visible"
    header="Success"
    modal
    :dismissable-mask="true"
    @update:visible="onUpdateVisible"
    style="min-width: 20rem; text-align: center"
    data-test="success-modal"
  >
    <div style="text-align: center; padding: 8px 0">
      <i class="pi pi-check-circle" style="font-size: 2rem; color: var(--p-green-500)"></i>
      <div style="margin-top: 8px">{{ props.message }}</div>
    </div>
    <template #footer>
      <PrimeButton label="OK" @click="emit('close')" data-test="close-success-modal-button" />
    </template>
  </PrimeDialog>
</template>

<script setup lang="ts">
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';

const props = defineProps<{ visible: boolean; message: string }>();
const emit = defineEmits(['close']);

/** Close the dialog when it is no longer visible
 */
function onUpdateVisible(newValue: boolean): void {
  if (!newValue) emit('close');
}
</script>
