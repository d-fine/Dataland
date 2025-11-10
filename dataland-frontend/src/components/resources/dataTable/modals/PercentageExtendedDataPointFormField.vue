<template>
  <h4>Value</h4>
  <InputNumber
    :placeholder="'Insert Value'"
    id="percentage"
    mode="decimal"
    suffix="%"
    :min="0"
    :max="100"
    fluid
    v-model="dataPointValue"
    :maxFractionDigits="2"
  />
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue';
import InputNumber from 'primevue/inputnumber';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import {
  buildApiBody,
  parseValue,
  type ExtendedDataPointType,
  type ExtendedDataPointMetaInfoType,
} from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();
const dataPointValue = ref<number | null>(parseValue(props.extendedDataPointObject.value));
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
