<template>
  <PrimeDialog
    id="documentMetaDataDialog"
    :dismissable-mask="true"
    :modal="true"
    class="col-4"
    style="min-width: 20rem"
    v-model:visible="isOpen"
    @hide="closeDialog"
    data-test="document-details-modal"
  >
    <template #header>
      <div style="display: flex; align-items: center; justify-content: space-between; width: 100%">
        <span class="p-dialog-title">Document Details</span>
        <PrimeButton
          v-if="!editMode && canUserPatchMetaData"
          icon="pi pi-pencil"
          data-test="edit-icon"
          variant="text"
          @click.stop="editMode = true"
          style="align-content: end; margin: var(--spacing-xxs)"
        />
      </div>
    </template>
    <div v-if="metaData" class="p-datatable">
      <div>
        <table class="p-datatable-table" aria-label="Data point content">
          <tbody>
            <tr>
              <th>Name</th>
              <td v-if="editMode">
                <InputText
                  v-model="metaDataPatch.documentName"
                  style="min-width: 15rem"
                  :invalid="!metaDataPatch.documentName"
                  placeholder="Enter document name"
                  data-test="document-name-input"
                />
              </td>
              <td v-else class="nowrap" data-test="document-link">
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
              <th>Document type</th>
              <td v-if="editMode">
                <Select
                  v-model="metaDataPatch.documentCategory"
                  :options="documentCategories"
                  optionLabel="label"
                  optionValue="value"
                  style="min-width: 15rem"
                  data-test="document-category-select"
                />
              </td>
              <td v-else data-test="document-type">{{ humanizeStringOrNumber(metaData?.documentCategory) }}</td>
            </tr>
            <tr>
              <th>Publication date</th>
              <td v-if="editMode">
                <DatePicker
                  v-model="metaDataPatch.publicationDate"
                  :updateModelType="'date'"
                  showIcon
                  dateFormat="D, d M yy"
                  placeholder="Select publication date"
                  data-test="publication-date-picker"
                />
              </td>
              <td v-else data-test="publication-date">
                {{ metaData.publicationDate ? dateStringFormatter(metaData.publicationDate) : '' }}
              </td>
            </tr>
            <tr>
              <th>Reporting period</th>
              <td v-if="editMode">
                <DatePicker
                  v-model="metaDataPatch.reportingPeriod"
                  :updateModelType="'date'"
                  showIcon
                  view="year"
                  dateFormat="yy"
                  placeholder="Select reporting period"
                  data-test="reporting-period-picker"
                />
              </td>
              <td v-else class="nowrap" data-test="reporting-period">{{ metaData.reportingPeriod }}</td>
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
      <Message
        v-if="errorMessage && editMode"
        severity="error"
        style="margin: var(--spacing-sm)"
        data-test="metadata-error-message"
      >
        {{ errorMessage }}
      </Message>
      <div v-if="editMode" class="button-row">
        <PrimeButton label="CANCEL" class="p-button-text" @click="onCancel" data-test="cancel-edit-button" />
        <PrimeButton label="SAVE CHANGES" @click="saveChanges" data-test="save-edit-button" />
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
import {
  DocumentMetaInfoDocumentCategoryEnum,
  type DocumentMetaInfoEntity,
  type DocumentMetaInfoPatch,
} from '@clients/documentmanager';
import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';
import { convertUnixTimeInMsToDateString, dateStringFormatter } from '@/utils/DataFormatUtils.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import PrimeButton from 'primevue/button';
import InputText from 'primevue/inputtext';
import Select from 'primevue/select';
import Message from 'primevue/message';
import DatePicker from 'primevue/datepicker';
import { checkIfUserHasRole, getUserId } from '@/utils/KeycloakUtils.ts';
import { KEYCLOAK_ROLE_ADMIN, KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles.ts';
import { AxiosError } from 'axios';

const props = defineProps<{
  documentId: string;
}>();

const emit = defineEmits(['data-patched']);

export interface CompanyDetails {
  name: string;
  id: string;
}

export interface ExtendedDocumentMetaInfoEntity extends Omit<DocumentMetaInfoEntity, 'companyIds'> {
  company: CompanyDetails[];
}

const isOpen = defineModel<boolean>('isOpen');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const documentControllerApi = apiClientProvider.apiClients.documentController;
const companyDataControllerApi = apiClientProvider.backendClients.companyDataController;
const metaData = ref<ExtendedDocumentMetaInfoEntity | null>(null);
const metaDataPatch = ref<{
  documentName?: string;
  documentCategory?: DocumentMetaInfoDocumentCategoryEnum;
  publicationDate?: Date | null;
  reportingPeriod?: Date | null;
}>({
  documentName: '',
  documentCategory: undefined,
  publicationDate: null,
  reportingPeriod: null,
});
const baseURL = ref(globalThis.location.origin);

const editMode = ref<boolean>(false);
const canUserPatchMetaData = ref<boolean>(false);
const errorMessage = ref<string>('');

const documentCategories = ref(
  Object.values(DocumentMetaInfoDocumentCategoryEnum).map((category) => ({
    label: humanizeStringOrNumber(category),
    value: category,
  }))
);

/**
 * Get metadata of document
 */
async function getDocumentMetaInformation(): Promise<void> {
  if (!getKeycloakPromise || !props.documentId) return;
  try {
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
    canUserPatchMetaData.value = await getUserPatchRights();
    setMetaDataPatch();
  } catch (error) {
    console.error(error);
  }
}

/**
 * Determine if user has rights to patch document metadata
 */
async function getUserPatchRights(): Promise<boolean> {
  const userId = await getUserId(assertDefined(getKeycloakPromise));
  const isUploader = await checkIfUserHasRole(KEYCLOAK_ROLE_UPLOADER, getKeycloakPromise);
  const isAdmin = await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise);
  return (userId === metaData.value?.uploaderId && isUploader) || isAdmin;
}

