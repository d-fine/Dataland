<template>
  <h4>Value</h4>
  <div style="gap: var(--spacing-xs); display: flex">
    <RadioButton v-model="dataPointValue" :inputId="'yes-no-yes'" :value="'Yes'" data-test="yes-input" />
    <label for="yes-no-yes">Yes</label>
    <RadioButton v-model="dataPointValue" :inputId="'yes-no-no'" :value="'No'" data-test="no-input" />
    <label for="yes-no-no">No</label>
  </div>
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
  />
</template>

<script setup lang="ts">
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import { ref } from 'vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import RadioButton from 'primevue/radiobutton';
import {
  buildApiBody,
  type ExtendedDataPointType,
  type ExtendedDataPointMetaInfoType,
} from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

const dataPointValue = ref<string | null>(
  props.extendedDataPointObject.value !== null && props.extendedDataPointObject.value !== undefined
    ? String(props.extendedDataPointObject.value)
    : null
);
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
const extendedDialogRef = ref<{ getFormData: () => ExtendedDataPointMetaInfoType }>();

/**
 * Reference to the extended dialog to get form data
 */
function buildApiBodyWithExtendedInfo(): string {
  return buildApiBody(dataPointValue.value, undefined, extendedDialogRef.value?.getFormData());
}
defineExpose({ buildApiBodyWithExtendedInfo });
</script>
