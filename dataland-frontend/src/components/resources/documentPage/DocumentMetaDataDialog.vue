<template>
  <PrimeDialog
    id="documentMetaDataDialog"
    :dismissable-mask="true"
    :modal="true"
    header="Document Details"
    class="col-6"
    v-model:visible="isOpen"
    @hide="closeDialog"
    data-test="document-details-modal"
  >
    <div v-if="metaData" class="p-datatable p-component">
      <div class="p-datatable-wrapper overflow-auto">
        <table class="p-datatable-table" aria-label="Data point content">
          <tbody class="p-datatable-body">
            <tr>
              <th>Name</th>
              <td class="nowrap" data-test="document-link">
                <DocumentDownloadLink
                  :document-download-info="{
                    downloadName: metaData.documentName ? metaData.documentName : metaData.documentId,
                    fileReference: metaData.documentId,
                  }"
                  show-icon
                />
              </td>
            </tr>
            <tr>
              <th>Publication date</th>
              <td data-test="publication-date">
                {{ metaData.publicationDate ? dateStringFormatter(metaData.publicationDate) : '' }}
              </td>
            </tr>
            <tr>
              <th>Document type</th>
              <td data-test="document-type">{{ humanizeStringOrNumber(metaData?.documentCategory) }}</td>
            </tr>
            <tr v-if="metaData.reportingPeriod">
              <th>Reporting period</th>
              <td class="nowrap" data-test="reporting-period">{{ metaData.reportingPeriod }}</td>
            </tr>
            <tr>
              <th>Upload time</th>
              <td data-test="upload-time">{{ convertUnixTimeInMsToDateString(metaData.uploadTime) }}</td>
            </tr>
            <tr>
              <th>Linked companies</th>
              <td data-test="linked-companies">
                <a
                  v-for="(company, index) in metaData.company"
                  :key="index"
                  :href="`${baseURL}/companies/${company.id}`"
                  target="_blank"
                  class="linked-companies"
                >
                  {{ company.name }}
                </a>
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
import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';
import { convertUnixTimeInMsToDateString, dateStringFormatter } from '@/utils/DataFormatUtils.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';

const props = defineProps<{
  documentId: string;
}>();

export interface CompanyDetails {
  name: string;
  id: string;
}

export interface ExtendedDocumentMetaInfoEntity extends Omit<DocumentMetaInfoEntity, 'companyIds'> {
  company: CompanyDetails[];
}

const isOpen = defineModel<boolean>('isOpen');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const metaData = ref<ExtendedDocumentMetaInfoEntity | null>(null);
const baseURL = ref(window.location.origin);

/**
 * Get metadata of document
 */
async function getDocumentMetaInformation(): Promise<void> {
  if (!getKeycloakPromise || !props.documentId) return;
  try {
    const documentControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).apiClients
      .documentController;
    const companyDataControllerApi = new ApiClientProvider(assertDefined(getKeycloakPromise)()).backendClients
      .companyDataController;
    const data: DocumentMetaInfoEntity = (await documentControllerApi.getDocumentMetaInformation(props.documentId))
      .data;
    const companyDetailsPromises = Array.from(data.companyIds).map((companyId) => {
      return { id: companyId, promise: companyDataControllerApi.getCompanyInfo(companyId) };
    });
    const companyDetails: CompanyDetails[] = [];
    for (const companyDetailPromise of companyDetailsPromises) {
      companyDetails.push({
        id: companyDetailPromise.id,
        name: (await companyDetailPromise.promise).data.companyName,
      });
    }
    metaData.value = { ...data, company: companyDetails };
  } catch (error) {
    console.error(error);
  }
}

watch(
  () => props.documentId,
  () => {
    getDocumentMetaInformation().catch((error) => console.error(error));
  }
);

const closeDialog = (): void => {
  isOpen.value = false;
};

onMounted(() => {
  getDocumentMetaInformation().catch((error) => console.error(error));
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
      width: 2rem;
      padding-right: 1rem;
      font-weight: normal;
    }
  }
}

.linked-companies {
  border: 0 none;
  text-decoration: none;
  color: #ff6813;
  display: block;
  margin: 0.5em;
}
</style>
