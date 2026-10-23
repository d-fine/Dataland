<template>
  <h4>Value</h4>
  <DatePicker
    v-model="dataPointValue"
    dateFormat="yy-mm-dd"
    placeholder="Select date"
    showIcon
    showButtonBar
    fluid
    data-test="date-select"
  />
  <ExtendedDataPointFormFieldDialog
    ref="extendedDialogRef"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import DatePicker from 'primevue/datepicker';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import { getHyphenatedDate } from '@/utils/DataFormatUtils.ts';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import {
  buildApiBody,
  type ExtendedDataPointMetaInfoType,
  type ExtendedDataPointType,
} from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps<{
  extendedDataPointObject: ExtendedDataPointType;
}>();

/**
 * @param isoDate a date string in the format yyyy-mm-dd
 * @returns the date at local midnight, avoiding the UTC shift of new Date(isoDate)
 */
function parseIsoDateAsLocalDate(isoDate: string): Date {
  const [year, month, day] = isoDate.split('-').map(Number);
  return new Date(year!, month! - 1, day);
}

const isoDatePattern = /^\d{4}-\d{2}-\d{2}$/;
const initialValue = props.extendedDataPointObject.value;
const dataPointValue = ref<Date | null>(
  typeof initialValue === 'string' && isoDatePattern.test(initialValue.trim())
    ? parseIsoDateAsLocalDate(initialValue.trim())
    : null
);
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
const extendedDialogRef = ref<{ getFormData: () => ExtendedDataPointMetaInfoType; isPageValid: boolean }>();

const isPageValid = computed<boolean>(() => extendedDialogRef.value?.isPageValid ?? true);

/**
 * @returns the JSON-stringified API body including the extended meta info
 */
function buildApiBodyWithExtendedInfo(): string {
  return buildApiBody(
    dataPointValue.value ? getHyphenatedDate(dataPointValue.value) : null,
    undefined,
    extendedDialogRef.value?.getFormData()
  );
}

defineExpose({ buildApiBodyWithExtendedInfo, isPageValid });
</script>
