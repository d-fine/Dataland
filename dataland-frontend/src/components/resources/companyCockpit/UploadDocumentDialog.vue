<template>
  <PrimeDialog
    v-model:visible="isVisible"
    modal
    header="Upload Document"
    :style="{ width: '40rem' }"
    data-test="upload-document-modal"
    @hide="emit('close')"
  >
    <div class="upload-document-container">
      <div class="field" :title="selectedFiles.length >= 1 ? 'You can only upload one document at a time.' : ''">
        <p class="upload-label">Select Document</p>
        <FileUpload
          :maxFileSize="DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES"
          :invalidFileSizeMessage="`{0}: Invalid file size, file size should be smaller than ${
            DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES / BYTE_TO_MEGABYTE_FACTOR
          } MB.`"
          mode="advanced"
          chooseLabel="CHOOSE"
          :showUploadButton="false"
          :showCancelButton="false"
          @select="onFileSelect"
          @remove="onFileRemove"
          :files="selectedFiles"
          :auto="false"
          :multiple="false"
          :disabled="selectedFiles.length >= 1"
          data-test="file-upload"
          :pt="{
            fileThumbnail: { style: 'display: none;' },
            pcFileBadge: { root: { style: 'display: none;' } },
            pcProgressBar: { root: { style: 'display: none;' } },
          }"
        >
          <template #empty>
            <span class="custom-drop-area">Drag and drop a file here to upload</span>
          </template>
        </FileUpload>
        <Message
          v-if="showErrors && selectedFiles.length === 0"
          severity="error"
          variant="simple"
          size="small"
          data-test="file-upload-error"
        >
          Please select a file to upload.
        </Message>
      </div>

      <div class="field">
        <p class="upload-label">Document Name</p>
        <InputText
          v-model="documentName"
          placeholder="Enter Document Name"
          :class="{ 'error-field': showErrors && !documentName }"
          data-test="document-name"
        />
        <Message
          v-if="showErrors && !documentName"
          severity="error"
          variant="simple"
          size="small"
          data-test="document-name-error"
        >
          Document name is required.
        </Message>
      </div>

      <div class="field">
        <p class="upload-label">Document Category</p>
        <Select
          v-model="documentCategory"
          :options="documentCategories"
          optionLabel="label"
          optionValue="value"
          placeholder="Select category"
          :class="{ 'error-field': showErrors && !documentCategory }"
          data-test="document-category"
        />
        <Message
          v-if="showErrors && !documentCategory"
          severity="error"
          variant="simple"
          size="small"
          data-test="document-category-error"
        >
          Please select a category.
        </Message>
      </div>
      <div class="date-row">
        <div class="field">
          <p class="upload-label">Publication Date (optional)</p>
          <DatePicker
            v-model="publicationDate"
            showIcon
            placeholder="Select publication date"
            data-test="publication-date"
          />
        </div>
        <div class="field">
          <p class="upload-label">Reporting Period (optional)</p>
          <DatePicker
            v-model="reportingPeriod"
            showIcon
            view="year"
            dateFormat="yy"
            placeholder="Select year"
            data-test="reporting-period"
          />
        </div>
      </div>
      <Message v-if="errorMessage" severity="error">
        {{ errorMessage }}
      </Message>
      <Button
        label="UPLOAD DOCUMENT"
        @click="onSubmit"
        :disabled="isUploadButtonDisabled"
        style="width: auto; margin-left: auto"
        data-test="upload-document-button"
      />
    </div>
  </PrimeDialog>
  <SuccessDialog
    :visible="successModalIsVisible"
    message="Document uploaded successfully."
    @close="closeSuccessModal"
  />
</template>

<script lang="ts" setup>
import { ref, computed, inject } from 'vue';
import PrimeDialog from 'primevue/dialog';
import FileUpload from 'primevue/fileupload';
import type { FileUploadSelectEvent, FileUploadRemoveEvent } from 'primevue/fileupload';
import InputText from 'primevue/inputtext';
import Select from 'primevue/select';
import DatePicker from 'primevue/datepicker';
import Button from 'primevue/button';
import Message from 'primevue/message';
import { DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES, BYTE_TO_MEGABYTE_FACTOR } from '@/DatalandSettings';
import { type DocumentMetaInfo, DocumentMetaInfoDocumentCategoryEnum } from '@clients/documentmanager';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { AxiosError, isAxiosError } from 'axios';
import SuccessDialog from '@/components/general/SuccessDialog.vue';

const props = defineProps<{ visible: boolean; companyId: string }>();
const emit = defineEmits(['close', 'document-uploaded', 'conflict']);

const isVisible = ref<boolean>(props.visible);
const selectedFiles = ref<File[]>([]);
const documentName = ref<string>('');
const documentCategory = ref<DocumentMetaInfoDocumentCategoryEnum | null>(null);
const documentCategories = ref(
  Object.values(DocumentMetaInfoDocumentCategoryEnum).map((category) => ({
    label: humanizeStringOrNumber(category),
    value: category,
  }))
);

const publicationDate = ref<Date | null>(null);
const reportingPeriod = ref<Date | null>(null);
const showErrors = ref<boolean>(false);
const successModalIsVisible = ref<boolean>(false);
const isUploadButtonDisabled = ref<boolean>(false);
const errorMessage = ref<string>('');

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const documentControllerApi = apiClientProvider.apiClients.documentController;

const isFormValid = computed<boolean>(() => {
  return selectedFiles.value.length == 1 && !!documentName.value && !!documentCategory.value;
});

/**
 * Handles the document upload process.
 */
async function handleDocumentUpload(): Promise<void> {
  if (selectedFiles.value.length === 0) {
    return;
  }

  const fileToUpload: File = selectedFiles.value[0] as File;
  const documentMetaInfo: DocumentMetaInfo = {
    documentName: documentName.value,
    documentCategory: documentCategory.value!,
    companyIds: [props.companyId] as unknown as Set<string>,
    publicationDate: publicationDate.value ? publicationDate.value.toISOString().split('T')[0] : undefined,
    reportingPeriod: reportingPeriod.value ? reportingPeriod.value.getFullYear().toString() : undefined,
  };
  await documentControllerApi.postDocument(fileToUpload, documentMetaInfo);
}

/**
 * Handles file selection event.
 */
const onFileSelect = (event: FileUploadSelectEvent): void => {
  selectedFiles.value = event.files;
};

/**
 * Handles file removal event.
 */
const onFileRemove = (event: FileUploadRemoveEvent): void => {
  selectedFiles.value = selectedFiles.value.filter((f) => f !== event.file);
};

/**
 * Submits the document upload form.
 */
const onSubmit = async (): Promise<void> => {
  showErrors.value = true;

  if (!isFormValid.value) {
    return;
  }

  isUploadButtonDisabled.value = true;

  try {
    await handleDocumentUpload();
    successModalIsVisible.value = true;
  } catch (error: unknown) {
    const documentId = extractDocumentIdFromError(error);
    if (documentId) {
      emit('conflict', documentId);
    } else {
      console.error('Error uploading document:', error);
      errorMessage.value = error instanceof AxiosError ? error.message : 'An unknown error occurred.';
    }
  } finally {
    isUploadButtonDisabled.value = false;
  }
};

/**
 * Extracts documentID from error response if available.
 */
function extractDocumentIdFromError(error: unknown): string | null {
  if (!isAxiosError(error) || error.response?.status !== 409) return null;
  const errors = error.response?.data?.errors;
  if (!Array.isArray(errors)) return null;
  const conflictError = errors.find((e) => e.errorType === 'conflict');
  if (!conflictError) return null;
  return conflictError.summary?.match(/Document ID: ([a-f0-9]+)/i)?.[1] ?? null;
}

/**
 * Closes the success modal and resets the form.
 */
const closeSuccessModal = (): void => {
  successModalIsVisible.value = false;
  emit('close');
  emit('document-uploaded');
};
</script>

<style scoped lang="scss">
.upload-document-container {
  display: flex;
  flex-direction: column;
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xxs);
}

.upload-label {
  font-weight: var(--font-weight-bold);
}

.date-row {
  display: flex;
  gap: var(--spacing-md);
}
.date-row .field {
  flex: 1;
}

.error-field {
  border-color: var(--message-error-border);
}

.custom-drop-area {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100px;
  border: 2px dashed var(--primary-color);
  background: var(--grey-tones-100);
}
</style>
