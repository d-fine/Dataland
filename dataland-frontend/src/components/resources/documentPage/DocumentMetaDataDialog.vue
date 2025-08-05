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

.p-datatable {
  border-radius: 0;
  background: var(--table-background-color);

  .border-left {
    border-left: 1px solid var(--table-border);
  }

  .border-right {
    border-right: 1px solid var(--table-border);
  }

  .border-bottom {
    border-bottom: 1px solid var(--table-border);
  }

  .horizontal-headers-size {
    background-color: var(--default-neutral-white);

    &:first-of-type {
      width: var(--first-table-column-width);
    }
  }
  tr {
    &:not(.p-rowgroup-header) {
      td {
        border-bottom: 1px solid var(--table-border);
      }
    }
    &:hover {
      background: var(--table-background-hover-color);
    }
    th,
    td {
      text-align: left;
      padding: 1rem;
    }
  }
  .p-datatable-tbody {
    tr {
      border-color: hsl(from var(--table-border-dark) h s 45);
    }
    .info-icon {
      float: right;
      max-width: 20%;
    }
    .table-left-label {
      float: left;
      max-width: 80%;
    }
  }
  .p-sortable-column {
    .p-sortable-column-icon {
      color: var(--table-icon-color);
      margin-left: 0.5rem;
    }
    &.p-highlight {
      background: var(--table-background-color);
      color: var(--main-color);
      .p-sortable-column-icon {
        color: var(--main-color);
      }
    }
  }
  .headers-bg {
    background-color: var(--tables-headers-bg);
    display: table-cell;
    width: var(--first-table-column-width);
  }
  .auto-headers-size {
    width: auto;
  }
  .p-rowgroup-header {
    background-color: var(--table-background-hover-color-light);
    cursor: pointer;

    &.p-topmost-header {
      background-color: var(--tables-headers-bg);
    }

    td {
      position: relative;
      width: var(--first-table-column-width);
      button {
        position: absolute;
        right: 1rem;
        top: 50%;
        margin-top: -7px;
      }
    }
  }

  .p-datatable-thead {
    z-index: 1;
    tr {
      box-shadow: none;
      &:hover {
        background: var(--table-background-color);
      }
    }
  }

  &.activities-data-table {
    $col-activity-width: 300px;
    $col-nace-codes-width: 70px;
    .group-row-header {
      background-color: var(--tables-headers-bg);
      border-bottom: 1px solid var(--table-border);
      .p-column-header-content {
        justify-content: center;
      }
      &:not(:first-of-type) {
        border-left: 1px solid var(--table-border);
      }
    }
    .first-group-column:not(:first-of-type) {
      border-left: 1px solid var(--table-border);
    }
    .non-frozen-header {
      vertical-align: top;
    }
    .frozen-row-header {
      vertical-align: top;
      background-color: var(--tables-headers-bg);
    }
    .col-activity {
      width: $col-activity-width;
      min-width: $col-activity-width;
    }
    .col-nace-codes {
      width: $col-nace-codes-width;
      min-width: $col-nace-codes-width;
      border-right: 1px solid var(--table-border);
    }
    .col-value {
      width: 160px;
      min-width: 160px;
    }
    .col-percentage {
      min-width: 6rem;
    }
  }
}

.info-icon {
  cursor: help;
}
</style>
