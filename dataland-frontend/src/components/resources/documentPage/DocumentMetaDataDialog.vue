<template>
  <PrimeDialog
    id="documentMetaDataDialog"
    :dismissable-mask="true"
    :modal="true"
    header="Header"
    class="col-6"
    v-model:visible="internalDialogVisible"
    @hide="closeDialog"
  >
    <template #header>
      <div v-if="metaData" class="p-datatable p-component">
      <h2 class="m-0">Document Details</h2>
      <div class="p-datatable-wrapper overflow-auto">
        <table class="p-datatable-table" aria-label="Data point content">
          <tbody class="p-datatable-body">
          <tr>
            <th class="headers-bg">Name</th>
            <td class="nowrap">
              <DocumentLink
                  :download-name="(metaData.documentName ? metaData.documentName : metaData.documentId)"
                  :file-reference="metaData.documentId"
                  show-icon
              />
            </td>
          </tr>
          <tr>
            <th class="headers-bg">Publication date</th>
            <td>{{ metaData.publicationDate }}</td>
          </tr>
          <tr>
            <th class="headers-bg">Document type</th>
            <td>{{ metaData?.documentCategory }}</td>
          </tr>
          <tr v-if="metaData.reportingPeriod">
            <th class="headers-bg">Reporting period</th>
            <td class="nowrap">{{metaData.reportingPeriod}}
            </td>
          </tr>
          <tr>
            <th class="headers-bg">Upload time</th>
            <td>{{ metaData.uploadTime }}</td>
          </tr>
          <tr>
            <th class="headers-bg">Linked companies</th>
            <td>{{ metaData.companyIds }}</td>
          </tr>
          </tbody>
        </table>
      </div>
      </div>
    </template>
  </PrimeDialog>
</template>

<script setup lang="ts">
import PrimeDialog from 'primevue/dialog';
import { inject, onMounted, ref, watch } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import type { DocumentMetaInfoEntity } from '@clients/documentmanager';
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";

const props = defineProps<{
  dialogVisible: boolean;
  documentId: string;
}>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const internalDialogVisible = ref(props.dialogVisible);
const emit = defineEmits(['update:dialogVisible']);
const metaData = ref<DocumentMetaInfoEntity | null>(null);

/**
 * Get metadata of document
 */
async function getDocumentMetaInformation(): Promise<void> {
  try {
    if (getKeycloakPromise) {
      const documentControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
        .documentController;
      metaData.value = (await documentControllerApi.getDocumentMetaInformation(props.documentId)).data;
    }
  } catch (error) {
    console.error(error);
  }
}

watch(internalDialogVisible, (newValue) => {
  emit('update:dialogVisible', newValue);
});

watch(
  () => props.dialogVisible,
  (newValue) => {
    if (internalDialogVisible.value !== newValue) {
      internalDialogVisible.value = newValue;
    }
  }
);

const closeDialog = () => {
  internalDialogVisible.value = false;
};

onMounted(() => {
  getDocumentMetaInformation();
});
</script>

<style scoped lang="scss">
.p-datatable-table {
  border-spacing: 0;
  border-collapse: collapse;
  tr {
    border-bottom: 1px solid #e3e2df;
    &:last-child {
      border: none;
    }
    td {
      padding: 0.5rem;
      border: none;
      &.nowrap {
        white-space: nowrap;
      }
    }
    th {
      &.headers-bg {
        width: 2rem;
        padding-right: 1rem;
        font-weight: normal;
      }
    }
  }
}
</style>
