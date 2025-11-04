<template>
  <h4>Value</h4>
  <InputNumber placeholder="Insert Value" fluid v-model="value" @input="emitValue" />
  <ExtendedDataPointFormFieldDialog />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import InputNumber from 'primevue/inputtext';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';

const emit = defineEmits(['update:value']);
const value = ref<number | null>(null);

/**
 * Emits an event to notify listeners of a value update.
 */
function emitValue(): void {
  emit('update:value', value.value);
}
</script>

<style scoped></style>
