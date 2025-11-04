<template>
  <h4>Quality</h4>
  <Select
    :placeholder="'Select Quality'"
    :options="qualityOptionsList ?? []"
    optionLabel="label"
    optionValue="value"
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
      @update:modelValue="(val) => (selectedDocument = val)"
      fluid
    />
    <InputText
      :modelValue="insertedPage"
      @update:modelValue="(val) => (insertedPage = val ?? null)"
      placeholder="Page Number"
      style="width: 8em"
    />
    <div
      v-if="selectedDocumentMetaInformation"
      class="dataland-info-text small"
      style="background-color: var(--p-blue-50); margin: var(--spacing-xs)"
    >
      <div><strong>Name:</strong> {{ selectedDocumentMetaInformation.documentName }}</div>
      <div><strong>Category:</strong> {{ selectedDocumentMetaInformation.documentCategory ?? '–' }}</div>
      <div><strong>Publication Date:</strong> {{ selectedDocumentMetaInformation.publicationDate ?? '–' }}</div>
      <div><strong>Reporting Period:</strong> {{ selectedDocumentMetaInformation.reportingPeriod ?? '–' }}</div>
    </div>
  </div>
  <PrimeButton
    label="Upload Document"
    icon="pi pi-upload"
    variant="link"
    @click="router.push(`/companies/${companyId}/documents`)"
  />
  <h4>Comment</h4>
  <Textarea
    :placeholder="'Insert comment'"
    :modelValue="insertedComment"
    @update:modelValue="(val) => (insertedComment = val)"
    rows="5"
    :draggable="false"
    fluid
  />
</template>

<script setup lang="ts">
import Select from 'primevue/select';
import { QualityOptions } from '@clients/backend';
import { computed, defineProps, inject, onMounted, ref, watch } from 'vue';
import type { DocumentMetaInfoResponse } from '@clients/documentmanager';
import PrimeButton from 'primevue/button';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import Textarea from 'primevue/textarea';
import InputText from 'primevue/inputtext';
import router from '@/router';

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

const qualityOptionsList = Object.values(QualityOptions).map((value) => ({ label: value, value }));
const selectedDocumentMetaInformation = computed(() => {
  const docs = Array.isArray(allDocuments) ? allDocuments : [];
  return docs.find((doc) => doc && doc.documentId === selectedDocument.value);
});

watch(chosenQuality, (val) => emit('update:chosenQuality', val));
watch(insertedComment, (val) => emit('update:insertedComment', val));
watch(insertedPage, (val) => emit('update:insertedPage', val));
watch(selectedDocument, (val) => {
  emit('update:selectedDocument', val);
  const meta = allDocuments.value.find((doc) => doc.documentId === val) ?? null;
  emit('update:selectedDocumentMeta', meta);
});

onMounted(async () => {
  await updateDocumentsList();
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
</script>

<style scoped>
.data-source-wrapper {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
</style>
