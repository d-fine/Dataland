<template>
  <h4>Value</h4>
  <InputNumber
      data-test="big-decimal-input"
      placeholder="Insert Value"
      fluid
      v-model="value"
      :maxFractionDigits="2"
      locale="en-US"
  />
  <ExtendedDataPointFormFieldDialog
      v-model:chosenQuality="chosenQuality"
      v-model:selectedDocument="selectedDocument"
      v-model:insertedComment="insertedComment"
      v-model:selectedDocumentMeta="selectedDocumentMeta"
      v-model:insertedPage="insertedPage"
  />
</template>

<script setup lang="ts">
import {ref, inject, watch, watchEffect} from 'vue';
import InputNumber from 'primevue/inputnumber';
import ExtendedDataPointFormFieldDialog
  from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import type {DocumentMetaInfoResponse} from '@clients/documentmanager';
import {buildApiBody} from '@/components/resources/dataTable/conversion/Utils.ts';


const props = defineProps({
  value: [String, Number, null],
  chosenQuality: String,
  selectedDocument: String,
  insertedComment: String,
  insertedPage: String,
});

function parseValue(val: string | number | null | undefined): number | null {
  if (typeof val === 'number') return val;
  if (typeof val === 'string') {
    const match = val.replace(/,/g, '').match(/-?\d+(\.\d+)?/);
    return match ? parseFloat(match[0]) : null;
  }
  return null;
}

const emit = defineEmits(['update:apiBody']);

const value = ref<number | null>(parseValue(props.value));
const chosenQuality = ref<string | null>(props.chosenQuality ?? null);
const selectedDocument = ref<string | null>(props.selectedDocument ?? null);
const insertedComment = ref<string | null>(props.insertedComment ?? null);
const insertedPage = ref<string | null>(props.insertedPage ?? null);
const apiBody = ref({});
const companyId = inject<string>('companyId');
const reportingPeriod = inject<string>('reportingPeriod');
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | null>(null);
const dataPointTypeId = inject<string>('dataPointTypeId');

watch(() => props.value, (newVal) => {
  value.value = parseValue(newVal);
});

watchEffect(() => {
  apiBody.value = buildApiBody(
      value.value,
      chosenQuality.value,
      selectedDocument.value,
      insertedComment.value,
      insertedPage.value,
      selectedDocumentMeta.value,
      companyId!,
      reportingPeriod!,
      dataPointTypeId!
  );
  emit('update:apiBody', apiBody.value);
});


</script>

<style scoped></style>
