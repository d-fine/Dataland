<template>
  <h4>Quality</h4>
  <Select
    :placeholder="'Select Quality'"
    v-model="chosenQuality"
    :options="qualityOptionsList ?? []"
    optionLabel="label"
    optionValue="value"
    data-test="quality-select"
    fluid
  />
  <h4>Data Source</h4>
  <div class="data-source-wrapper">
    <Select
      :placeholder="'Select Document'"
      v-model="selectedDocument"
      :options="availableDocuments ?? []"
      optionLabel="label"
      optionValue="value"
      data-test="document-select"
      style="width: 17em"
      fluid
    />
    <InputText
      v-model="insertedPage"
      placeholder="Page Number"
      data-test="page-number-input"
      style="width: 8em"
      :disabled="!selectedDocument"
    />
  </div>
  <div v-if="selectedDocumentMetaInformation" class="dataland-info-text small" style="margin: var(--spacing-xs)">
    <div><strong>Name:</strong> {{ selectedDocumentMetaInformation.documentName }}</div>
    <div><strong>Category:</strong> {{ selectedDocumentMetaInformation.documentCategory ?? '–' }}</div>
    <div><strong>Publication Date:</strong> {{ selectedDocumentMetaInformation.publicationDate ?? '–' }}</div>
    <div><strong>Reporting Period:</strong> {{ selectedDocumentMetaInformation.reportingPeriod ?? '–' }}</div>
  </div>

  <PrimeButton label="Upload Document" icon="pi pi-upload" variant="link" @click="handleUploadDocumentClick" />
  <h4>Comment</h4>
  <Textarea
    :placeholder="'Insert comment'"
    v-model="insertedComment"
    data-test="comment-textarea"
    rows="5"
    :draggable="false"
    fluid
  />
</template>

<script setup lang="ts">
import Select from 'primevue/select';
import { QualityOptions } from '@clients/backend';
import { computed, defineProps, inject, onMounted, ref, watch, type Ref } from 'vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import PrimeButton from 'primevue/button';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import Textarea from 'primevue/textarea';
import InputText from 'primevue/inputtext';
import router from '@/router';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import type {
  ExtendedDataPointType,
  ExtendedDataPointMetaInfoType,
} from '@/components/resources/dataTable/conversion/Utils.ts';

const allDocuments = ref<DocumentMetaInfoResponse[]>([]);
const availableDocuments = ref<{ label: string; value: string }[]>([]);
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const props = defineProps<{
  extendedDataPointObject?: ExtendedDataPointType;
}>();

const chosenQuality = ref<string | undefined>(props.extendedDataPointObject?.quality ?? undefined);
const selectedDocument = ref<string | null>(props.extendedDataPointObject?.dataSource?.fileReference ?? null);
const fileName = ref<string | null>(props.extendedDataPointObject?.dataSource?.fileName ?? null);
const insertedComment = ref<string | undefined>(props.extendedDataPointObject?.comment ?? undefined);
const insertedPage = ref<string | null>(props.extendedDataPointObject?.dataSource?.page ?? null);
const companyId = inject<string>('companyId');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');

const qualityOptionsList = Object.values(QualityOptions).map((value) => ({ label: value, value }));
const selectedDocumentMetaInformation = computed(() => {
  return allDocuments.value.find((doc) => doc.documentId === selectedDocument.value) ?? null;
});

watch(selectedDocument, (val) => {
  const meta = allDocuments.value.find((doc) => doc.documentId === val) ?? null;
  if (!meta) {
    insertedPage.value = null;
  }
});

onMounted(async () => {
  await updateDocumentsList();

  if (fileName.value) {
    setSelectedDocument(allDocuments.value.find((doc) => doc.documentName === fileName.value)?.documentId ?? null);
  } else {
    setSelectedDocument(null);
  }

  if (chosenQuality.value) {
    const matchQuality = qualityOptionsList.find((q) => q.value === chosenQuality.value);
    if (matchQuality) {
      chosenQuality.value = matchQuality.value;
    }
  }
});

/**
 * Gathers the current form data into an object.
 * @returns An object containing the current form data.
 */
function getFormData(): ExtendedDataPointMetaInfoType {
  return {
    quality: chosenQuality.value ?? undefined,
    comment: insertedComment.value ?? undefined,
    dataSource: {
      fileName: selectedDocumentMetaInformation.value?.documentName ?? undefined,
      page: insertedPage.value?.trim() || undefined,
      fileReference: selectedDocument.value ?? undefined,
      publicationDate: selectedDocumentMetaInformation.value?.publicationDate ?? undefined,
    },
  };
}

defineExpose({
  getFormData,
});

/**
 * Fetches the list of documents from the API and updates the availableDocuments and allDocuments refs.
 */
async function updateDocumentsList(): Promise<void> {
  try {
    const documentControllerApi = apiClientProvider.apiClients.documentController;
    const response = await documentControllerApi.searchForDocumentMetaInformation(companyId);
    allDocuments.value = response.data;
    availableDocuments.value = allDocuments.value
      .filter((doc) => doc.documentName && doc.documentId)
      .map((doc) => ({
        label: doc.documentName!,
        value: doc.documentId,
      }));
  } catch (error) {
    console.error('Error fetching documents:', error);
    allDocuments.value = [];
    availableDocuments.value = [];
  }
}

/**
 * Handles the click event for the "Upload Document" button.
 * Navigates to the document upload page for the current company.
 */
async function handleUploadDocumentClick(): Promise<void> {
  await router.push(`/companies/${companyId}/documents`);
  dialogRef?.value?.close();
}

/**
 * Sets the selected document based on the provided document ID.
 * Updates the selectedDocument, selectedDocumentMeta, and insertedPage refs accordingly.
 *
 * @param docId - The ID of the document to select, or null to clear the selection.
 */
function setSelectedDocument(docId: string | null): void {
  const meta = allDocuments.value.find((doc) => doc.documentId === docId) ?? null;

  selectedDocument.value = meta?.documentId ?? null;
  insertedPage.value = meta ? insertedPage.value : null;
}
</script>

<style scoped>
.data-source-wrapper {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
</style>
