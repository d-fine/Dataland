<template>
  <PrimeDialog
    id="documentMetaDataDialog"
    :dismissable-mask="true"
    :modal="true"
    header="Document Details"
    class="col-6"
    v-model:visible="internalDialogVisible"
    @hide="closeDialog"
  >
    <div v-if="metaData" class="p-datatable p-component">
      <div class="p-datatable-wrapper overflow-auto">
        <table class="p-datatable-table" aria-label="Data point content">
          <tbody class="p-datatable-body">
            <tr>
              <th class="headers-bg">Name</th>
              <td class="nowrap">
                <DocumentLink
                  :download-name="metaData.documentName ? metaData.documentName : metaData.documentId"
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
              <td class="nowrap">{{ metaData.reportingPeriod }}</td>
            </tr>
            <tr>
              <th class="headers-bg">Upload time</th>
              <td>{{ metaData.uploadTime }}</td>
            </tr>
            <tr>
              <th class="headers-bg">Linked companies</th>
              <td>
                <span v-for="(company, index) in metaData.company" :key="index">
                  <a
                    :href="`${baseURL}/companies/${company.id}`"
                    target="_blank"
                    style="border: 0 none; text-decoration: none; color: #ff6813"
                  >
                   {{ company.name }}<br />
                    <br />
                  </a>
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </PrimeDialog>
</template>

<script setup lang="ts">
import PrimeDialog from 'primevue/dialog';
import { inject, onMounted, ref, watch } from 'vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import type { DocumentMetaInfoEntity } from '@clients/documentmanager';
import DocumentLink from '@/components/resources/frameworkDataSearch/DocumentLink.vue';

const props = defineProps<{
  dialogVisible: boolean;
  documentId: string;
}>();

export interface CompanyDetails {
  name: string;
  id: string;
}

export interface ExtendedDocumentMetaInfoEntity extends Omit<DocumentMetaInfoEntity, 'companyIds'> {
  company: CompanyDetails[];
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const internalDialogVisible = ref(props.dialogVisible);
const emit = defineEmits(['update:dialogVisible']);
const metaData = ref<ExtendedDocumentMetaInfoEntity | null>(null);
const baseURL = ref(window.location.origin);


/**
 * Get metadata of document
 */
async function getDocumentMetaInformation(): Promise<void> {
  try {
    if (getKeycloakPromise) {
      const documentControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
        .documentController;
      const companyDataControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)())
          .backendClients.companyDataController;
      const data:DocumentMetaInfoEntity = (await documentControllerApi.getDocumentMetaInformation(props.documentId)).data;
      const companyDetails = ref<CompanyDetails[]>([]);
      for (const companyId of data.companyIds) {
        const company = await companyDataControllerApi.getCompanyInfo(companyId);
        companyDetails.value.push( {id: companyId, name:company.data.companyName});
      }
      metaData.value = {
        ...data,
        company: companyDetails.value
      };


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
    if (newValue && props.documentId) {
      getDocumentMetaInformation();
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
