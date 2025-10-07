<template>
  <PrimeDialog
    v-model:visible="isVisible"
    modal
    header="Upload Document"
    :style="{ width: '40rem' }"
    data-test="upload-document-modal"
    @hide="onCancel"
  >
    <div class="upload-document-container">
      <div class="field">
        <label class="upload-label" for="file-upload">Select Document<span class="required-asterisk">*</span></label>
        <FileUpload
          id="file-upload"
          mode="advanced"
          chooseLabel="Choose"
          cancelLabel="Cancel"
          customUpload
          :showUploadButton="false"
          @select="onFileSelect"
          @remove="onFileRemove"
          @clear="selectedFiles = []"
          :files="selectedFiles"
          :auto="false"
          :multiple="false"
          data-test="file-upload"
          aria-required="true"
          :pt="{
            fileThumbnail: { style: 'display: none;' },
            pcFileBadge: { root: { style: 'display: none;' } },
          }"
        >
          <template #empty>
            <span>Drag and drop a file here to upload</span>
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
        <Message v-if="showFileLimitError" severity="error" variant="simple" size="small" data-test="file-limit-error">
          You can only upload one file.
        </Message>
      </div>

      <div class="field">
        <label class="upload-label" for="document-name">Document Name<span class="required-asterisk">*</span></label>
        <InputText
          id="document-name"
          v-model="documentName"
          placeholder="Enter document name"
          :class="{ 'error-field': showErrors && !documentName }"
          data-test="document-name"
          aria-required="true"
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
        <label class="upload-label" for="document-category"
          >Document Category<span class="required-asterisk">*</span></label
        >
        <Select
          id="document-category"
          v-model="documentCategory"
          :options="documentCategories"
          optionLabel="label"
          optionValue="value"
          placeholder="Select category"
          :class="{ 'error-field': showErrors && !documentCategory }"
          data-test="document-category"
          aria-required="true"
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
          <label class="upload-label" for="publication-date">Publication Date</label>
          <DatePicker
            id="publication-date"
            v-model="publicationDate"
            showIcon
            placeholder="Select publication date"
            :showOnFocus="false"
            data-test="publication-date"
          />
        </div>
        <div class="field">
          <label class="upload-label" for="reporting-period">Reporting Period</label>
          <DatePicker
            id="reporting-period"
            v-model="reportingPeriod"
            :showIcon="true"
            view="year"
            dateFormat="yy"
            validation="required"
            placeholder="Select year"
            :showOnFocus="false"
            data-test="reporting-period"
          />
        </div>
      </div>

      <div class="actions">
        <div class="required-fields-note">
          <span class="required-asterisk">*</span>
          <span class="upload-label">required Fields</span>
        </div>
        <div class="button-row">
          <Button label="Cancel" class="p-button-text" @click="onCancel" data-test="cancel-button" />
          <Button
            label="Upload Document"
            class="p-button"
            @click="onSubmit"
            :disabled="isUploadButtonDisabled"
            data-test="upload-document-button"
          />
        </div>
      </div>
    </div>
  </PrimeDialog>
  <PrimeDialog
    id="successModal"
    v-model:visible="successModalIsVisible"
    modal
    :closable="false"
    :dismissableMask="true"
    style="border-radius: 0.75rem; text-align: center"
    :show-header="false"
    @hide="closeSuccessModal"
    data-test="successModal"
  >
    <div class="text-center" style="display: flex; flex-direction: column">
      <div style="margin: 10px">
        <em class="material-icons info-icon green-text" style="font-size: 2.5em"> check_circle </em>
      </div>
      <div style="margin: 10px">
        <h2 class="m-0" data-test="successText">Success</h2>
      </div>
    </div>
    <div class="text-block" style="margin: 15px; white-space: pre">Document uploaded successfully.</div>
    <Button label="CLOSE" @click="closeSuccessModal" variant="outlined" data-test="close-success-modal-button" />
  </PrimeDialog>
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
import { type DocumentMetaInfo, DocumentMetaInfoDocumentCategoryEnum } from '@clients/documentmanager';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type Keycloak from 'keycloak-js';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { isAxiosError } from 'axios';

const props = defineProps<{ visible: boolean; companyId: string }>();
const emit = defineEmits(['close', 'document-uploaded', 'conflict']);

const isVisible = ref<boolean>(props.visible);
const selectedFiles = ref<File[]>([]);
const documentName = ref<string>('');
const documentCategory = ref<DocumentMetaInfoDocumentCategoryEnum | null>(null);
const documentCategories = ref<
  {
    label: string;
    value: DocumentMetaInfoDocumentCategoryEnum;
  }[]
>([]);

for (const category of Object.values(DocumentMetaInfoDocumentCategoryEnum)) {
  documentCategories.value.push({
    label: humanizeStringOrNumber(category),
    value: category,
  });
}

const publicationDate = ref<Date | null>(null);
const reportingPeriod = ref<Date | null>(null);
const showErrors = ref<boolean>(false);
const successModalIsVisible = ref<boolean>(false);
const isUploadButtonDisabled = ref<boolean>(false);

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const documentControllerApi = apiClientProvider.apiClients.documentController;

const isFormValid = computed<boolean>(() => {
  return selectedFiles.value.length == 1 && !!documentName.value && !!documentCategory.value;
});

const showFileLimitError = computed<boolean>(() => {
  return selectedFiles.value.length > 1;
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
  console.log('Uploading document with meta info:', documentMetaInfo);
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
 * Cancels the modal and clears form values.
 */
const onCancel = (): void => {
  emit('close');
  resetForm();
};

/**
 * Submits the document upload form.
 */
const onSubmit = async (): Promise<void> => {
  showErrors.value = true;
  const fileToUpload = selectedFiles.value[0] || null;

  if (!isFormValid.value) {
    return;
  }

  isUploadButtonDisabled.value = true;

  console.log('Submitting:', {
    file: fileToUpload.name,
    documentName: documentName.value,
    documentCategory: documentCategory.value,
    publicationDate: publicationDate.value,
    reportingPeriod: reportingPeriod.value,
  });

  try {
    await handleDocumentUpload();
    successModalIsVisible.value = true;
  } catch (error: unknown) {
    const documentId = extractDocumentIdFromError(error);
    if (documentId) {
      emit('conflict', documentId);
    } else if (isAxiosError(error) && error.response?.status === 409) {
      console.error('Document already exists (409 Conflict), but no documentID found.');
    } else {
      console.error('Error uploading document:', error);
    }
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
 * Resets all form values.
 */
const resetForm = (): void => {
  selectedFiles.value = [];
  documentName.value = '';
  documentCategory.value = null;
  publicationDate.value = null;
  reportingPeriod.value = null;
  showErrors.value = false;
  isUploadButtonDisabled.value = false;
};

/**
 * Closes the success modal and resets the form.
 */
const closeSuccessModal = (): void => {
  successModalIsVisible.value = false;
  emit('close');
  resetForm();
  emit('document-uploaded');
};
</script>

<style scoped lang="scss">
.upload-document-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.button-row {
  gap: 1rem;
}

.required-fields-note {
  text-align: left;
  margin-top: 0;
  margin-bottom: 0;
}

.upload-label {
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-xs);
  margin-bottom: 0;
}

.required-asterisk {
  color: red;
  margin-left: 0.2em;
}

.date-row {
  display: flex;
  gap: 1rem;
}
.date-row .field {
  flex: 1;
}

.error-field {
  border-color: var(--message-error-border);
}

.green-text {
  color: var(--green);
}
</style>
