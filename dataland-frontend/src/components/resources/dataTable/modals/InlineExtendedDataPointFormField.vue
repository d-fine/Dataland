<template>
  <div class="inline-layout">
    <Divider />
    <div class="inline-toolbar">
      <div class="field-group">
        <label for="quality-select" class="field-label">Quality</label>
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
          <div class="field-group">
            <label for="document-select" class="field-label">Document</label>
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
          </div>
          <div class="field-group">
            <label for="page-number-input" class="field-label">Page(s)</label>
            <InputText
              v-model="insertedPage"
              id="page-number-input"
              placeholder="Page(s)"
              data-test="page-number-input"
              style="width: 6em"
              :disabled="!selectedDocument"
              :invalid="!isPageValid"
            />
          </div>
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

    <div class="field-group comment-group">
      <label for="comment-input" class="field-label">Comment</label>
      <Textarea
        :placeholder="'Insert comment'"
        v-model="insertedComment"
        data-test="comment-textarea"
        id="comment-input"
        aria-label="Comment"
        rows="2"
        :draggable="false"
        fluid
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { inject } from 'vue';
import Select from 'primevue/select';
import Divider from 'primevue/divider';
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

const companyId = inject<string>('companyId');
if (!companyId) throw new Error("Missing provided 'companyId'");

const {
  chosenQuality,
  selectedDocument,
  insertedComment,
  insertedPage,
  availableDocuments,
  selectedDocumentMetaInformation,
  isPageValid,
  getFormData,
} = useExtendedDataPointForm(companyId, props.extendedDataPointObject);

defineExpose({
  getFormData,
  isPageValid,
});
</script>

<style scoped>
.comment-group {
  margin-top: var(--spacing-xs);
  align-items: stretch !important;
}

.inline-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: var(--spacing-sm);
}

.inline-layout .field-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--spacing-xxs);
}

.field-label {
  font-size: var(--font-size-sm, 0.875rem);
  font-weight: 600;
}

.data-source-wrapper {
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-sm);
}

.page-number-error {
  color: var(--input-error);
  display: block;
  margin-top: var(--spacing-xxs);
}
</style>
