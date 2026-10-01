<template>
  <h4>Value</h4>
  <InputNumber
    data-test="big-decimal-input"
    placeholder="Insert Value"
    fluid
    v-model="dataPointValue"
    :maxFractionDigits="2"
    locale="en-US"
  />
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
  />
</template>

<script setup lang="ts">
import InputNumber from 'primevue/inputnumber';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import { useNumericExtendedDataPointFormField } from '@/components/resources/dataTable/composables/useNumericExtendedDataPointFormField.ts';
import type { ExtendedDataPointType } from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

const { dataPointValue, selectedDocumentMeta, extendedDialogRef, isPageValid, buildApiBodyWithExtendedInfo } =
  useNumericExtendedDataPointFormField(props.extendedDataPointObject);

defineExpose({ buildApiBodyWithExtendedInfo, isPageValid });
</script>
