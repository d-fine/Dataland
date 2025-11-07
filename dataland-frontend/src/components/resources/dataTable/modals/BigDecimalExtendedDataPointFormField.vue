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
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    :extendedDataPointObject="props.extendedDataPointObject"
  />
</template>

<script setup lang="ts">
import { inject, ref, watch, watchEffect } from 'vue';
import InputNumber from 'primevue/inputnumber';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import {buildApiBody, parseValue, type DataPointObject } from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps<{
  extendedDataPointObject: DataPointObject;
  reportingPeriod: string;
  dataPointTypeId: string;
}>();

const emit = defineEmits(['update:apiBody']);

const dataPointValue = ref<number | null>(parseValue(props.extendedDataPointObject.value));
const companyId = inject<string>('companyId');
const reportingPeriod = ref<string>(props.reportingPeriod!);
const dataPointTypeId = ref<string>(props.dataPointTypeId!);
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | undefined>(undefined);
const apiBody = ref({});

watch(
  () => dataPointValue.value,
  (newVal) => {
    props.extendedDataPointObject.value?.value = parseValue(newVal);
  }
);

watchEffect(() => {
  apiBody.value = buildApiBody(
    props.extendedDataPointObject,
    selectedDocumentMeta.value,
    companyId!,
    reportingPeriod.value,
    dataPointTypeId.value,
  );
  emit('update:apiBody', apiBody.value);
});
</script>