/**
 * Set metadata patch object based on current metadata
 */
function setMetaDataPatch(): void {
  metaDataPatch.value = {
    documentName: metaData.value?.documentName,
    documentCategory: metaData.value?.documentCategory,
    publicationDate: metaData.value?.publicationDate ? new Date(metaData.value.publicationDate) : null,
    reportingPeriod: metaData.value?.reportingPeriod ? new Date(metaData.value.reportingPeriod) : null,
  };
}

/**
 * Save changes made to document metadata
 */
async function saveChanges(): Promise<void> {
  if (!metaDataPatch.value.documentName || !metaDataPatch.value.documentCategory) {
    errorMessage.value = 'Please fill in all required fields.';
    return;
  }
  const payload = {
    ...metaDataPatch.value,
    publicationDate: metaDataPatch.value.publicationDate
      ? metaDataPatch.value.publicationDate.toLocaleDateString('en-CA')
      : undefined,
    reportingPeriod: metaDataPatch.value.reportingPeriod
      ? metaDataPatch.value.reportingPeriod.getFullYear().toString()
      : undefined,
  } as DocumentMetaInfoPatch;
  console.log('Saving changes for document:', payload);
  try {
    await documentControllerApi.patchDocumentMetaInfo(props.documentId, payload);
  } catch (error: unknown) {
    errorMessage.value = error instanceof AxiosError ? error.message : 'An unknown error occurred.';
    console.error('Error saving document metadata changes:', error);
    return;
  }
  errorMessage.value = '';
  editMode.value = false;
  getDocumentMetaInformation().catch((error) => console.error(error));
  emit('data-patched');
}

/**
 * Cancel editing document metadata
 */
function onCancel(): void {
  editMode.value = false;
  errorMessage.value = '';
  setMetaDataPatch();
}

watch(
  () => props.documentId,
  () => {
    getDocumentMetaInformation().catch((error) => console.error(error));
  }
);

const closeDialog = (): void => {
  errorMessage.value = '';
  isOpen.value = false;
  editMode.value = false;
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

    th {
      width: 2rem;
      padding-right: var(--spacing-md);
    }

    &:hover {
      background: none;
    }
  }
}

.p-datatable {
  tr {
    th,
    td {
      text-align: left;
      padding: var(--spacing-md);
    }
  }
}

.p-dialog-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.linked-companies {
  border: 0 none;
  text-decoration: none;
  color: var(--primary-color);
  display: block;
  margin: 0.5em;
}

.button-row {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  margin-top: var(--spacing-md);
}
</style>
