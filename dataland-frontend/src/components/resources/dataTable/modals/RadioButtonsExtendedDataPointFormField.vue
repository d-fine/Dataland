<template>
  <h4>Value</h4>
  <SelectButton
    v-model="dataPointValue"
    :options="options"
    optionLabel="label"
    optionValue="value"
    :allowEmpty="true"
    data-test="radio-buttons-select"
  />
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import SelectButton from 'primevue/selectbutton';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import {
  buildApiBody,
  type ExtendedDataPointMetaInfoType,
  type ExtendedDataPointType,
} from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

// Currently only used for the fiscal year deviation data point.
const options = [
  { label: 'Deviation', value: 'Deviation' },
  { label: 'No Deviation', value: 'NoDeviation' },
];

const initialValue = props.extendedDataPointObject.value;
const dataPointValue = ref<string | null>(
  typeof initialValue === 'string'
    ? (options.find((option) => option.value === initialValue || option.label === initialValue)?.value ?? null)
    : null
);
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
const extendedDialogRef = ref<{ getFormData: () => ExtendedDataPointMetaInfoType; isPageValid: boolean }>();

const isPageValid = computed<boolean>(() => extendedDialogRef.value?.isPageValid ?? true);

/**
 * @returns the JSON-stringified API body including the extended meta info
 */
function buildApiBodyWithExtendedInfo(): string {
  return buildApiBody(dataPointValue.value, undefined, extendedDialogRef.value?.getFormData());
}

defineExpose({ buildApiBodyWithExtendedInfo, isPageValid });
</script>
