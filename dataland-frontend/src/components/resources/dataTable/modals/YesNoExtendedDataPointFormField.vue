<template>
  <h4>Value</h4>
  <div style="gap: 0.5rem; display: flex">
    <RadioButton v-model="value" :inputId="'yes-no-yes'" :value="'Yes'" data-test="yes-input" />
    <label for="yes-no-yes">Yes</label>
    <RadioButton v-model="value" :inputId="'yes-no-no'" :value="'No'" data-test="no-input" />
    <label for="yes-no-no">No</label>
  </div>
  <ExtendedDataPointFormFieldDialog
    v-model:chosenQuality="chosenQuality"
    v-model:selectedDocument="selectedDocument"
    v-model:insertedComment="insertedComment"
    v-model:selectedDocumentMeta="selectedDocumentMeta"
    v-model:insertedPage="insertedPage"
  />
</template>

<script setup lang="ts">
import ExtendedDataPointFormFieldDialog from '@/components/resources/dataTable/modals/ExtendedDataPointFormFieldDialog.vue';
import { inject, ref, watch, watchEffect } from 'vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import RadioButton from 'primevue/radiobutton';
import { buildApiBody, parseValue } from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps({
  value: [String, Number, null],
  chosenQuality: String,
  selectedDocument: String,
  insertedComment: String,
  insertedPage: String,
});

const value = ref<number | null>(parseValue(props.value));
const chosenQuality = ref<string | null>(props.chosenQuality ?? null);
const selectedDocument = ref<string | null>(props.selectedDocument ?? null);
const insertedComment = ref<string | null>(props.insertedComment ?? null);
const insertedPage = ref<string | null>(props.insertedPage ?? null);
const apiBody = ref({});
const companyId = inject<string>('companyId');
const reportingPeriod = inject<string>('reportingPeriod');
const dataPointTypeId = inject<string>('dataPointTypeId');
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | null>(null);

const emit = defineEmits(['update:apiBody']);

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
    reportingPeriod!,
    dataPointTypeId!
  );
  emit('update:apiBody', apiBody.value);
});
</script>