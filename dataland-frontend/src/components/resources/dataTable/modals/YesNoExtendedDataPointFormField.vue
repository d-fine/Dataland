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
import { buildApiBody } from '@/components/resources/dataTable/conversion/Utils.ts';

const props = defineProps({
  value: [String, Number, null],
  chosenQuality: String,
  selectedDocument: String,
  insertedComment: String,
  insertedPage: String,
  reportingPeriod: String,
  dataPointTypeId: String,
});

const chosenQuality = ref<string | null>(props.chosenQuality ?? null);
const selectedDocument = ref<string | null>(props.selectedDocument ?? null);
const insertedComment = ref<string | null>(props.insertedComment ?? null);
const insertedPage = ref<string | null>(props.insertedPage ?? null);
const apiBody = ref({});
const companyId = inject<string>('companyId');
const reportingPeriod = ref<string>(props.reportingPeriod!);
const dataPointTypeId = ref<string>(props.dataPointTypeId!);
const selectedDocumentMeta = ref<DocumentMetaInfoResponse | null>(null);
const emit = defineEmits(['update:apiBody']);
const value = ref<string | null>(props.value !== null && props.value !== undefined ? String(props.value) : null);

watch(
  () => props.value,
  (newVal) => {
    value.value = newVal !== null && newVal !== undefined ? String(newVal) : null;
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
