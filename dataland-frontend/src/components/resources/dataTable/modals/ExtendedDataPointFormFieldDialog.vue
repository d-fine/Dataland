<template>
  <h4>Quality</h4>
  <Select
    :placeholder="'Select Quality'"
    :options="qualityOptionsList ?? []"
    optionLabel="label"
    optionValue="value"
    data-test="quality-select"
    :modelValue="chosenQuality"
    @update:modelValue="(val) => (chosenQuality = val)"
    fluid
  />
  <h4>Data Source</h4>
  <div class="data-source-wrapper">
    <Select
      :placeholder="'Select Document'"
      :modelValue="selectedDocument"
      :options="availableDocuments ?? []"
      optionLabel="label"
      optionValue="value"
      data-test="document-select"
      @update:modelValue="(val) => (selectedDocument = val)"
      style="width: 17em"
      fluid
    />
    <InputText
      :modelValue="insertedPage"
      @update:modelValue="(val) => (insertedPage = val ?? null)"
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

  <PrimeButton label="Up- or download Document" icon="pi pi-file" variant="link" @click="handleUploadDocumentClick" />
  <h4>Comment</h4>
  <Textarea
    :placeholder="'Insert comment'"
    :modelValue="insertedComment"
    data-test="comment-textarea"
    @update:modelValue="(val) => (insertedComment = val)"
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

const allDocuments = ref<DocumentMetaInfoResponse[]>([]);
const availableDocuments = ref<{ label: string; value: string }[]>([]);
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const props = defineProps<{
  chosenQuality?: string | null;
  selectedDocument?: string | null;
  insertedComment?: string | null;
  insertedPage?: string | null;
}>();

const emit = defineEmits([
  'update:chosenQuality',
  'update:selectedDocument',
  'update:insertedComment',
  'update:selectedDocumentMeta',
  'update:insertedPage',
]);

const chosenQuality = ref<string | null>(props.chosenQuality ?? null);
const selectedDocument = ref<string | null>(props.selectedDocument ?? null);
const insertedComment = ref<string | null>(props.insertedComment ?? null);
const insertedPage = ref<string | null>(props.insertedPage ?? null);
const companyId = inject<string>('companyId');
const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');

const qualityOptionsList = Object.values(QualityOptions).map((value) => ({ label: value, value }));
const selectedDocumentMetaInformation = computed(() => {
  return allDocuments.value.find((doc) => doc.documentId === selectedDocument.value) ?? null;
});

watch(chosenQuality, (val) => emit('update:chosenQuality', val));
watch(insertedComment, (val) => emit('update:insertedComment', val));
watch(insertedPage, (val) => emit('update:insertedPage', val));
watch(selectedDocument, (val) => {
  setSelectedDocument(val);
});

onMounted(async () => {
  await updateDocumentsList();

  if (props.selectedDocument) {
    setSelectedDocument(
        allDocuments.value.find(
            (doc) => doc.documentName === props.selectedDocument
        )?.documentId ?? null
    );
  } else {
    setSelectedDocument(null);
  }

  if (props.chosenQuality) {
    const matchQuality = qualityOptionsList.find((q) => q.value === props.chosenQuality);
    if (matchQuality) {
      chosenQuality.value = matchQuality.value;
      emit('update:chosenQuality', chosenQuality.value);
    }
  }
});

watch(selectedDocument, (val) => {
  const meta = allDocuments.value.find((doc) => doc.documentId === val);

  if (meta == null) {
    selectedDocument.value = null;
    emit('update:selectedDocument', null);
    emit('update:selectedDocumentMeta', null);
    insertedPage.value = null;
    emit('update:insertedPage', null);
  } else {
    emit('update:selectedDocument', val);
    emit('update:selectedDocumentMeta', meta);
  }
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

  if (meta == null) {
    selectedDocument.value = null;
    insertedPage.value = null;
    emit('update:selectedDocument', null);
    emit('update:selectedDocumentMeta', null);
    emit('update:insertedPage', null);
  } else {
    selectedDocument.value = meta.documentId;
    emit('update:selectedDocument', meta.documentId);
    emit('update:selectedDocumentMeta', meta);
  }
}
</script>

<style scoped>
.data-source-wrapper {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
</style>
