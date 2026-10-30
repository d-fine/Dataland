<template>
  <div class="inline-layout">
    <div class="inline-toolbar">
      <slot name="toolbar-start" />
      <div class="field-group">
        <Select
          :placeholder="'Select Quality'"
          v-model="chosenQuality"
          :options="qualityOptionsList"
          optionLabel="label"
          optionValue="value"
          data-test="quality-select"
          inputId="quality-select"
          aria-label="Quality"
          style="width: 10em"
        />
      </div>
      <div class="field-group">
        <div class="data-source-wrapper">
          <Select
            :placeholder="'Select Document'"
            v-model="selectedDocument"
            :options="availableDocuments"
            optionLabel="label"
            optionValue="value"
            data-test="document-select"
            style="width: 14em"
            inputId="document-select"
            aria-label="Data Source"
            fluid
          />
          <InputText
            v-model="insertedPage"
            placeholder="Page(s)"
            data-test="page-number-input"
            style="width: 6em"
            :disabled="!selectedDocument"
            :invalid="!isPageValid"
          />
          <PrimeButton
            label="Go to documents page"
            variant="link"
            as="a"
            :href="`/companies/${companyId}/documents`"
            target="_blank"
            rel="noopener noreferrer"
            data-test="go-to-documents-link"
          />
        </div>
      </div>
    </div>
    <small v-if="!isPageValid" data-test="page-number-error" class="page-number-error">
      {{ PAGE_NUMBER_VALIDATION_ERROR_MESSAGE }}
    </small>
    <div v-if="selectedDocumentMetaInformation" class="dataland-info-text small" style="margin: var(--spacing-xs)">
      <div><strong>Name:</strong> {{ selectedDocumentMetaInformation.documentName }}</div>
      <div><strong>Category:</strong> {{ selectedDocumentMetaInformation.documentCategory ?? '–' }}</div>
      <div><strong>Publication Date:</strong> {{ selectedDocumentMetaInformation.publicationDate ?? '–' }}</div>
      <div><strong>Reporting Period:</strong> {{ selectedDocumentMetaInformation.reportingPeriod ?? '–' }}</div>
    </div>

    <Textarea
      :placeholder="'Insert comment'"
      v-model="insertedComment"
      data-test="comment-textarea"
      id="comment-input"
      aria-label="Comment"
      rows="2"
      :draggable="false"
      fluid
      style="margin-top: var(--spacing-xs)"
    />
  </div>
</template>

<script setup lang="ts">
import Select from 'primevue/select';
import PrimeButton from 'primevue/button';
import Textarea from 'primevue/textarea';
import InputText from 'primevue/inputtext';
import { PAGE_NUMBER_VALIDATION_ERROR_MESSAGE } from '@/utils/ValidationUtils.ts';
import type { ExtendedDataPointType } from '@/components/resources/dataTable/conversion/Utils.ts';
import {
  qualityOptionsList,
  useExtendedDataPointForm,
} from '@/components/resources/dataTable/modals/useExtendedDataPointForm.ts';

const props = defineProps<{
  extendedDataPointObject?: ExtendedDataPointType;
}>();

const {
  companyId,
  chosenQuality,
  selectedDocument,
  insertedComment,
  insertedPage,
  availableDocuments,
  selectedDocumentMetaInformation,
  isPageValid,
  getFormData,
} = useExtendedDataPointForm(props.extendedDataPointObject);

defineExpose({
  getFormData,
  isPageValid,
});
</script>

<style scoped>
.inline-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--spacing-sm);
}

.inline-layout .field-group {
  display: flex;
  align-items: center;
}

.data-source-wrapper {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.page-number-error {
  color: var(--input-error);
  display: block;
  margin-top: var(--spacing-xxs);
}
</style>
