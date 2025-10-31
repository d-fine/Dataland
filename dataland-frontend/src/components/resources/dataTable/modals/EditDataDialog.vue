<template>
  <div>
    <h4>Value</h4>
    <InputText :placeholder="dataPointProperties?.value ?? 'Insert value'" v-model="insertedValue" fluid />

    <h4>Quality</h4>
    <Select
      :placeholder="dataPointProperties?.quality ?? 'Select Quality'"
      :options="qualityOptionsList ?? []"
      optionLabel="label"
      optionValue="value"
      v-model="insertedQuality"
      fluid
    />

    <h4>Data Source</h4>
    <Select
      v-model="selectedDocument"
      :options="availableDocuments ?? []"
      optionLabel="label"
      optionValue="value"
      placeholder="Select Data Source"
      fluid
    />

    <div
      v-if="selectedDocumentMeta"
      class="dataland-info-text small"
      style="background-color: var(--p-blue-50); margin: var(--spacing-xs)"
    >
      <div><strong>Name:</strong> {{ selectedDocumentMeta.documentName }}</div>
      <div><strong>Category:</strong> {{ selectedDocumentMeta.documentCategory ?? '–' }}</div>
      <div><strong>Publication Date:</strong> {{ selectedDocumentMeta.publicationDate ?? '–' }}</div>
      <div><strong>Reporting Period:</strong> {{ selectedDocumentMeta.reportingPeriod ?? '–' }}</div>
    </div>

    <h4>Comment</h4>
    <InputText :placeholder="dataPointProperties?.comment ?? 'Insert comment'" v-model="insertedComment" fluid />

    <PrimeButton label="SAVE CHANGES" icon="pi pi-save" style="margin-top: var(--spacing-md)" @click="saveChanges" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject, onMounted, type Ref } from 'vue';
import Select from 'primevue/select';
import InputText from 'primevue/inputtext';
import PrimeButton from 'primevue/button';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import type { BaseDataPoint } from '@/utils/DataPoint.ts';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const dataPointProperties = ref<{
  value: any;
  quality: string | undefined;
  dataSource: any;
  comment: string | undefined;
} | null>(null);

const qualityOptionsList = ref<{ label: string; value: string }[]>([]);
const allDocuments = ref<DocumentMetaInfoResponse[]>([]);
const availableDocuments = ref<{ label: string; value: string }[]>([]);

const insertedValue = ref<BaseDataPoint<any> | null>(null);
const insertedQuality = ref<string | null>(null);
const insertedComment = ref<string | null>(null);
const selectedDocument = ref<string | null>(null);

onMounted(() => {
  const data = dialogRef?.value?.data;
  if (data) {
    dataPointProperties.value = data.dataPointProperties;
    allDocuments.value = data.allDocuments;
    availableDocuments.value = data.availableDocuments;
  }
});

const selectedDocumentMeta = computed(() =>
  allDocuments.value.find((doc) => doc.documentId === selectedDocument.value)
);

function saveChanges() {
  dialogRef?.value?.close({
    value: insertedValue.value,
    quality: insertedQuality.value,
    comment: insertedComment.value,
    documentId: selectedDocument.value,
  });
}
</script>
