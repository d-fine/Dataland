<template>
  <h4>Quality</h4>
  <Select
      :placeholder="dataPointProperties?.quality ?? 'Select Quality'"
      :options="qualityOptionsList ?? []"
      optionLabel="label"
      optionValue="value"
      :modelValue="insertedQuality"
      fluid
  />
  <h4>Data Source</h4>
  <Select
      :modelValue="selectedDocument"
      :options="availableDocuments ?? []"
      optionLabel="label"
      optionValue="value"
      :placeholder="dataPointProperties?.dataSource?.fileName"
      fluid
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
  <PrimeButton label="Upload Document" icon="pi pi-upload" variant="link" />
  <h4>Comment</h4>
  <Textarea :placeholder="props.dataPointProperties?.comment ?? 'Insert comment'"
             :modelValue="insertedComment"
             fluid />
</template>

<script setup lang="ts">

import Select from "primevue/select";
import {QualityOptions} from "@clients/backend";
import {computed, defineProps, inject, onMounted, ref} from "vue";
import type {DocumentMetaInfoResponse} from "@clients/documentmanager";
import PrimeButton from 'primevue/button'
import type Keycloak from "keycloak-js";
import {ApiClientProvider} from "@/services/ApiClients.ts";
import {assertDefined} from "@/utils/TypeScriptUtils.ts";
import Textarea from "primevue/textarea";

const allDocuments = ref<DocumentMetaInfoResponse[]>([]);
const availableDocuments = ref<{ label: string; value: string }[]>([]);
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const insertedQuality = ref<string | null>(null);
const insertedComment = ref<string | null>(null);
const selectedDocument = ref<string | null>(null);

const props = defineProps({
  dataPointProperties: {
    type: Object as () => Record<string, any> | undefined,
    required: false
  },
});
const companyID = inject<string>('companyID');

onMounted(() => {
  updateDocumentsList();
});

/**
 * Fetches the list of documents from the API and updates the availableDocuments and allDocuments refs.
 */
async function updateDocumentsList(): Promise<void> {
  try {
    const documentControllerApi = apiClientProvider.apiClients.documentController;
    const response = await documentControllerApi.searchForDocumentMetaInformation(companyID);
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

const qualityOptionsList = Object.values(QualityOptions).map(value => ({ label: value, value }))
const selectedDocumentMetaInformation = computed(() => {
  const docs = Array.isArray(allDocuments) ? allDocuments : [];
  return docs.find(doc => doc && doc.documentId === selectedDocument.value);
});

</script>

<style scoped>

</style>