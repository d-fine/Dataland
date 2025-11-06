<template>
  <h4>Value</h4>
  <InputNumber id="percentage" mode="decimal" suffix="%" :min="0" :max="100" fluid v-model="value" />
  <ExtendedDataPointFormFieldDialog
    v-model:chosenQuality="chosenQuality"
    v-model:selectedDocument="selectedDocument"
    v-model:insertedComment="insertedComment"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    v-model:insertedPage="insertedPage"
  />
</template>

<script setup lang="ts">
import { ref, watchEffect, watch, inject } from 'vue';
import InputNumber from 'primevue/inputnumber';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import { buildApiBody, parseValue } from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps({
  value: [String, Number, null],
  chosenQuality: String,
  selectedDocument: String,
  insertedComment: String,
  insertedPage: String,
  reportingPeriod: String,
  dataPointTypeId: String,
});

const emit = defineEmits(['update:apiBody']);

const value = ref<number | null>(parseValue(props.value));
const chosenQuality = ref<string | null>(props.chosenQuality ?? null);
const selectedDocument = ref<string | null>(props.selectedDocument ?? null);
const insertedComment = ref<string | null>(props.insertedComment ?? null);
const insertedPage = ref<string | null>(props.insertedPage ?? null);
const apiBody = ref({});
const companyId = inject<string>('companyId');
const reportingPeriod = ref<string>(props.reportingPeriod!);
const dataPointTypeId = ref<string>(props.dataPointTypeId!);
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | null>(null);

watch(
  () => props.value,
  (newVal) => {
    value.value = parseValue(newVal);
  }
);

watchEffect(() => {
  apiBody.value = buildApiBody(
    value.value,
    chosenQuality.value,
    selectedDocument.value,
    insertedComment.value,
    insertedPage.value,
    selectedDocumentMeta.value,
    companyId!,
    reportingPeriod.value,
    dataPointTypeId.value
  );
  emit('update:apiBody', apiBody.value);
});
</script>
