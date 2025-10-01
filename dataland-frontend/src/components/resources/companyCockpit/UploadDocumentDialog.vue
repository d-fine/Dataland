<template>
  <Dialog
    v-model:visible="isVisible"
    modal
    header="Upload Document"
    :style="{ width: '40rem' }"
    data-test="upload-document-modal"
    @hide="emit('close')"
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
        <Message v-if="showFileLimitError" severity="error" variant="simple" size="small">
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
        <div>
          <Button label="Cancel" class="p-button-text" @click="onCancel" data-test="cancel-button" />
          <Button label="Upload Document" class="p-button" @click="onSubmit" data-test="upload-document-button" />
        </div>
      </div>
    </div>
  </Dialog>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import Dialog from 'primevue/dialog';
import FileUpload from 'primevue/fileupload';
import type { FileUploadSelectEvent, FileUploadRemoveEvent } from 'primevue/fileupload';
import InputText from 'primevue/inputtext';
import Select from 'primevue/select';
import DatePicker from 'primevue/datepicker';
import Button from 'primevue/button';
import Message from 'primevue/message';

interface DocumentCategory {
  label: string;
  value: string;
}

const props = defineProps<{ visible: boolean }>();
const emit = defineEmits(['close']);

const isVisible = ref<boolean>(props.visible);
const selectedFiles = ref<File[]>([]);
const documentName = ref<string>('');
const documentCategory = ref<string>('');
const documentCategories = ref<DocumentCategory[]>([
  { label: 'Finance', value: 'finance' },
  { label: 'Legal', value: 'legal' },
  { label: 'HR', value: 'hr' },
]);
const publicationDate = ref<Date | null>(null);
const reportingPeriod = ref<Date | null>(null);
const showErrors = ref<boolean>(false);

/**
 * Computed property to check if form is valid.
 */
const isFormValid = computed<boolean>(() => {
  return selectedFiles.value.length == 1 && !!documentName.value && !!documentCategory.value;
});

const showFileLimitError = computed<boolean>(() => {
  return selectedFiles.value.length > 1;
});

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
const onSubmit = (): void => {
  showErrors.value = true;
  const fileToUpload = selectedFiles.value[0] || null;

  if (!isFormValid.value) {
    return;
  }

  console.log('Submitting:', {
    file: fileToUpload.name,
    documentName: documentName.value,
    documentCategory: documentCategory.value,
    publicationDate: publicationDate.value,
    reportingPeriod: reportingPeriod.value,
  });

  emit('close');
  resetForm();
};

/**
 * Resets all form values.
 */
const resetForm = (): void => {
  selectedFiles.value = [];
  documentName.value = '';
  documentCategory.value = '';
  publicationDate.value = null;
  reportingPeriod.value = null;
  showErrors.value = false;
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
</style>
