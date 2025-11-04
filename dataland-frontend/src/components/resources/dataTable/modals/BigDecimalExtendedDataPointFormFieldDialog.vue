<template>
  <h4>Value</h4>
  <InputNumber placeholder="Insert Value" fluid v-model="value" />
  <ExtendedDataPointFormFieldDialog
    v-model:chosenQuality="chosenQuality"
    v-model:selectedDocument="selectedDocument"
    v-model:insertedComment="insertedComment"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    v-model:insertedPage="insertedPage"
  />
</template>

<script setup lang="ts">
import { ref, inject, watchEffect } from 'vue';
import InputNumber from 'primevue/inputnumber';
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import { buildApiBody } from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps({
  value: Number,
  chosenQuality: String,
  selectedDocument: String,
  insertedComment: String,
  insertedPage: String,
});

const emit = defineEmits(['update:apiBody']);

const value = ref<number | null>(props.value ?? null);
const chosenQuality = ref<string | null>(props.chosenQuality ?? null);
const selectedDocument = ref<string | null>(props.selectedDocument ?? null);
const insertedComment = ref<string | null>(props.insertedComment ?? null);
const insertedPage = ref<string | null>(props.insertedPage ?? null);
const apiBody = ref({});
const companyId = inject<string>('companyId');
const reportingPeriod = inject<string>('reportingPeriod');
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | null>(null);

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
  );
  emit('update:apiBody', apiBody.value);
});
</script>

<style scoped></style>
